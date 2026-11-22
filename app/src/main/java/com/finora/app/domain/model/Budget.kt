package com.finora.app.domain.model

data class Budget(
    val id: String,
    val month: String,
    val category: String,
    val limitAmount: Double
)
