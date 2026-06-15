package com.example.smartmoneyeverydaysa_1.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val idNumber: String,
    val phoneNumber: String,
    val email: String,
    val passwordHash: String,
    val memberSince: String
)
