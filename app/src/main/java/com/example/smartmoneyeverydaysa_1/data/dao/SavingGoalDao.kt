package com.example.smartmoneyeverydaysa_1.data.dao

import androidx.room.*
import com.example.smartmoneyeverydaysa_1.data.entities.SavingGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingGoalDao {
    @Query("SELECT * FROM saving_goals WHERE userId = :userId")
    fun getGoalsForUser(userId: Long): Flow<List<SavingGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingGoalEntity)

    @Update
    suspend fun updateGoal(goal: SavingGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingGoalEntity)
}
