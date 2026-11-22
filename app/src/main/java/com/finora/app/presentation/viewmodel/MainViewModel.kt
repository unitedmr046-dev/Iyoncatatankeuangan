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
import java.util.*
import javax.inject.Inject

data class MainUiState(
    val wallets: List<Wallet> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FinoraRepository
) : ViewModel() {

    private val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    val uiState: StateFlow<MainUiState> = combine(
        repository.getWallets(),
        repository.getTransactions(),
        repository.getBudgetsByMonth(currentMonth)
    ) { wallets, transactions, budgets ->
        val totalBalance = wallets.sumOf { wallet ->
            var balance = wallet.initialBalance
            transactions.forEach { tx ->
                if (tx.walletId == wallet.id) {
                    if (tx.type == "income") balance += tx.amount
                    else if (tx.type == "expense" || tx.type == "transfer") balance -= tx.amount
                }
                if (tx.type == "transfer" && tx.toWalletId == wallet.id) {
                    balance += tx.amount
                }
            }
            balance
        }
        
        val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }

        MainUiState(
            wallets = wallets,
            transactions = transactions,
            budgets = budgets,
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
        viewModelScope.launch {
            repository.insertWallet(
                Wallet(UUID.randomUUID().toString(), name, type, initialBalance)
            )
        }
    }

    fun addTransaction(
        type: String, amount: Double, walletId: String, 
        category: String, note: String
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    amount = amount,
                    walletId = walletId,
                    toWalletId = null,
                    category = category,
                    subcategory = null,
                    note = note,
                    date = System.currentTimeMillis(),
                    paymentMethod = "Tunai",
                    receiptUri = null,
                    recurring = false
                )
            )
        }
    }

    fun addBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.insertBudget(
                Budget(
                    id = UUID.randomUUID().toString(),
                    month = currentMonth,
                    category = category,
                    limitAmount = limit
                )
            )
        }
    }
}
