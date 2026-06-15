package com.example.smartmoneyeverydaysa_1.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val accountName: String, // e.g., "Smart Saver", "Everyday Banking"
    val accountNumber: String,
    val balance: Double,
    val accountType: String // e.g., "Savings", "Current"
)
