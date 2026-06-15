package com.example.smartmoneyeverydaysa_1.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val type: String, // e.g., "Expense", "Income"
    val amount: Double,
    val category: String, // e.g., "Food", "Salary", "Transfer"
    val description: String,
    val timestamp: Long,
    val merchantName: String? = null
)
