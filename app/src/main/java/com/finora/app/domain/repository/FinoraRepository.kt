package com.finora.app.domain.repository

import com.finora.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface FinoraRepository {
    // Wallets
    fun getWallets(): Flow<List<Wallet>>
    suspend fun insertWallet(wallet: Wallet)
    suspend fun deleteWallet(wallet: Wallet)

    // Transactions
    fun getTransactions(): Flow<List<Transaction>>
    fun getTransactionsByWallet(walletId: String): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)

    // Budgets
    fun getBudgetsByMonth(month: String): Flow<List<Budget>>
    suspend fun insertBudget(budget: Budget)
    suspend fun deleteBudget(budget: Budget)

    // Goals
    fun getGoals(): Flow<List<Goal>>
    suspend fun insertGoal(goal: Goal)
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(goal: Goal)

    // Bills
    fun getBills(): Flow<List<Bill>>
    suspend fun insertBill(bill: Bill)
    suspend fun updateBill(bill: Bill)
    suspend fun deleteBill(bill: Bill)
}
