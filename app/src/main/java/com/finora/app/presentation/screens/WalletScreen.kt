package com.finora.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.app.presentation.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WalletScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Dompet")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.wallets) { wallet ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(wallet.name, style = MaterialTheme.typography.titleMedium)
                        Text("Saldo Awal: ${formatRupiah.format(wallet.initialBalance)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Tipe: ${wallet.type.uppercase()}", style = MaterialTheme.typography.bodySmall)
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
}

@Composable
fun AddWalletDialog(onDismiss: () -> Unit, onSave: (String, String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("cash") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Dompet") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Dompet") })
                OutlinedTextField(
                    value = balance, onValueChange = { balance = it },
                    label = { Text("Saldo Awal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank() && balance.isNotBlank()) {
                    onSave(name, type, balance.toDoubleOrNull() ?: 0.0)
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
