package com.finora.app.domain.model

data class Wallet(
    val id: String,
    val name: String,
    val type: String,
    val initialBalance: Double
)
