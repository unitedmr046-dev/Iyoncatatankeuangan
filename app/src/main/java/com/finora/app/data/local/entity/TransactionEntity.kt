package com.finora.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String, // "income", "expense", "transfer"
    val amount: Double,
    val walletId: String,
    val toWalletId: String?, // Hanya untuk transfer
    val category: String,
    val subcategory: String?,
    val note: String,
    val date: Long, // Disimpan dalam bentuk Epoch Millis
    val paymentMethod: String?,
    val receiptUri: String?, // Path foto struk (jika ada)
    val recurring: Boolean
)
