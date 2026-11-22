package com.finora.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val month: String, // Format: "YYYY-MM"
    val category: String,
    val limitAmount: Double
)
