package com.finora.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.app.BuildConfig
import com.finora.app.presentation.viewmodel.MainViewModel
import com.finora.app.utils.BiometricHelper
import com.finora.app.utils.findFragmentActivity

private enum class PinDialogMode {
    CREATE,
    CHANGE,
    DISABLE
}

@Composable
fun ProfileScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val biometricSupported = remember(context) { BiometricHelper.isBiometricAvailable(context) }

    var pinDialogMode by remember { mutableStateOf<PinDialogMode?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Color(0xFF10B981), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Avatar",
                tint = Color.White,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Pengguna FINORA", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "Versi ${BuildConfig.VERSION_NAME}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Dompet", state.wallets.size.toString())
                StatItem("Transaksi", state.transactions.size.toString())
                StatItem("Target", state.goals.size.toString())
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Tampilan",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Mode Gelap", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(
                    "Sesuaikan tampilan untuk kenyamanan mata",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = state.isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode(it) }
            )
        }
        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Keamanan",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text("Kunci PIN (6 Digit)", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (state.isPinEnabled) "Aplikasi dilindungi PIN saat dibuka"
                        else "Lindungi catatan keuangan dengan PIN",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = state.isPinEnabled,
                onCheckedChange = { enable ->
                    pinDialogMode = if (enable) PinDialogMode.CREATE else PinDialogMode.DISABLE
                }
            )
        }
        HorizontalDivider()

        if (state.isPinEnabled) {
            TextButton(
                onClick = { pinDialogMode = PinDialogMode.CHANGE },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.Password, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ubah PIN 6 Digit", modifier = Modifier.weight(1f))
            }
            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Buka dengan Sidik Jari", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(
                            if (biometricSupported) "Akses cepat tanpa mengetik PIN"
                            else "Sensor sidik jari belum aktif / tidak tersedia di perangkat",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = state.isBiometricEnabled,
                    enabled = biometricSupported,
                    onCheckedChange = { enable ->
                        if (enable && activity != null) {
                            BiometricHelper.showBiometricPrompt(
                                activity = activity,
                                title = "Aktifkan Sidik Jari",
                                subtitle = "Verifikasi sidik jari untuk mengaktifkan login cepat",
                                negativeButtonText = "Batal",
                                onSuccess = { viewModel.setBiometricEnabled(true) }
                            )
                        } else {
                            viewModel.setBiometricEnabled(false)
                        }
                    }
                )
            }
            HorizontalDivider()
        }
    }

    pinDialogMode?.let { mode ->
        PinConfigDialog(
            mode = mode,
            onDismiss = { pinDialogMode = null },
            onVerifyOldPin = { viewModel.verifyPin(it) },
            onCreateOrChangePin = { newPin ->
                viewModel.setupPin(newPin)
                pinDialogMode = null
            },
            onDisablePin = { currentPin ->
                val ok = viewModel.disablePin(currentPin)
                if (ok) pinDialogMode = null
                ok
            }
        )
    }
}

@Composable
private fun PinConfigDialog(
    mode: PinDialogMode,
    onDismiss: () -> Unit,
    onVerifyOldPin: (String) -> Boolean,
    onCreateOrChangePin: (String) -> Unit,
    onDisablePin: (String) -> Boolean
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val title = when (mode) {
        PinDialogMode.CREATE -> "Buat PIN 6 Digit"
        PinDialogMode.CHANGE -> "Ubah PIN"
        PinDialogMode.DISABLE -> "Nonaktifkan Kunci PIN"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (mode == PinDialogMode.CHANGE || mode == PinDialogMode.DISABLE) {
                    OutlinedTextField(
                        value = oldPin,
                        onValueChange = {
                            if (it.length <= 6) oldPin = it.filter(Char::isDigit)
                            error = null
                        },
                        label = { Text("PIN Saat Ini") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (mode == PinDialogMode.CREATE || mode == PinDialogMode.CHANGE) {
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            if (it.length <= 6) newPin = it.filter(Char::isDigit)
                            error = null
                        },
                        label = { Text("PIN Baru (6 angka)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            if (it.length <= 6) confirmPin = it.filter(Char::isDigit)
                            error = null
                        },
                        label = { Text("Ulangi PIN Baru") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when (mode) {
                    PinDialogMode.CREATE -> {
                        when {
                            newPin.length != 6 -> error = "PIN harus tepat 6 angka."
                            newPin != confirmPin -> error = "Konfirmasi PIN tidak cocok."
                            else -> onCreateOrChangePin(newPin)
                        }
                    }
                    PinDialogMode.CHANGE -> {
                        when {
                            !onVerifyOldPin(oldPin) -> error = "PIN saat ini salah."
                            newPin.length != 6 -> error = "PIN baru harus tepat 6 angka."
                            newPin != confirmPin -> error = "Konfirmasi PIN baru tidak cocok."
                            else -> onCreateOrChangePin(newPin)
                        }
                    }
                    PinDialogMode.DISABLE -> {
                        if (!onDisablePin(oldPin)) {
                            error = "PIN saat ini salah."
                        }
                    }
                }
            }) {
                Text(if (mode == PinDialogMode.DISABLE) "Nonaktifkan" else "Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
