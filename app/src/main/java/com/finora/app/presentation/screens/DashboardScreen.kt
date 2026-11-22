package com.finora.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Saldo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF064E3B))),
                    RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text("Total Saldo", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Text(
                    text = formatRupiah.format(state.totalBalance),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Pemasukan", color = Color(0xFF34D399), fontSize = 12.sp)
                        Text(formatRupiah.format(state.totalIncome), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("Pengeluaran", color = Color(0xFFF87171), fontSize = 12.sp)
                        Text(formatRupiah.format(state.totalExpense), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Transaksi Terbaru",
            modifier = Modifier.padding(horizontal = 24.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        if (state.transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada transaksi. Yuk mulai catat!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.transactions) { tx ->
                    TransactionItem(tx, formatRupiah)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(tx: Transaction, formatRupiah: NumberFormat) {
    val isIncome = tx.type == "income"
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isIncome) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isIncome) "↓" else "↑", fontSize = 24.sp, color = if (isIncome) Color(0xFF059669) else Color(0xFFEF4444))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.note.ifEmpty { tx.category }, fontWeight = FontWeight.Bold)
                Text(dateFormat.format(Date(tx.date)), fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = (if (isIncome) "+" else "-") + formatRupiah.format(tx.amount),
                color = if (isIncome) Color(0xFF059669) else Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
