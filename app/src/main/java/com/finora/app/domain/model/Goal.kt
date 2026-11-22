package com.finora.app.domain.model

data class Goal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val deadline: Long?
)
