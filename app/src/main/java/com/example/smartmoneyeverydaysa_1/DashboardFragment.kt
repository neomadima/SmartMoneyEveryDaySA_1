package com.example.smartmoneyeverydaysa_1

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.databinding.FragmentDashboardBinding
import com.google.android.material.R as MaterialR
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * DashboardFragment serves as the main landing screen of the application.
 * It provides an overview of:
 * - Current balance and activity streak.
 * - Spending analytics via a line graph and category breakdown.
 * - Progress towards savings goals.
 * - Recent transaction history.
 * - Gamified achievements (badges).
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private val financialTips = listOf(
        "Avoid 'lifestyle creep' by saving 50% of every salary increase.",
        "The best time to start saving was yesterday. The second best time is today.",
        "Track every cent. Small leaks can sink a big ship.",
        "Build an emergency fund of 3-6 months of expenses.",
        "Review your subscriptions. If you don't use it, cancel it!",
        "Pay yourself first: Automate your savings as soon as you get paid.",
        "Compare prices before you buy. A 5-minute search can save hundreds."
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Initializes the UI by observing ViewModel data and setting up interaction listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show a random tip every time they open the dashboard
        val randomTip = financialTips[Random.nextInt(financialTips.size)]
        binding.dailyTipText.text = randomTip

        // Navigation
        binding.totalBalanceText.setOnClickListener {
            val bundle = Bundle().apply {
                putString("accountName", "Cheque Account")
            }
            findNavController().navigate(R.id.action_DashboardFragment_to_AccountDetailFragment, bundle)
        }

        binding.streakChip.setOnClickListener {
            Toast.makeText(requireContext(), "Keep logging to increase your streak!", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddGoal.setOnClickListener {
            findNavController().navigate(R.id.action_DashboardFragment_to_AddGoalFragment)
        }

        binding.tipCard.setOnClickListener {
            val nextTip = financialTips[Random.nextInt(financialTips.size)]
            binding.dailyTipText.text = nextTip
        }

        // Observe and Render Goals
        viewModel.goals.observe(viewLifecycleOwner) { goals ->
            renderGoals(goals)
        }

        // Observe and Render Transactions
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            renderTransactions(transactions)
        }

        // Observe Monthly Spending Goals
        viewModel.monthlySpent.observe(viewLifecycleOwner) { spent ->
            updateMonthlySpendStatus(spent, viewModel.minMonthlySpend.value, viewModel.maxMonthlySpend.value)
        }
        
        // Observe Today's Spending
        viewModel.todaySpent.observe(viewLifecycleOwner) { spent ->
            val atm = viewModel.dailyAtmWithdrawalLimit.value ?: 0.0
            val online = viewModel.dailyOnlinePurchaseLimit.value ?: 0.0
            val dailyTotalLimit = atm + online
            
            if (dailyTotalLimit > 0) {
                binding.dailySpendStatusText.visibility = View.VISIBLE
                val percent = (spent / dailyTotalLimit * 100).toInt()
                binding.dailySpendStatusText.text = getString(R.string.daily_spend_status, spent, dailyTotalLimit, percent)
                
                if (spent > dailyTotalLimit) {
                    binding.dailySpendStatusText.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                } else {
                    binding.dailySpendStatusText.setTextColor(resources.getColor(android.R.color.darker_gray, null))
                }
            } else {
                binding.dailySpendStatusText.visibility = View.GONE
            }
        }
        viewModel.minMonthlySpend.observe(viewLifecycleOwner) { min ->
            binding.spendingLineGraph.setGoals(min, viewModel.maxMonthlySpend.value)
            updateMonthlySpendStatus(viewModel.monthlySpent.value ?: 0.0, min, viewModel.maxMonthlySpend.value)
        }
        viewModel.maxMonthlySpend.observe(viewLifecycleOwner) { max ->
            binding.spendingLineGraph.setGoals(viewModel.minMonthlySpend.value, max)
            updateMonthlySpendStatus(viewModel.monthlySpent.value ?: 0.0, viewModel.minMonthlySpend.value, max)
        }

        // Observe Category Spending for the Graph
        viewModel.categorySpending.observe(viewLifecycleOwner) { spendingMap ->
            renderCategorySpending(spendingMap)
        }

        viewModel.spendingTrend.observe(viewLifecycleOwner) { trend ->
            // Format labels from "yyyy-MM-dd" to "dd" (just the day)
            val labels = trend.map { it.first.split("-").last() }
            binding.spendingLineGraph.setData(trend.map { it.second }, labels)
        }

        // Observe Badges
        viewModel.badges.observe(viewLifecycleOwner) { badges ->
            renderBadges(badges)
        }

        // Observe Financial Health Score
        viewModel.financialHealthScore.observe(viewLifecycleOwner) { score ->
            binding.healthScoreText.text = score.toString()
            binding.healthScoreProgress.progress = score
            val color = when {
                score >= 80 -> R.color.income_green
                score >= 50 -> R.color.primary
                else -> android.R.color.holo_red_dark
            }
            binding.healthScoreProgress.setIndicatorColor(resources.getColor(color, null))
        }

        // Observe Spending Projection
        viewModel.spendingProjection.observe(viewLifecycleOwner) { projection ->
            val max = viewModel.maxMonthlySpend.value ?: 0.0
            if (max > 0 && projection > max) {
                binding.projectionText.visibility = View.VISIBLE
                binding.projectionText.text = getString(R.string.spending_projection, projection)
            } else {
                binding.projectionText.visibility = View.GONE
            }
        }

        // Observe Streak
        viewModel.streakCount.observe(viewLifecycleOwner) { count ->
            binding.streakChip.text = "$count Day Streak! 🔥"
            binding.streakChip.visibility = if (count > 0) View.VISIBLE else View.GONE
        }

        binding.btnSelectPeriod.setOnClickListener {
            showGraphDatePicker()
        }
    }

    /**
     * Displays a date range picker to filter the category spending graph.
     */
    private fun showGraphDatePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Graph Period")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val start = selection.first
            val end = selection.second
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            binding.btnSelectPeriod.text = "${sdf.format(Date(start))} - ${sdf.format(Date(end))} ▾"
            
            val filteredData = viewModel.getCategorySpending(start, end)
            renderCategorySpending(filteredData)
        }
        dateRangePicker.show(parentFragmentManager, "graph_date_picker")
    }

    /**
     * Dynamically renders the spending breakdown by category using progress bars.
     * Also includes a performance comparison against set monthly goals.
     */
    private fun renderCategorySpending(spendingMap: Map<String, Double>) {
        binding.categoryGraphContainer.removeAllViews()
        val totalSpent = spendingMap.values.sum()
        
        // Add Total vs Goals Bar first
        val min = viewModel.minMonthlySpend.value
        val max = viewModel.maxMonthlySpend.value
        if (min != null || max != null) {
            val goalsView = createGoalsComparisonView(totalSpent, min, max)
            binding.categoryGraphContainer.addView(goalsView)
            
            val divider = View(requireContext())
            val dividerHeight = (1 * resources.displayMetrics.density).toInt().coerceAtLeast(1)
            val margin = (16 * resources.displayMetrics.density).toInt()
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dividerHeight)
            lp.setMargins(0, margin, 0, margin)
            divider.layoutParams = lp
            divider.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            divider.alpha = 0.3f
            binding.categoryGraphContainer.addView(divider)
        }

        if (totalSpent == 0.0) return

        spendingMap.toList().sortedByDescending { it.second }.forEach { (category, amount) ->
            val percentage = (amount / totalSpent * 100).toInt()
            
            val row = LinearLayout(requireContext())
            row.orientation = LinearLayout.VERTICAL
            row.setPadding(0, 8, 0, 8)

            val labelLayout = RelativeLayout(requireContext())
            val labelTv = TextView(requireContext())
            labelTv.text = category
            labelTv.setTextAppearance(MaterialR.style.TextAppearance_Material3_LabelMedium)
            
            val amountTv = TextView(requireContext())
            amountTv.text = "R ${String.format(Locale.getDefault(), "%,.0f", amount)} ($percentage%)"
            amountTv.setTextAppearance(MaterialR.style.TextAppearance_Material3_LabelSmall)
            amountTv.setTextColor(resources.getColor(R.color.primary, null))
            
            val lpAmount = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            lpAmount.addRule(RelativeLayout.ALIGN_PARENT_END)
            amountTv.layoutParams = lpAmount
            
            labelLayout.addView(labelTv)
            labelLayout.addView(amountTv)
            row.addView(labelLayout)

            val bar = LinearProgressIndicator(requireContext())
            bar.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20)
            (bar.layoutParams as LinearLayout.LayoutParams).topMargin = 8
            bar.progress = percentage
            bar.trackCornerRadius = 10
            bar.trackThickness = 20
            row.addView(bar)

            binding.categoryGraphContainer.addView(row)
        }
    }

    /**
     * Creates a visual comparison view showing total spending vs min/max budget goals.
     */
    private fun createGoalsComparisonView(totalSpent: Double, min: Double?, max: Double?): View {
        val container = LinearLayout(requireContext())
        container.orientation = LinearLayout.VERTICAL
        
        val titleTv = TextView(requireContext())
        titleTv.text = "Budget Performance (Total vs Goals)"
        titleTv.setTextAppearance(MaterialR.style.TextAppearance_Material3_LabelSmall)
        titleTv.setTextColor(resources.getColor(R.color.secondary, null))
        container.addView(titleTv)

        val bar = LinearProgressIndicator(requireContext())
        bar.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 40)
        (bar.layoutParams as LinearLayout.LayoutParams).topMargin = 12
        
        val maxValueForBar = max ?: (min?.let { it * 1.5 } ?: (totalSpent * 1.2))
        val progress = ((totalSpent / maxValueForBar) * 100).toInt()
        
        bar.progress = progress.coerceAtMost(100)
        bar.trackCornerRadius = 20
        bar.trackThickness = 40
        
        // Color coding
        if (max != null && totalSpent > max) {
            bar.setIndicatorColor(resources.getColor(android.R.color.holo_red_dark, null))
        } else if (min != null && totalSpent >= min) {
            bar.setIndicatorColor(resources.getColor(R.color.income_green, null))
        } else {
            bar.setIndicatorColor(resources.getColor(R.color.primary, null))
        }
        
        container.addView(bar)
        
        val markersLayout = RelativeLayout(requireContext())
        markersLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        
        if (min != null) {
            val minTv = TextView(requireContext())
            minTv.text = "Min: R${min.toInt()}"
            minTv.setTextAppearance(MaterialR.style.TextAppearance_Material3_LabelSmall)
            markersLayout.addView(minTv)
        }
        
        if (max != null) {
            val maxTv = TextView(requireContext())
            maxTv.text = "Max: R${max.toInt()}"
            maxTv.setTextAppearance(MaterialR.style.TextAppearance_Material3_LabelSmall)
            val lp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            lp.addRule(RelativeLayout.ALIGN_PARENT_END)
            maxTv.layoutParams = lp
            markersLayout.addView(maxTv)
        }
        
        container.addView(markersLayout)
        return container
    }

    /**
     * Renders the horizontal list of achievement badges.
     * Earned badges are highlighted, while locked ones appear grayscale.
     */
    private fun renderBadges(badges: List<Badge>) {
        binding.badgesContainer.removeAllViews()
        val density = resources.displayMetrics.density
        badges.forEach { badge ->
            val badgeLayout = LinearLayout(requireContext())
            badgeLayout.orientation = LinearLayout.VERTICAL
            badgeLayout.gravity = android.view.Gravity.CENTER
            val horizontalPadding = (12 * density).toInt()
            val verticalPadding = (8 * density).toInt()
            badgeLayout.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            
            // Container for the icon with background
            val iconContainer = RelativeLayout(requireContext())
            val bgSize = (56 * density).toInt()
            
            val iconBg = View(requireContext())
            iconBg.layoutParams = RelativeLayout.LayoutParams(bgSize, bgSize)
            iconBg.setBackgroundResource(R.drawable.circle_background_tint)
            if (!badge.isEarned) {
                iconBg.alpha = 0.2f
            } else {
                iconBg.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    resources.getColor(R.color.primaryContainer, null)
                )
            }
            iconContainer.addView(iconBg)

            val icon = android.widget.ImageView(requireContext())
            val lpIcon = RelativeLayout.LayoutParams((32 * density).toInt(), (32 * density).toInt())
            lpIcon.addRule(RelativeLayout.CENTER_IN_PARENT)
            icon.layoutParams = lpIcon
            icon.setImageResource(badge.iconRes)
            
            if (badge.isEarned) {
                icon.setColorFilter(resources.getColor(R.color.primary, null))
                badgeLayout.alpha = 1.0f
            } else {
                icon.setColorFilter(Color.GRAY)
                badgeLayout.alpha = 0.5f
            }
            iconContainer.addView(icon)
            
            badgeLayout.addView(iconContainer)

            val titleTv = TextView(requireContext())
            titleTv.text = badge.title
            titleTv.setTextAppearance(MaterialR.style.TextAppearance_Material3_LabelSmall)
            titleTv.gravity = android.view.Gravity.CENTER
            titleTv.setPadding(0, 8, 0, 0)
            badgeLayout.addView(titleTv)

            badgeLayout.setOnClickListener {
                val status = if (badge.isEarned) "Earned! ✅" else "Locked 🔒"
                Toast.makeText(requireContext(), "$status\n${badge.title}: ${badge.description}", Toast.LENGTH_SHORT).show()
            }
            binding.badgesContainer.addView(badgeLayout)
        }
    }

    /**
     * Updates the text status and color coding for monthly spending limits.
     */
    private fun updateMonthlySpendStatus(spent: Double, min: Double?, max: Double?) {
        val spentStr = "R ${String.format(Locale.getDefault(), "%,.2f", spent)}"
        
        if (min == null && max == null) {
            binding.monthlySpendStatusText.visibility = View.GONE
            return
        }
        
        binding.monthlySpendStatusText.visibility = View.VISIBLE
        
        val statusText = StringBuilder()
        if (max != null && max > 0) {
            if (spent > max) {
                binding.monthlySpendStatusText.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                statusText.append(getString(R.string.monthly_spend_status_over_limit, spentStr, max))
            } else {
                binding.monthlySpendStatusText.setTextColor(resources.getColor(R.color.primary, null))
                statusText.append(getString(R.string.monthly_spend_status_spent, spentStr, max))
            }
        } else {
            binding.monthlySpendStatusText.setTextColor(resources.getColor(R.color.primary, null))
            statusText.append(getString(R.string.monthly_spend_status_no_limit, spentStr))
        }

        if (min != null) {
            if (spent < min) {
                statusText.append(getString(R.string.monthly_spend_status_goal_min, min))
            } else {
                statusText.append(getString(R.string.monthly_spend_status_goal_reached))
            }
        }
        
        binding.monthlySpendStatusText.text = statusText.toString()
    }

    /**
     * Populates the transactions container with a list of recent activities.
     */
    private fun renderTransactions(transactions: List<Transaction>) {
        binding.transactionsContainer.removeAllViews()
        // Removed the .take(5) limit to show ALL expenses/transactions as requested
        transactions.forEachIndexed { index, txn ->
            val txnView = createTransactionView(txn)
            binding.transactionsContainer.addView(txnView)

            if (index < transactions.size - 1) {
                val divider = View(requireContext())
                val dividerHeight = (1 * resources.displayMetrics.density).toInt().coerceAtLeast(1)
                divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dividerHeight)
                divider.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
                divider.alpha = 0.3f
                binding.transactionsContainer.addView(divider)
            }
        }
    }

    /**
     * Generates a view for a single transaction item, including click navigation to details.
     */
    private fun createTransactionView(txn: Transaction): View {
        val container = LinearLayout(requireContext())
        container.orientation = LinearLayout.VERTICAL
        container.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val padding = (16 * resources.displayMetrics.density).toInt()
        container.setPadding(padding, padding, padding, padding)
        container.isClickable = true
        container.isFocusable = true
        val outValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        container.setBackgroundResource(outValue.resourceId)

        val titleTv = TextView(requireContext())
        val amountStr = if (txn.type == "Outgoing") "- R ${String.format(Locale.getDefault(), "%,.2f", txn.amount)}" 
                        else "+ R ${String.format(Locale.getDefault(), "%,.2f", txn.amount)}"
        titleTv.text = "${txn.title} - $amountStr"
        titleTv.setTextAppearance(MaterialR.style.TextAppearance_Material3_BodyLarge)
        container.addView(titleTv)

        val dateTv = TextView(requireContext())
        dateTv.text = txn.date
        dateTv.setTextAppearance(MaterialR.style.TextAppearance_Material3_BodySmall)
        dateTv.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        container.addView(dateTv)

        container.setOnClickListener {
            val bundle = Bundle().apply {
                putString("title", txn.title)
                putString("amount", amountStr)
                putString("date", txn.date)
                putString("photoUri", txn.photoUri)
            }
            findNavController().navigate(R.id.action_DashboardFragment_to_TransactionDetailFragment, bundle)
        }

        return container
    }

    /**
     * Renders the list of active savings goals with progress indicators.
     */
    private fun renderGoals(goals: List<SavingGoal>) {
        binding.goalsContainer.removeAllViews()
        goals.forEachIndexed { index, goal ->
            val goalView = createGoalView(goal)
            binding.goalsContainer.addView(goalView)

            if (index < goals.size - 1) {
                val divider = View(requireContext())
                val dividerHeight = (2 * resources.displayMetrics.density).toInt().coerceAtLeast(1)
                val margin = (16 * resources.displayMetrics.density).toInt()
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dividerHeight)
                params.setMargins(0, margin, 0, margin)
                divider.layoutParams = params
                divider.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
                divider.alpha = 0.3f
                binding.goalsContainer.addView(divider)
            }
        }
    }

    /**
     * Creates a custom progress view for an individual saving goal.
     * Includes dynamic coloring based on how close the user is to completion.
     */
    private fun createGoalView(goal: SavingGoal): View {
        val container = LinearLayout(requireContext())
        container.orientation = LinearLayout.VERTICAL
        container.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        container.isClickable = true
        container.isFocusable = true
        container.setPadding(0, 12, 0, 12)
        
        val percentage = ((goal.savedAmount / goal.targetAmount) * 100).toInt()

        val headerLayout = RelativeLayout(requireContext())
        
        val titleTv = TextView(requireContext())
        titleTv.text = goal.name
        titleTv.setTextAppearance(MaterialR.style.TextAppearance_Material3_LabelLarge)
        titleTv.setTextColor(resources.getColor(android.R.color.white, null))
        headerLayout.addView(titleTv)

        val targetTv = TextView(requireContext())
        targetTv.text = "Goal: R ${String.format(Locale.getDefault(), "%,.0f", goal.targetAmount)}"
        targetTv.setTextAppearance(MaterialR.style.TextAppearance_Material3_LabelSmall)
        targetTv.setTextColor(resources.getColor(android.R.color.white, null))
        val lpTarget = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        lpTarget.addRule(RelativeLayout.ALIGN_PARENT_END)
        targetTv.layoutParams = lpTarget
        headerLayout.addView(targetTv)
        
        container.addView(headerLayout)

        val progress = LinearProgressIndicator(requireContext())
        val progressParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        progressParams.setMargins(0, 12, 0, 12)
        progress.layoutParams = progressParams
        progress.progress = percentage.coerceAtMost(100)
        progress.trackCornerRadius = 16
        progress.trackThickness = 32
        
        // Dynamic coloring based on progress
        when {
            percentage >= 100 -> progress.setIndicatorColor(resources.getColor(R.color.income_green, null))
            percentage >= 75 -> progress.setIndicatorColor(resources.getColor(R.color.primary, null))
            percentage >= 25 -> progress.setIndicatorColor(resources.getColor(R.color.secondary, null))
            else -> progress.setIndicatorColor(resources.getColor(android.R.color.holo_orange_light, null))
        }
        
        container.addView(progress)

        val footerLayout = RelativeLayout(requireContext())

        val savedTv = TextView(requireContext())
        savedTv.text = "R ${String.format(Locale.getDefault(), "%,.0f", goal.savedAmount)} saved"
        savedTv.setTextAppearance(MaterialR.style.TextAppearance_Material3_BodySmall)
        savedTv.setTextColor(resources.getColor(android.R.color.white, null))
        footerLayout.addView(savedTv)

        val percentTv = TextView(requireContext())
        percentTv.text = "$percentage%"
        percentTv.setTextAppearance(MaterialR.style.TextAppearance_Material3_BodySmall)
        percentTv.setTextColor(resources.getColor(android.R.color.white, null))
        percentTv.setTypeface(null, android.graphics.Typeface.BOLD)
        val lpPercent = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        lpPercent.addRule(RelativeLayout.ALIGN_PARENT_END)
        percentTv.layoutParams = lpPercent
        footerLayout.addView(percentTv)
        
        container.addView(footerLayout)

        container.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("goalId", goal.id.toLong())
                putString("name", goal.name)
                putDouble("target", goal.targetAmount)
                putDouble("saved", goal.savedAmount)
            }
            findNavController().navigate(R.id.action_DashboardFragment_to_AddGoalFragment, bundle)
        }

        return container
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}