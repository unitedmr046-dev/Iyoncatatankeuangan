package com.finora.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.app.presentation.viewmodel.MainViewModel
import com.finora.app.utils.FormatUtils
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val modelProducer = remember { CartesianChartModelProducer() }
    val hasData = state.totalIncome > 0.0 || state.totalExpense > 0.0

    val bottomFormatter = remember {
        CartesianValueFormatter { _, value, _ ->
            when (value) {
                0.0 -> "Pemasukan"
                1.0 -> "Pengeluaran"
                else -> " "
            }
        }
    }

    LaunchedEffect(state.totalIncome, state.totalExpense) {
        if (hasData) {
            modelProducer.runTransaction {
                columnSeries {
                    series(listOf(state.totalIncome, state.totalExpense))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Laporan Keuangan") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Total Pemasukan",
                    value = FormatUtils.formatRupiah(state.totalIncome),
                    valueColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Total Pengeluaran",
                    value = FormatUtils.formatRupiah(state.totalExpense),
                    valueColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Perbandingan Pemasukan vs Pengeluaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!hasData) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada data transaksi untuk ditampilkan.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberColumnCartesianLayer(),
                                startAxis = VerticalAxis.rememberStart(),
                                bottomAxis = HorizontalAxis.rememberBottom(
                                    valueFormatter = bottomFormatter,
                                    itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() }
                                )
                            ),
                            modelProducer = modelProducer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Pengeluaran per Kategori (${FormatUtils.currentMonthLabel()})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.categorySpentMap.isEmpty()) {
                        Text(
                            "Belum ada pengeluaran bulan ini.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val totalMonthlyExp = state.monthlyExpense.coerceAtLeast(1.0)
                        state.categorySpentMap.entries
                            .sortedByDescending { it.value }
                            .forEach { (cat, amt) ->
                                val pct = (amt / totalMonthlyExp).toFloat().coerceIn(0f, 1f)
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            cat.replaceFirstChar { it.uppercase() },
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text("${FormatUtils.formatRupiah(amt)} (${(pct * 100).toInt()}%)")
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { pct },
                                        modifier = Modifier.fillMaxWidth().height(6.dp)
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}
