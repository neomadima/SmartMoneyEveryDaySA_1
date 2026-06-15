package com.example.smartmoneyeverydaysa_1.data.dao

import androidx.room.*
import com.example.smartmoneyeverydaysa_1.data.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    fun observeUserByEmail(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE idNumber = :idNumber LIMIT 1")
    suspend fun getUserByIdNumber(idNumber: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}
