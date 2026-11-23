package com.finora.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.app.domain.model.Wallet
import com.finora.app.presentation.viewmodel.MainViewModel
import com.finora.app.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    onSuccess: () -> Unit,
    onScanClick: () -> Unit,
    scannedAmount: Double?,
    onScannedAmountConsumed: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("expense") }
    var selectedWalletId by remember { mutableStateOf("") }
    var selectedToWalletId by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Makanan") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val expenseCategories = remember {
        listOf("Makanan", "Transportasi", "Belanja", "Tagihan", "Hiburan", "Kesehatan", "Pendidikan", "Lainnya")
    }
    val incomeCategories = remember {
        listOf("Gaji", "Bonus", "Usaha", "Investasi", "Hadiah", "Lainnya")
    }

    LaunchedEffect(scannedAmount) {
        if (scannedAmount != null) {
            amount = scannedAmount.toLong().toString()
            errorMessage = null
            onScannedAmountConsumed()
        }
    }

    LaunchedEffect(state.wallets) {
        if (state.wallets.isNotEmpty()) {
            if (selectedWalletId.isEmpty() || state.wallets.none { it.id == selectedWalletId }) {
                selectedWalletId = state.wallets.first().id
            }
            if (state.wallets.size >= 2) {
                if (selectedToWalletId.isEmpty() || selectedToWalletId == selectedWalletId ||
                    state.wallets.none { it.id == selectedToWalletId }
                ) {
                    selectedToWalletId = state.wallets.first { it.id != selectedWalletId }.id
                }
            }
        }
    }

    LaunchedEffect(selectedType) {
        selectedCategory = when (selectedType) {
            "income" -> incomeCategories.first()
            "expense" -> expenseCategories.first()
            else -> "Transfer"
        }
        errorMessage = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == "expense",
                    onClick = { selectedType = "expense" },
                    label = { Text("Pengeluaran") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == "income",
                    onClick = { selectedType = "income" },
                    label = { Text("Pemasukan") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == "transfer",
                    onClick = { selectedType = "transfer" },
                    label = { Text("Transfer") },
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.wallets.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Belum ada dompet tersedia.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.addWallet("Dompet Tunai", "cash", 0.0) }) {
                            Text("Buat Dompet Tunai")
                        }
                    }
                }
            } else {
                WalletDropdown(
                    label = if (selectedType == "transfer") "Dari Dompet" else "Dompet",
                    wallets = state.wallets,
                    balances = state.walletBalances,
                    selectedWalletId = selectedWalletId,
                    onWalletSelected = { chosen ->
                        selectedWalletId = chosen
                        if (selectedToWalletId == chosen && state.wallets.size >= 2) {
                            selectedToWalletId = state.wallets.first { it.id != chosen }.id
                        }
                        errorMessage = null
                    }
                )

                if (selectedType == "transfer") {
                    if (state.wallets.size < 2) {
                        Text(
                            "Tambahkan minimal 2 dompet di menu Dompet untuk melakukan transfer.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        val targetWallets = state.wallets.filter { it.id != selectedWalletId }
                        WalletDropdown(
                            label = "Ke Dompet",
                            wallets = targetWallets,
                            balances = state.walletBalances,
                            selectedWalletId = selectedToWalletId,
                            onWalletSelected = {
                                selectedToWalletId = it
                                errorMessage = null
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { ch -> ch.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Jumlah (Rp)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalIconButton(
                    onClick = onScanClick,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Scan Struk"
                    )
                }
            }

            if (selectedType != "transfer") {
                val categories = if (selectedType == "income") incomeCategories else expenseCategories
                Text("Kategori", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Catatan (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    when {
                        state.wallets.isEmpty() || selectedWalletId.isEmpty() -> {
                            errorMessage = "Pilih atau buat dompet terlebih dahulu."
                        }
                        selectedType == "transfer" && state.wallets.size < 2 -> {
                            errorMessage = "Minimal harus memiliki 2 dompet untuk transfer."
                        }
                        selectedType == "transfer" && (selectedToWalletId.isEmpty() || selectedToWalletId == selectedWalletId) -> {
                            errorMessage = "Dompet tujuan harus berbeda dari dompet asal."
                        }
                        amt == null || amt <= 0.0 -> {
                            errorMessage = "Masukkan jumlah nominal yang valid (> 0)."
                        }
                        else -> {
                            viewModel.addTransaction(
                                type = selectedType,
                                amount = amt,
                                walletId = selectedWalletId,
                                category = selectedCategory,
                                note = note,
                                toWalletId = if (selectedType == "transfer") selectedToWalletId else null
                            )
                            onSuccess()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Simpan Transaksi")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletDropdown(
    label: String,
    wallets: List<Wallet>,
    balances: Map<String, Double>,
    selectedWalletId: String,
    onWalletSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedWallet = wallets.find { it.id == selectedWalletId } ?: wallets.firstOrNull()
    val displayValue = selectedWallet?.let {
        val bal = balances[it.id] ?: it.initialBalance
        "${it.name} (${FormatUtils.formatRupiah(bal)})"
    } ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            wallets.forEach { wallet ->
                val bal = balances[wallet.id] ?: wallet.initialBalance
                DropdownMenuItem(
                    text = { Text("${wallet.name} (${FormatUtils.formatRupiah(bal)})") },
                    onClick = {
                        onWalletSelected(wallet.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
