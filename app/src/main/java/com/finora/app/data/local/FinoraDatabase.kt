package com.finora.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.finora.app.data.local.dao.*
import com.finora.app.data.local.entity.*

@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        BillEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinoraDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun billDao(): BillDao
}
