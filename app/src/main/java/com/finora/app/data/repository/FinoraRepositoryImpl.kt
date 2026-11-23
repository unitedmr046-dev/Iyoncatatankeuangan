package com.finora.app.data.repository

import com.finora.app.data.local.FinoraDatabase
import com.finora.app.data.local.entity.*
import com.finora.app.domain.model.*
import com.finora.app.domain.repository.FinoraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FinoraRepositoryImpl @Inject constructor(
    private val db: FinoraDatabase
) : FinoraRepository {

    // === WALLETS ===
    override fun getWallets(): Flow<List<Wallet>> =
        db.walletDao().getAllWallets().map { entities ->
            entities.map { Wallet(it.id, it.name, it.type, it.initialBalance) }
        }

    override suspend fun insertWallet(wallet: Wallet) =
        db.walletDao().insertWallet(WalletEntity(wallet.id, wallet.name, wallet.type, wallet.initialBalance))

    override suspend fun deleteWallet(wallet: Wallet) =
        db.walletDao().deleteWallet(WalletEntity(wallet.id, wallet.name, wallet.type, wallet.initialBalance))

    // === TRANSACTIONS ===
    override fun getTransactions(): Flow<List<Transaction>> =
        db.transactionDao().getAllTransactions().map { entities ->
            entities.map {
                Transaction(
                    it.id, it.type, it.amount, it.walletId, it.toWalletId,
                    it.category, it.subcategory, it.note, it.date,
                    it.paymentMethod, it.receiptUri, it.recurring
                )
            }
        }

    override fun getTransactionsByWallet(walletId: String): Flow<List<Transaction>> =
        db.transactionDao().getTransactionsByWallet(walletId).map { entities ->
            entities.map {
                Transaction(
                    it.id, it.type, it.amount, it.walletId, it.toWalletId,
                    it.category, it.subcategory, it.note, it.date,
                    it.paymentMethod, it.receiptUri, it.recurring
                )
            }
        }

    override suspend fun insertTransaction(transaction: Transaction) =
        db.transactionDao().insertTransaction(transaction.toEntity())

    override suspend fun updateTransaction(transaction: Transaction) =
        db.transactionDao().updateTransaction(transaction.toEntity())

    override suspend fun deleteTransaction(transaction: Transaction) =
        db.transactionDao().deleteTransaction(transaction.toEntity())

    override suspend fun deleteTransactionsByWallet(walletId: String) =
        db.transactionDao().deleteTransactionsByWallet(walletId)

    // === BUDGETS ===
    override fun getBudgetsByMonth(month: String): Flow<List<Budget>> =
        db.budgetDao().getBudgetsByMonth(month).map { entities ->
            entities.map { Budget(it.id, it.month, it.category, it.limitAmount) }
        }

    override suspend fun insertBudget(budget: Budget) =
        db.budgetDao().insertBudget(BudgetEntity(budget.id, budget.month, budget.category, budget.limitAmount))

    override suspend fun deleteBudget(budget: Budget) =
        db.budgetDao().deleteBudget(BudgetEntity(budget.id, budget.month, budget.category, budget.limitAmount))

    // === GOALS ===
    override fun getGoals(): Flow<List<Goal>> =
        db.goalDao().getAllGoals().map { entities ->
            entities.map { Goal(it.id, it.name, it.targetAmount, it.savedAmount, it.deadline) }
        }

    override suspend fun insertGoal(goal: Goal) =
        db.goalDao().insertGoal(GoalEntity(goal.id, goal.name, goal.targetAmount, goal.savedAmount, goal.deadline))

    override suspend fun updateGoal(goal: Goal) =
        db.goalDao().updateGoal(GoalEntity(goal.id, goal.name, goal.targetAmount, goal.savedAmount, goal.deadline))

    override suspend fun deleteGoal(goal: Goal) =
        db.goalDao().deleteGoal(GoalEntity(goal.id, goal.name, goal.targetAmount, goal.savedAmount, goal.deadline))

    // === BILLS ===
    override fun getBills(): Flow<List<Bill>> =
        db.billDao().getAllBills().map { entities ->
            entities.map { Bill(it.id, it.name, it.amount, it.dueDate, it.recurring, it.isPaid) }
        }

    override suspend fun insertBill(bill: Bill) =
        db.billDao().insertBill(BillEntity(bill.id, bill.name, bill.amount, bill.dueDate, bill.recurring, bill.isPaid))

    override suspend fun updateBill(bill: Bill) =
        db.billDao().updateBill(BillEntity(bill.id, bill.name, bill.amount, bill.dueDate, bill.recurring, bill.isPaid))

    override suspend fun deleteBill(bill: Bill) =
        db.billDao().deleteBill(BillEntity(bill.id, bill.name, bill.amount, bill.dueDate, bill.recurring, bill.isPaid))

    private fun Transaction.toEntity() = TransactionEntity(
        id, type, amount, walletId, toWalletId, category, subcategory,
        note, date, paymentMethod, receiptUri, recurring
    )
}
