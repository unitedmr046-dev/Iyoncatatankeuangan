package com.finora.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Double,
    val dueDate: Long, // Epoch millis
    val recurring: Boolean,
    val isPaid: Boolean
)
