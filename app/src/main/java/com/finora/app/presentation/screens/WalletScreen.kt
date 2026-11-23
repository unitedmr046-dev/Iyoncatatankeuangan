package com.finora.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.app.domain.model.Wallet
import com.finora.app.presentation.viewmodel.MainViewModel
import com.finora.app.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var walletToDelete by remember { mutableStateOf<Wallet?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dompet Saya") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Dompet")
            }
        }
    ) { padding ->
        if (state.wallets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Belum ada dompet. Tekan + untuk menambah dompet pertama.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.wallets, key = { it.id }) { wallet ->
                    val currentBalance = state.walletBalances[wallet.id] ?: wallet.initialBalance
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        wallet.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(walletTypeLabel(wallet.type)) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Saldo Saat Ini: ${FormatUtils.formatRupiah(currentBalance)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Saldo Awal: ${FormatUtils.formatRupiah(wallet.initialBalance)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { walletToDelete = wallet }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Hapus Dompet",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddWalletDialog(
            onDismiss = { showDialog = false },
            onSave = { name, type, balance ->
                viewModel.addWallet(name, type, balance)
                showDialog = false
            }
        )
    }

    walletToDelete?.let { wallet ->
        AlertDialog(
            onDismissRequest = { walletToDelete = null },
            title = { Text("Hapus Dompet?") },
            text = {
                Text("Menghapus \"${wallet.name}\" juga akan menghapus semua riwayat transaksi pada dompet ini.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWallet(wallet)
                        walletToDelete = null
                    }
                ) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { walletToDelete = null }) { Text("Batal") }
            }
        )
    }
}

private fun walletTypeLabel(type: String): String = when (type) {
    "cash" -> "Tunai"
    "bank" -> "Bank"
    "ewallet" -> "E-Wallet"
    "credit" -> "Kredit"
    else -> type.uppercase()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddWalletDialog(onDismiss: () -> Unit, onSave: (String, String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("cash") }
    var error by remember { mutableStateOf<String?>(null) }

    val walletTypes = listOf(
        "cash" to "Tunai",
        "bank" to "Bank",
        "ewallet" to "E-Wallet",
        "credit" to "Kredit"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Dompet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Nama Dompet (mis. BCA, GoPay)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balance,
                    onValueChange = {
                        balance = it.filter { ch -> ch.isDigit() }
                        error = null
                    },
                    label = { Text("Saldo Awal (opsional)") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Tipe Dompet", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    walletTypes.forEach { (key, label) ->
                        FilterChip(
                            selected = type == key,
                            onClick = { type = key },
                            label = { Text(label) }
                        )
                    }
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) {
                    error = "Nama dompet wajib diisi."
                    return@TextButton
                }
                val parsedBalance = if (balance.isBlank()) 0.0 else balance.toDoubleOrNull()
                if (parsedBalance == null) {
                    error = "Saldo awal tidak valid."
                    return@TextButton
                }
                onSave(name, type, parsedBalance)
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
