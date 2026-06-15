package com.example.smartmoneyeverydaysa_1.data.dao

import androidx.room.*
import com.example.smartmoneyeverydaysa_1.data.entities.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE userId = :userId")
    fun getAccountsForUser(userId: Long): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, amount: Double)
}
