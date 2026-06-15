package com.example.smartmoneyeverydaysa_1.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smartmoneyeverydaysa_1.data.dao.AccountDao
import com.example.smartmoneyeverydaysa_1.data.dao.SavingGoalDao
import com.example.smartmoneyeverydaysa_1.data.dao.TransactionDao
import com.example.smartmoneyeverydaysa_1.data.dao.UserDao
import com.example.smartmoneyeverydaysa_1.data.entities.AccountEntity
import com.example.smartmoneyeverydaysa_1.data.entities.SavingGoalEntity
import com.example.smartmoneyeverydaysa_1.data.entities.TransactionEntity
import com.example.smartmoneyeverydaysa_1.data.entities.UserEntity

@Database(
    entities = [UserEntity::class, AccountEntity::class, TransactionEntity::class, SavingGoalEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingGoalDao(): SavingGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_money_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
