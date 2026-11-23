package com.finora.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.app.data.local.FinoraPreferences
import com.finora.app.domain.model.*
import com.finora.app.domain.repository.FinoraRepository
import com.finora.app.utils.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private data class SecurityState(
    val isDarkMode: Boolean,
    val isPinEnabled: Boolean,
    val isBiometricEnabled: Boolean,
    val isAppLocked: Boolean
)

data class MainUiState(
    val wallets: List<Wallet> = emptyList(),
    val walletBalances: Map<String, Double> = emptyMap(),
    val transactions: List<Transaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val categorySpentMap: Map<String, Double> = emptyMap(),
    val goals: List<Goal> = emptyList(),
    val bills: List<Bill> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val isDarkMode: Boolean = false,
    val isPinEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isAppLocked: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FinoraRepository,
    private val preferences: FinoraPreferences
) : ViewModel() {

    private val currentMonth = FormatUtils.currentMonthKey()
    private val _isAppLocked = MutableStateFlow(preferences.hasPin())

    private val securityFlow = combine(
        preferences.isDarkMode,
        preferences.isPinEnabled,
        preferences.isBiometricEnabled,
        _isAppLocked
    ) { darkMode, pinEnabled, biometricEnabled, locked ->
        SecurityState(darkMode, pinEnabled, biometricEnabled, locked && pinEnabled)
    }

    private val dataFlow = combine(
        repository.getWallets(),
        repository.getTransactions(),
        repository.getBudgetsByMonth(currentMonth)
    ) { wallets, transactions, budgets ->
        Triple(wallets, transactions, budgets)
    }

    private val planningFlow = combine(
        repository.getGoals(),
        repository.getBills(),
        securityFlow
    ) { goals, bills, security ->
        Triple(goals, bills, security)
    }

    init {
        viewModelScope.launch {
            if (!preferences.hasSeededDefaultWallet()) {
                val existingWallets = repository.getWallets().first()
                if (existingWallets.isEmpty()) {
                    repository.insertWallet(
                        Wallet(
                            id = UUID.randomUUID().toString(),
                            name = "Dompet Tunai",
                            type = "cash",
                            initialBalance = 0.0
                        )
                    )
                }
                preferences.markDefaultWalletSeeded()
            }
        }
    }

    val uiState: StateFlow<MainUiState> = combine(
        dataFlow,
        planningFlow
    ) { (wallets, transactions, budgets), (goals, bills, security) ->
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

        val monthlyTx = transactions.filter {
            FormatUtils.monthKeyFromMillis(it.date) == currentMonth
        }
        val monthlyIncome = monthlyTx.filter { it.type == "income" }.sumOf { it.amount }
        val monthlyExpense = monthlyTx.filter { it.type == "expense" }.sumOf { it.amount }

        val categorySpentMap = monthlyTx
            .filter { it.type == "expense" }
            .groupBy { it.category.trim().lowercase() }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        MainUiState(
            wallets = wallets,
            walletBalances = walletBalances,
            transactions = transactions,
            budgets = budgets,
            categorySpentMap = categorySpentMap,
            goals = goals,
            bills = bills,
            totalBalance = totalBalance,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            monthlyIncome = monthlyIncome,
            monthlyExpense = monthlyExpense,
            isDarkMode = security.isDarkMode,
            isPinEnabled = security.isPinEnabled,
            isBiometricEnabled = security.isBiometricEnabled,
            isAppLocked = security.isAppLocked
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(
            isDarkMode = preferences.isDarkMode.value,
            isPinEnabled = preferences.hasPin(),
            isBiometricEnabled = preferences.isBiometricEnabled.value,
            isAppLocked = preferences.hasPin()
        )
    )

    // === KEAMANAN (PIN & BIOMETRIK) ===
    fun setupPin(pin: String) {
        preferences.setPin(pin)
        _isAppLocked.value = false
    }

    fun verifyPin(pin: String): Boolean = preferences.verifyPin(pin)

    fun unlockWithPin(pin: String): Boolean {
        val valid = preferences.verifyPin(pin)
        if (valid) _isAppLocked.value = false
        return valid
    }

    fun unlockWithBiometric() {
        _isAppLocked.value = false
    }

    fun lockAppIfEnabled() {
        if (preferences.hasPin()) {
            _isAppLocked.value = true
        }
    }

    fun disablePin(currentPin: String): Boolean {
        if (!preferences.verifyPin(currentPin)) return false
        preferences.clearPin()
        _isAppLocked.value = false
        return true
    }

    fun setBiometricEnabled(enabled: Boolean) {
        preferences.setBiometricEnabled(enabled)
    }

    fun toggleDarkMode(enabled: Boolean) {
        preferences.setDarkMode(enabled)
    }

    // === WALLETS ===
    fun addWallet(name: String, type: String, initialBalance: Double) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertWallet(
                Wallet(UUID.randomUUID().toString(), name.trim(), type, initialBalance)
            )
            preferences.markDefaultWalletSeeded()
        }
    }

    fun deleteWallet(wallet: Wallet) {
        viewModelScope.launch {
            repository.deleteTransactionsByWallet(wallet.id)
            repository.deleteWallet(wallet)
        }
    }

    // === TRANSACTIONS ===
    fun addTransaction(
        type: String,
        amount: Double,
        walletId: String,
        category: String,
        note: String,
        toWalletId: String? = null
    ) {
        if (amount <= 0.0 || walletId.isBlank()) return
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    amount = amount,
                    walletId = walletId,
                    toWalletId = if (type == "transfer") toWalletId else null,
                    category = if (type == "transfer") "Transfer" else category,
                    subcategory = null,
                    note = note.trim(),
                    date = System.currentTimeMillis(),
                    paymentMethod = null,
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

    // === BUDGETS ===
    fun addBudget(category: String, limit: Double) {
        if (category.isBlank() || limit <= 0.0) return
        viewModelScope.launch {
            val trimmed = category.trim()
            val existing = repository.getBudgetsByMonth(currentMonth).first().find {
                it.category.equals(trimmed, ignoreCase = true)
            }
            repository.insertBudget(
                Budget(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    month = currentMonth,
                    category = trimmed,
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

    // === GOALS ===
    fun addGoal(name: String, targetAmount: Double, initialSaved: Double = 0.0) {
        if (name.isBlank() || targetAmount <= 0.0) return
        viewModelScope.launch {
            repository.insertGoal(
                Goal(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    targetAmount = targetAmount,
                    savedAmount = initialSaved.coerceAtLeast(0.0),
                    deadline = null
                )
            )
        }
    }

    fun addGoalContribution(goal: Goal, amountToAdd: Double) {
        if (amountToAdd <= 0.0) return
        viewModelScope.launch {
            repository.updateGoal(
                goal.copy(savedAmount = (goal.savedAmount + amountToAdd).coerceAtLeast(0.0))
            )
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // === BILLS ===
    fun addBill(name: String, amount: Double, dueDateMillis: Long, recurring: Boolean) {
        if (name.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            repository.insertBill(
                Bill(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    amount = amount,
                    dueDate = dueDateMillis,
                    recurring = recurring,
                    isPaid = false
                )
            )
        }
    }

    fun toggleBillPaid(bill: Bill) {
        viewModelScope.launch {
            repository.updateBill(bill.copy(isPaid = !bill.isPaid))
        }
    }

    fun deleteBill(bill: Bill) {
        viewModelScope.launch {
            repository.deleteBill(bill)
        }
    }
}
