package com.finora.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.app.domain.model.Transaction
import com.finora.app.presentation.viewmodel.MainViewModel
import com.finora.app.utils.FormatUtils

@Composable
fun DashboardScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onAddTransactionClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val walletNameMap = remember(state.wallets) {
        state.wallets.associate { it.id to it.name }
    }
    var txToDelete by remember { mutableStateOf<Transaction?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF064E3B))),
                    RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                )
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    "Total Saldo",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = FormatUtils.formatRupiah(state.totalBalance),
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Pemasukan Bulan Ini", color = Color(0xFF34D399), fontSize = 12.sp)
                        Text(
                            FormatUtils.formatRupiah(state.monthlyIncome),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pengeluaran Bulan Ini", color = Color(0xFFF87171), fontSize = 12.sp)
                        Text(
                            FormatUtils.formatRupiah(state.monthlyExpense),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        val unpaidBills = state.bills.filter { !it.isPaid }
        if (unpaidBills.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Ada ${unpaidBills.size} tagihan belum dibayar (Total ${FormatUtils.formatRupiah(unpaidBills.sumOf { it.amount })})",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Transaksi Terbaru",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            TextButton(onClick = onAddTransactionClick) {
                Text("+ Catat")
            }
        }

        if (state.transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Belum ada transaksi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tekan tombol + di kanan bawah untuk mulai mencatat.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.transactions, key = { it.id }) { tx ->
                    val walletLabel = if (tx.type == "transfer") {
                        "${walletNameMap[tx.walletId] ?: "Dompet"} -> ${walletNameMap[tx.toWalletId] ?: "Dompet"}"
                    } else {
                        walletNameMap[tx.walletId] ?: "Dompet"
                    }
                    TransactionItem(
                        tx = tx,
                        walletLabel = walletLabel,
                        onDeleteClick = { txToDelete = tx }
                    )
                }
            }
        }
    }

    txToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("Hapus Transaksi?") },
            text = { Text("Transaksi ${tx.category} sebesar ${FormatUtils.formatRupiah(tx.amount)} akan dihapus.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        txToDelete = null
                    }
                ) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun TransactionItem(
    tx: Transaction,
    walletLabel: String,
    onDeleteClick: () -> Unit
) {
    val isIncome = tx.type == "income"
    val isTransfer = tx.type == "transfer"

    val iconBg = when {
        isIncome -> Color(0xFFD1FAE5)
        isTransfer -> Color(0xFFDBEAFE)
        else -> Color(0xFFFEE2E2)
    }
    val iconTint = when {
        isIncome -> Color(0xFF059669)
        isTransfer -> Color(0xFF2563EB)
        else -> Color(0xFFEF4444)
    }
    val prefix = when {
        isIncome -> "+"
        isTransfer -> ""
        else -> "-"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isIncome -> Icons.Default.ArrowDownward
                        isTransfer -> Icons.Default.SwapHoriz
                        else -> Icons.Default.ArrowUpward
                    },
                    contentDescription = tx.type,
                    tint = iconTint
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tx.note.ifEmpty { tx.category },
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    "${tx.category} • $walletLabel",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    FormatUtils.formatDateTime(tx.date),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = prefix + FormatUtils.formatRupiah(tx.amount),
                    color = iconTint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
