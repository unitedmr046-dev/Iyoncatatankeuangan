package com.finora.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.app.utils.BiometricHelper
import com.finora.app.utils.findFragmentActivity
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    isBiometricEnabled: Boolean,
    onUnlockWithPin: (String) -> Boolean,
    onUnlockWithBiometric: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val canUseBiometric = remember(context, isBiometricEnabled) {
        isBiometricEnabled && activity != null && BiometricHelper.isBiometricAvailable(context)
    }

    var pin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var lockoutSeconds by remember { mutableIntStateOf(0) }

    fun triggerBiometric() {
        if (canUseBiometric && activity != null && lockoutSeconds == 0) {
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                onSuccess = onUnlockWithBiometric,
                onErrorOrFallback = { msg ->
                    if (msg != null) errorText = msg
                }
            )
        }
    }

    LaunchedEffect(canUseBiometric) {
        if (canUseBiometric) {
            triggerBiometric()
        }
    }

    LaunchedEffect(lockoutSeconds) {
        if (lockoutSeconds > 0) {
            delay(1000L)
            lockoutSeconds -= 1
            if (lockoutSeconds == 0) {
                failedAttempts = 0
                errorText = null
            }
        }
    }

    fun onDigitPressed(digit: String) {
        if (lockoutSeconds > 0 || pin.length >= 6) return
        errorText = null
        val updated = pin + digit
        pin = updated
        if (updated.length == 6) {
            if (!onUnlockWithPin(updated)) {
                pin = ""
                failedAttempts += 1
                if (failedAttempts >= 5) {
                    lockoutSeconds = 30
                    errorText = "Terlalu banyak percobaan. Coba lagi dalam 30 detik."
                } else {
                    errorText = "PIN salah (${failedAttempts}/5)"
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 36.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Terkunci",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "FINORA Terkunci",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Masukkan 6 digit PIN untuk membuka aplikasi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    repeat(6) { index ->
                        val filled = index < pin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (filled) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (filled) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val statusText = if (lockoutSeconds > 0) {
                    "Terkunci sementara: $lockoutSeconds detik"
                } else {
                    errorText ?: ""
                }
                Text(
                    text = statusText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.height(20.dp)
                )
            }

            PinNumberPad(
                enabled = lockoutSeconds == 0,
                showBiometric = canUseBiometric,
                onDigit = { onDigitPressed(it) },
                onBackspace = {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                },
                onBiometricClick = { triggerBiometric() }
            )
        }
    }
}

@Composable
private fun PinNumberPad(
    enabled: Boolean,
    showBiometric: Boolean,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onBiometricClick: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { digit ->
                    PadDigitButton(digit = digit, enabled = enabled) { onDigit(digit) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            if (showBiometric) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable(enabled = enabled) { onBiometricClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = "Sidik Jari",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }

            PadDigitButton(digit = "0", enabled = enabled) { onDigit("0") }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable(enabled = enabled) { onBackspace() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun PadDigitButton(
    digit: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = digit,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
