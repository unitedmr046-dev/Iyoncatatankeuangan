package com.finora.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val deadline: Long? // Epoch millis, nullable
)
