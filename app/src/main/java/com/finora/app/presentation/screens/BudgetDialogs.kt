package com.finora.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.finora.app.domain.model.Goal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddBudgetDialog(onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    val presetCategories = listOf("Makanan", "Transportasi", "Belanja", "Tagihan", "Hiburan", "Kesehatan")
    var category by remember { mutableStateOf("Makanan") }
    var limit by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur Anggaran Bulanan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetCategories.forEach { preset ->
                        FilterChip(
                            selected = category.equals(preset, ignoreCase = true),
                            onClick = { category = preset },
                            label = { Text(preset) }
                        )
                    }
                }
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it; error = null },
                    label = { Text("Kategori") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it.filter { c -> c.isDigit() }; error = null },
                    label = { Text("Batas Maksimal (Rp)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsed = limit.toDoubleOrNull()
                if (category.isBlank() || parsed == null || parsed <= 0.0) {
                    error = "Isi kategori dan batas anggaran (> 0)."
                } else {
                    onSave(category, parsed)
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
internal fun AddGoalDialog(onDismiss: () -> Unit, onSave: (String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var initial by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Target Tabungan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Nama Target (mis. Dana Darurat)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it.filter { c -> c.isDigit() }; error = null },
                    label = { Text("Target Nominal (Rp)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = initial,
                    onValueChange = { initial = it.filter { c -> c.isDigit() } },
                    label = { Text("Saldo Terkumpul Awal (opsional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val targetVal = target.toDoubleOrNull()
                val initialVal = initial.toDoubleOrNull() ?: 0.0
                if (name.isBlank() || targetVal == null || targetVal <= 0.0) {
                    error = "Nama target dan nominal target (> 0) wajib diisi."
                } else {
                    onSave(name, targetVal, initialVal)
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
internal fun TopUpGoalDialog(goal: Goal, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah ke ${goal.name}") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() } },
                label = { Text("Nominal Tabungan (Rp)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val parsed = amount.toDoubleOrNull()
                if (parsed != null && parsed > 0.0) {
                    onSave(parsed)
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AddBillDialog(onDismiss: () -> Unit, onSave: (String, Double, Long, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var daysUntilDue by remember { mutableIntStateOf(7) }
    var recurring by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val dayOptions = listOf(1 to "Besok", 3 to "+3 Hari", 7 to "+7 Hari", 14 to "+14 Hari", 30 to "+30 Hari")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Tagihan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Nama Tagihan (mis. Listrik, WiFi)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() }; error = null },
                    label = { Text("Nominal Tagihan (Rp)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Jatuh Tempo", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    dayOptions.forEach { (days, label) ->
                        FilterChip(
                            selected = daysUntilDue == days,
                            onClick = { daysUntilDue = days },
                            label = { Text(label) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tagihan rutin bulanan")
                    Switch(checked = recurring, onCheckedChange = { recurring = it })
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull()
                if (name.isBlank() || amt == null || amt <= 0.0) {
                    error = "Isi nama tagihan dan nominal (> 0)."
                } else {
                    val dueMillis = System.currentTimeMillis() + daysUntilDue * 86_400_000L
                    onSave(name, amt, dueMillis, recurring)
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
