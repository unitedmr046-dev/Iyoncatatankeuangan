package com.finora.app.data.local.dao

import androidx.room.*
import com.finora.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE walletId = :walletId OR toWalletId = :walletId ORDER BY date DESC")
    fun getTransactionsByWallet(walletId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE walletId = :walletId OR toWalletId = :walletId")
    suspend fun deleteTransactionsByWallet(walletId: String)
}
