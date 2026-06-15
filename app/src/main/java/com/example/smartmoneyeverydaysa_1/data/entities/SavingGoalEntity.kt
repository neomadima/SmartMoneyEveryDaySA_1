package com.example.smartmoneyeverydaysa_1.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saving_goals")
data class SavingGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double
)
