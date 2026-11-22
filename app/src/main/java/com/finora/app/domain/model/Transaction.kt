package com.finora.app.domain.model

data class Transaction(
    val id: String,
    val type: String, // "income", "expense", "transfer"
    val amount: Double,
    val walletId: String,
    val toWalletId: String?,
    val category: String,
    val subcategory: String?,
    val note: String,
    val date: Long,
    val paymentMethod: String?,
    val receiptUri: String?,
    val recurring: Boolean
)
