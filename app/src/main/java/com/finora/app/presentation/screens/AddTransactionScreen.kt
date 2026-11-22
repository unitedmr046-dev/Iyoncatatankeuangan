package com.finora.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.app.presentation.viewmodel.MainViewModel

@Composable
fun AddTransactionScreen(
    onSuccess: () -> Unit,
    onScanClick: () -> Unit,
    scannedAmount: Double?,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("expense") }
    var selectedWalletId by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Makanan") }

    LaunchedEffect(scannedAmount) {
        if (scannedAmount != null) {
            amount = scannedAmount.toLong().toString()
        }
    }

    LaunchedEffect(state.wallets) {
        if (state.wallets.isNotEmpty() && selectedWalletId.isEmpty()) {
            selectedWalletId = state.wallets.first().id
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tambah Transaksi", style = MaterialTheme.typography.headlineMedium)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            FilterChip(selected = selectedType == "expense", onClick = { selectedType = "expense" }, label = { Text("Pengeluaran") })
            FilterChip(selected = selectedType == "income", onClick = { selectedType = "income" }, label = { Text("Pemasukan") })
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = amount, onValueChange = { amount = it },
                label = { Text("Jumlah") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onScanClick) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Scan Struk", tint = MaterialTheme.colorScheme.primary)
            }
        }

        OutlinedTextField(
            value = note, onValueChange = { note = it },
            label = { Text("Catatan (Opsional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val amt = amount.toDoubleOrNull()
                if (amt != null && selectedWalletId.isNotEmpty()) {
                    viewModel.addTransaction(selectedType, amt, selectedWalletId, selectedCategory, note)
                    onSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan Transaksi")
        }
    }
}
