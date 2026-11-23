package com.finora.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.app.domain.model.Bill
import com.finora.app.domain.model.Budget
import com.finora.app.domain.model.Goal
import com.finora.app.presentation.viewmodel.MainViewModel
import com.finora.app.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Anggaran", "Target", "Tagihan")

    var showBudgetDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showBillDialog by remember { mutableStateOf(false) }
    var goalToTopUp by remember { mutableStateOf<Goal?>(null) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Perencanaan Keuangan") })
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> showBudgetDialog = true
                        1 -> showGoalDialog = true
                        2 -> showBillDialog = true
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> BudgetsTab(
                budgets = state.budgets,
                categorySpentMap = state.categorySpentMap,
                onDelete = { viewModel.deleteBudget(it) },
                modifier = Modifier.padding(padding)
            )
            1 -> GoalsTab(
                goals = state.goals,
                onTopUp = { goalToTopUp = it },
                onDelete = { viewModel.deleteGoal(it) },
                modifier = Modifier.padding(padding)
            )
            2 -> BillsTab(
                bills = state.bills,
                onTogglePaid = { viewModel.toggleBillPaid(it) },
                onDelete = { viewModel.deleteBill(it) },
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showBudgetDialog = false },
            onSave = { category, limit ->
                viewModel.addBudget(category, limit)
                showBudgetDialog = false
            }
        )
    }

    if (showGoalDialog) {
        AddGoalDialog(
            onDismiss = { showGoalDialog = false },
            onSave = { name, target, initial ->
                viewModel.addGoal(name, target, initial)
                showGoalDialog = false
            }
        )
    }

    if (showBillDialog) {
        AddBillDialog(
            onDismiss = { showBillDialog = false },
            onSave = { name, amount, dueMillis, recurring ->
                viewModel.addBill(name, amount, dueMillis, recurring)
                showBillDialog = false
            }
        )
    }

    goalToTopUp?.let { goal ->
        TopUpGoalDialog(
            goal = goal,
            onDismiss = { goalToTopUp = null },
            onSave = { amount ->
                viewModel.addGoalContribution(goal, amount)
                goalToTopUp = null
            }
        )
    }
}

@Composable
private fun BudgetsTab(
    budgets: List<Budget>,
    categorySpentMap: Map<String, Double>,
    onDelete: (Budget) -> Unit,
    modifier: Modifier = Modifier
) {
    if (budgets.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Belum ada anggaran untuk bulan ${FormatUtils.currentMonthLabel()}.\nTekan + untuk menambah batas pengeluaran.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Periode: ${FormatUtils.currentMonthLabel()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(budgets, key = { it.id }) { budget ->
            val spent = categorySpentMap[budget.category.trim().lowercase()] ?: 0.0
            val ratio = if (budget.limitAmount > 0) (spent / budget.limitAmount).toFloat() else 0f
            val progressColor = when {
                ratio >= 1f -> Color(0xFFEF4444)
                ratio >= 0.8f -> Color(0xFFF59E0B)
                else -> Color(0xFF10B981)
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            budget.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { onDelete(budget) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Terpakai ${FormatUtils.formatRupiah(spent)} / ${FormatUtils.formatRupiah(budget.limitAmount)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { ratio.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = progressColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val remaining = budget.limitAmount - spent
                    Text(
                        text = if (remaining >= 0) {
                            "Sisa ${FormatUtils.formatRupiah(remaining)}"
                        } else {
                            "Melebihi batas ${FormatUtils.formatRupiah(-remaining)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalsTab(
    goals: List<Goal>,
    onTopUp: (Goal) -> Unit,
    onDelete: (Goal) -> Unit,
    modifier: Modifier = Modifier
) {
    if (goals.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Belum ada target tabungan.\nTekan + untuk membuat target baru.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(goals, key = { it.id }) { goal ->
            val ratio = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat() else 0f
            val reached = goal.savedAmount >= goal.targetAmount
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            goal.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { onDelete(goal) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${FormatUtils.formatRupiah(goal.savedAmount)} / ${FormatUtils.formatRupiah(goal.targetAmount)} (${(ratio * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { ratio.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (reached) "Target tercapai!" else "Sisa ${FormatUtils.formatRupiah(goal.targetAmount - goal.savedAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (reached) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilledTonalButton(onClick = { onTopUp(goal) }) {
                            Text("+ Nabung")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BillsTab(
    bills: List<Bill>,
    onTogglePaid: (Bill) -> Unit,
    onDelete: (Bill) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bills.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Belum ada catatan tagihan.\nTekan + untuk menambah pengingat tagihan.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val now = System.currentTimeMillis()
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(bills, key = { it.id }) { bill ->
            val overdue = !bill.isPaid && bill.dueDate < now
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = bill.isPaid,
                        onCheckedChange = { onTogglePaid(bill) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            bill.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            FormatUtils.formatRupiah(bill.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = buildString {
                                append("Jatuh tempo: ${FormatUtils.formatDate(bill.dueDate)}")
                                if (bill.recurring) append(" • Rutin")
                                if (bill.isPaid) append(" • Lunas")
                                else if (overdue) append(" • Terlambat")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDelete(bill) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
