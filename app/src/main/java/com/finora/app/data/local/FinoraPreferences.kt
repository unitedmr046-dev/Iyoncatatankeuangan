package com.finora.app.data.local

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinoraPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("finora_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isPinEnabled = MutableStateFlow(hasPin())
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(
        hasPin() && prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    )
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled
    }

    fun hasSeededDefaultWallet(): Boolean =
        prefs.getBoolean(KEY_SEEDED_DEFAULT_WALLET, false)

    fun markDefaultWalletSeeded() {
        prefs.edit().putBoolean(KEY_SEEDED_DEFAULT_WALLET, true).apply()
    }

    fun hasPin(): Boolean =
        !prefs.getString(KEY_PIN_HASH, null).isNullOrEmpty()

    fun setPin(pin: String) {
        val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        val hash = hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .apply()
        _isPinEnabled.value = true
    }

    fun verifyPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return MessageDigest.isEqual(
            hashPin(pin, salt).toByteArray(),
            savedHash.toByteArray()
        )
    }

    fun clearPin() {
        prefs.edit()
            .remove(KEY_PIN_SALT)
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
        _isPinEnabled.value = false
        _isBiometricEnabled.value = false
    }

    fun setBiometricEnabled(enabled: Boolean) {
        val allow = enabled && hasPin()
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, allow).apply()
        _isBiometricEnabled.value = allow
    }

    private fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$pin".toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_SEEDED_DEFAULT_WALLET = "seeded_default_wallet"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
}
