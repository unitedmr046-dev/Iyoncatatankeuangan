package com.finora.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "cash", "bank", "ewallet", "credit"
    val initialBalance: Double
)
