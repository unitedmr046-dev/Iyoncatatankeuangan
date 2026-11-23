package com.finora.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.app.domain.model.Budget
import com.finora.app.domain.model.Transaction
import com.finora.app.domain.model.Wallet
import com.finora.app.domain.repository.FinoraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class MainUiState(
    val wallets: List<Wallet> = emptyList(),
    val walletBalances: Map<String, Double> = emptyMap(),
    val transactions: List<Transaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val spentByCategory: Map<String, Double> = emptyMap(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FinoraRepository
) : ViewModel() {

    private val currentMonth = SimpleDateFormat("yyyy-MM", Locale.ROOT).format(Date())

    val uiState: StateFlow<MainUiState> = combine(
        repository.getWallets(),
        repository.getTransactions(),
        repository.getBudgetsByMonth(currentMonth)
    ) { wallets, transactions, budgets ->
        val walletBalances = wallets.associate { wallet ->
            var balance = wallet.initialBalance
            transactions.forEach { tx ->
                if (tx.walletId == wallet.id) {
                    when (tx.type) {
                        "income" -> balance += tx.amount
                        "expense", "transfer" -> balance -= tx.amount
                    }
                }
                if (tx.type == "transfer" && tx.toWalletId == wallet.id) {
                    balance += tx.amount
                }
            }
            wallet.id to balance
        }

        val totalBalance = walletBalances.values.sum()
        val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }

        val monthFormatter = SimpleDateFormat("yyyy-MM", Locale.ROOT)
        val spentByCategory = transactions
            .filter { tx ->
                tx.type == "expense" && monthFormatter.format(Date(tx.date)) == currentMonth
            }
            .groupBy { it.category.trim().lowercase(Locale.ROOT) }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        MainUiState(
            wallets = wallets,
            walletBalances = walletBalances,
            transactions = transactions,
            budgets = budgets,
            spentByCategory = spentByCategory,
            totalBalance = totalBalance,
            totalIncome = totalIncome,
            totalExpense = totalExpense
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun addWallet(name: String, type: String, initialBalance: Double) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertWallet(
                Wallet(UUID.randomUUID().toString(), name.trim(), type, initialBalance)
            )
        }
    }

    fun deleteWallet(wallet: Wallet) {
        viewModelScope.launch {
            repository.deleteWallet(wallet)
        }
    }

    fun addTransaction(
        type: String,
        amount: Double,
        walletId: String,
        category: String,
        note: String,
        toWalletId: String? = null
    ) {
        if (amount <= 0.0 || walletId.isBlank()) return
        if (type == "transfer" && (toWalletId.isNullOrBlank() || toWalletId == walletId)) return

        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    amount = amount,
                    walletId = walletId,
                    toWalletId = if (type == "transfer") toWalletId else null,
                    category = if (type == "transfer") "Transfer" else category.trim(),
                    subcategory = null,
                    note = note.trim(),
                    date = System.currentTimeMillis(),
                    paymentMethod = "Tunai",
                    receiptUri = null,
                    recurring = false
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addBudget(category: String, limit: Double) {
        val cleanCategory = category.trim()
        if (cleanCategory.isBlank() || limit <= 0.0) return

        viewModelScope.launch {
            val existing = uiState.value.budgets.find {
                it.category.equals(cleanCategory, ignoreCase = true)
            }
            repository.insertBudget(
                Budget(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    month = currentMonth,
                    category = cleanCategory,
                    limitAmount = limit
                )
            )
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }
}
