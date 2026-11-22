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
fun BudgetScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Anggaran")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.budgets) { budget ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(budget.category, style = MaterialTheme.typography.titleMedium)
                        Text("Batas: ${formatRupiah.format(budget.limitAmount)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddBudgetDialog(
            onDismiss = { showDialog = false },
            onSave = { category, limit ->
                viewModel.addBudget(category, limit)
                showDialog = false
            }
        )
    }
}

@Composable
fun AddBudgetDialog(onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var category by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Anggaran") },
        text = {
            Column {
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori (e.g. Makanan)") })
                OutlinedTextField(
                    value = limit, onValueChange = { limit = it },
                    label = { Text("Batas Maksimal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (category.isNotBlank() && limit.isNotBlank()) {
                    onSave(category, limit.toDoubleOrNull() ?: 0.0)
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
