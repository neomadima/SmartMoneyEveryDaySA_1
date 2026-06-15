package com.example.smartmoneyeverydaysa_1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.smartmoneyeverydaysa_1.data.AppDatabase
import com.example.smartmoneyeverydaysa_1.data.entities.AccountEntity
import com.example.smartmoneyeverydaysa_1.data.entities.SavingGoalEntity
import com.example.smartmoneyeverydaysa_1.data.entities.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Account(
    val id: Long,
    val name: String,
    val balance: Double,
    val type: String,
    val accountNumber: String
)

data class SavingGoal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double
)

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val date: String,
    val category: String,
    val type: String, // "Income" or "Outgoing"
    val timestamp: Long,
    val photoUri: String? = null,
    val accountName: String = "Cheque Account"
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val isEarned: Boolean
)

/**
 * MainViewModel manages the business logic and data state for the application.
 * It interacts with the Room database and provides LiveData streams to the UI.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val _currentUserEmail = MutableStateFlow<String>("john.doe@example.com")
    val currentUserEmail: LiveData<String> = _currentUserEmail.asLiveData()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val accounts: LiveData<List<Account>> = _currentUserEmail.flatMapLatest { email ->
        db.accountDao().getAccountsForUser(1)
    }.asLiveData().map { entityList ->
        entityList.map { entity ->
            Account(
                id = entity.id,
                name = entity.accountName,
                balance = entity.balance,
                type = entity.accountType,
                accountNumber = entity.accountNumber
            )
        }
    }

    fun setCurrentUserEmail(email: String) {
        _currentUserEmail.value = email
    }

    /**
     * Observable list of saving goals, automatically refreshed from the database.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val goals: LiveData<List<SavingGoal>> = _currentUserEmail.flatMapLatest { email ->
        // Assuming userId 1 for default, in real app we search by email
        db.savingGoalDao().getGoalsForUser(1)
    }.asLiveData().map { entityList ->
        if (entityList.isEmpty()) {
             listOf(
                SavingGoal("1", "New Car Fund", 150000.0, 97500.0),
                SavingGoal("2", "Emergency Fund", 50000.0, 15000.0)
            )
        } else {
            entityList.map { entity ->
                SavingGoal(
                    id = entity.id.toString(),
                    name = entity.name,
                    targetAmount = entity.targetAmount,
                    savedAmount = entity.savedAmount
                )
            }
        }
    }

    /**
     * Observable list of recent transactions.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transactions: LiveData<List<Transaction>> = _currentUserEmail.flatMapLatest { email ->
        db.transactionDao().getRecentTransactions(100)
    }.asLiveData().map { entityList ->
        entityList.map { entity ->
            Transaction(
                id = entity.id.toString(),
                title = entity.description,
                amount = entity.amount,
                date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(entity.timestamp)),
                category = entity.category,
                type = if (entity.type == "Income") "Income" else "Outgoing",
                timestamp = entity.timestamp,
                photoUri = null,
                accountName = "Cheque Account"
            )
        }
    }

    /**
     * Computes the user's achievements (badges) based on their financial activity.
     */
    val badges: LiveData<List<Badge>> = transactions.map { txns ->
        val goalsList = goals.value.orEmpty()
        val spent = monthlySpent.value ?: 0.0
        val maxLimit = maxMonthlySpend.value ?: 0.0
        
        listOf(
            Badge(
                "1", 
                "Budget Master", 
                "Stayed under limit this month", 
                android.R.drawable.btn_star_big_on, 
                maxLimit > 0 && spent <= maxLimit && txns.isNotEmpty()
            ),
            Badge(
                "2", 
                "Super Saver", 
                "Reached a savings goal", 
                android.R.drawable.ic_menu_save, 
                goalsList.any { it.savedAmount >= it.targetAmount }
            ),
            Badge(
                "3", 
                "Big Spender", 
                "Logged more than 10 transactions", 
                android.R.drawable.ic_menu_agenda, 
                txns.size >= 10
            ),
            Badge(
                "4", 
                "Goal Getter", 
                "Created at least 2 savings goals", 
                android.R.drawable.ic_input_add, 
                goalsList.size >= 2
            )
        )
    }

    /**
     * Calculates the consecutive days the user has logged transactions.
     */
    val streakCount: LiveData<Int> = transactions.map { txns ->
        if (txns.isEmpty()) return@map 0
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = txns.map { dateFormat.format(Date(it.timestamp)) }.distinct().sortedDescending()
        
        var streak = 0
        val calendar = java.util.Calendar.getInstance()
        
        // Check if there's a transaction today or yesterday to start/continue streak
        val today = dateFormat.format(calendar.time)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterday = dateFormat.format(calendar.time)
        
        if (dates.first() != today && dates.first() != yesterday) return@map 0

        var currentCheckDate = dates.first()
        calendar.time = dateFormat.parse(currentCheckDate)!!
        
        for (date in dates) {
            if (date == dateFormat.format(calendar.time)) {
                streak++
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        streak
    }

    private val _minMonthlySpend = MutableLiveData<Double?>(null)
    val minMonthlySpend: LiveData<Double?> = _minMonthlySpend

    private val _maxMonthlySpend = MutableLiveData<Double?>(25000.0)
    val maxMonthlySpend: LiveData<Double?> = _maxMonthlySpend

    private val _dailyAtmWithdrawalLimit = MutableLiveData<Double?>(3000.0)
    val dailyAtmWithdrawalLimit: LiveData<Double?> = _dailyAtmWithdrawalLimit

    private val _dailyOnlinePurchaseLimit = MutableLiveData<Double?>(5000.0)
    val dailyOnlinePurchaseLimit: LiveData<Double?> = _dailyOnlinePurchaseLimit

    /**
     * Calculates today's outgoing expenditure.
     */
    val todaySpent: LiveData<Double> = transactions.map { txns ->
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        txns.filter { it.type == "Outgoing" && dateFormat.format(Date(it.timestamp)) == todayStr }
            .sumOf { it.amount }
    }

    /**
     * Calculates total outgoing expenditure for the current context.
     */
    val monthlySpent: LiveData<Double> = transactions.map { txns ->
        txns.filter { it.type == "Outgoing" }.sumOf { it.amount }
    }

    /**
     * Groups spending by category for visualization.
     */
    val categorySpending: LiveData<Map<String, Double>> = transactions.map { txns ->
        txns.filter { it.type == "Outgoing" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    /**
     * Generates a daily spending trend for the last 7 days.
     */
    val spendingTrend: LiveData<List<Pair<String, Double>>> = transactions.map { txns ->
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()
        val last7Days = mutableListOf<String>()
        
        for (i in 0..6) {
            val date = dateFormat.format(calendar.time)
            last7Days.add(0, date)
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }

        val dailyTotals = txns.filter { it.type == "Outgoing" }
            .groupBy { dateFormat.format(Date(it.timestamp)) }
            .mapValues { it.value.sumOf { txn -> txn.amount } }

        last7Days.map { date ->
            Pair(date, dailyTotals[date] ?: 0.0)
        }
    }

    /**
     * Estimates the total monthly spend based on current average daily spending.
     */
    val spendingProjection: LiveData<Double> = spendingTrend.map { trend ->
        val avgDaily = if (trend.isEmpty()) 0.0 else trend.map { it.second }.average()
        val calendar = java.util.Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        avgDaily * daysInMonth
    }

    /**
     * A composite score representing the user's overall financial health.
     */
    val financialHealthScore: LiveData<Int> = MediatorLiveData<Int>().apply {
        val updateScore = {
            val spent = monthlySpent.value ?: 0.0
            val max = maxMonthlySpend.value ?: 1.0
            val today = todaySpent.value ?: 0.0
            val dailyLimit = (dailyAtmWithdrawalLimit.value ?: 0.0) + (dailyOnlinePurchaseLimit.value ?: 0.0)
            val goalList = goals.value.orEmpty()
            val streak = streakCount.value ?: 0
            
            // 1. Budget Score (30 pts): Staying under monthly limit
            val budgetScore = ((1.0 - (spent / max)).coerceIn(0.0, 1.0) * 30).toInt()
            
            // 2. Daily Discipline (20 pts): Staying under daily limit
            val dailyScore = if (dailyLimit > 0) {
                ((1.0 - (today / dailyLimit)).coerceIn(0.0, 1.0) * 20).toInt()
            } else 20
            
            // 3. Savings Score (30 pts): Progress on goals
            val avgGoalProgress = if (goalList.isEmpty()) 0.0 else goalList.map { it.savedAmount / it.targetAmount }.average()
            val savingsScore = (avgGoalProgress.coerceIn(0.0, 1.0) * 30).toInt()
            
            // 4. Consistency Score (20 pts): Based on a 30-day target
            val consistencyScore = ((streak / 30.0).coerceAtMost(1.0) * 20).toInt()
            
            value = budgetScore + dailyScore + savingsScore + consistencyScore
        }
        
        addSource(monthlySpent) { updateScore() }
        addSource(todaySpent) { updateScore() }
        addSource(goals) { updateScore() }
        addSource(streakCount) { updateScore() }
        addSource(maxMonthlySpend) { updateScore() }
        addSource(dailyAtmWithdrawalLimit) { updateScore() }
        addSource(dailyOnlinePurchaseLimit) { updateScore() }
    }

    fun getCategorySpending(start: Long, end: Long): Map<String, Double> {
        return transactions.value.orEmpty()
            .filter { it.type == "Outgoing" && it.timestamp in start..end }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { txn -> txn.amount } }
    }

    fun setMonthlySpendingGoals(min: Double?, max: Double?) {
        _minMonthlySpend.value = min
        _maxMonthlySpend.value = max
    }

    fun setDailyLimits(atm: Double?, online: Double?) {
        _dailyAtmWithdrawalLimit.value = atm
        _dailyOnlinePurchaseLimit.value = online
    }

    /**
     * Persists a new account to the database.
     */
    fun addAccount(name: String, balance: Double, type: String) {
        viewModelScope.launch {
            db.accountDao().insertAccount(
                AccountEntity(
                    userId = 1,
                    accountName = name,
                    balance = balance,
                    accountType = type,
                    accountNumber = "**** " + (1000..9999).random().toString()
                )
            )
        }
    }

    /**
     * Persists a new saving goal to the database.
     */
    fun addGoal(name: String, target: Double, initial: Double) {
        viewModelScope.launch {
            db.savingGoalDao().insertGoal(
                SavingGoalEntity(
                    userId = 1, // Default user
                    name = name,
                    targetAmount = target,
                    savedAmount = initial
                )
            )
        }
    }

    /**
     * Updates an existing saving goal in the database.
     */
    fun updateGoal(id: Long, name: String, target: Double, initial: Double) {
        viewModelScope.launch {
            db.savingGoalDao().updateGoal(
                SavingGoalEntity(
                    id = id,
                    userId = 1,
                    name = name,
                    targetAmount = target,
                    savedAmount = initial
                )
            )
        }
    }

    /**
     * Adds a new transaction and updates the associated account balance.
     */
    fun addTransaction(
        title: String,
        amount: Double,
        date: String,
        category: String,
        type: String,
        photoUri: String? = null,
        accountName: String = "Cheque Account"
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                accountId = 1, // Default account
                type = if (type == "Income") "Income" else "Outgoing",
                amount = amount,
                category = category,
                description = title,
                timestamp = System.currentTimeMillis()
            )
            db.transactionDao().insertTransaction(transaction)
            db.accountDao().updateBalance(1, if (type == "Income") amount else -amount)
        }
    }

    fun getFilteredTransactions(start: Long, end: Long): List<Transaction> {
        return transactions.value.orEmpty().filter {
            it.timestamp in start..end
        }
    }
}
