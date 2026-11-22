package com.finora.app.domain.model

data class Bill(
    val id: String,
    val name: String,
    val amount: Double,
    val dueDate: Long,
    val recurring: Boolean,
    val isPaid: Boolean
)
