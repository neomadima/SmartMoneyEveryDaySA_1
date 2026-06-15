package com.example.smartmoneyeverydaysa_1

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.databinding.FragmentStatementViewBinding
import java.util.Locale

class StatementViewFragment : Fragment() {

    private var _binding: FragmentStatementViewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatementViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val month = arguments?.getString("month") ?: "MAY 2024"
        val startDate = arguments?.getLong("startDate", -1L) ?: -1L
        val endDate = arguments?.getLong("endDate", -1L) ?: -1L

        binding.statementMonthHeader.text = month
        binding.toolbar.title = "Statement: $month"

        if (startDate != -1L && endDate != -1L) {
            renderFilteredTransactions(startDate, endDate)
        } else {
            // Default: show all for now or mock data
            viewModel.transactions.value?.let { renderTransactions(it) }
        }
    }

    private fun renderFilteredTransactions(start: Long, end: Long) {
        val filtered = viewModel.getFilteredTransactions(start, end)
        renderTransactions(filtered)
        
        // Update Summary based on filtered
        val credits = filtered.filter { it.type == "Income" }.sumOf { it.amount }
        val debits = filtered.filter { it.type == "Outgoing" }.sumOf { it.amount }
        
        binding.totalCreditsValue.text = "+ R ${String.format(Locale.getDefault(), "%,.2f", credits)}"
        binding.totalDebitsValue.text = "- R ${String.format(Locale.getDefault(), "%,.2f", debits)}"
    }

    private fun renderTransactions(transactions: List<Transaction>) {
        binding.transactionsListContainer.removeAllViews()
        transactions.forEach { txn ->
            val row = createTransactionRow(txn)
            binding.transactionsListContainer.addView(row)
            
            val divider = View(requireContext())
            divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            divider.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            divider.alpha = 0.2f
            binding.transactionsListContainer.addView(divider)
        }
    }

    private fun createTransactionRow(txn: Transaction): View {
        val container = LinearLayout(requireContext())
        container.orientation = LinearLayout.HORIZONTAL
        container.setPadding(0, 32, 0, 32)
        container.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        
        val outValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        container.setBackgroundResource(outValue.resourceId)
        container.isClickable = true
        container.isFocusable = true

        val detailsLayout = LinearLayout(requireContext())
        detailsLayout.orientation = LinearLayout.VERTICAL
        detailsLayout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val titleTv = TextView(requireContext())
        titleTv.text = txn.title
        titleTv.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
        titleTv.textStyle = android.graphics.Typeface.BOLD
        detailsLayout.addView(titleTv)

        val subTitleTv = TextView(requireContext())
        val photoIndicator = if (txn.photoUri != null) " 📷" else ""
        subTitleTv.text = "${txn.date} | ${txn.category}$photoIndicator"
        subTitleTv.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelSmall)
        subTitleTv.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        detailsLayout.addView(subTitleTv)

        container.addView(detailsLayout)

        val amountTv = TextView(requireContext())
        val prefix = if (txn.type == "Outgoing") "-" else "+"
        amountTv.text = "$prefix R ${String.format(Locale.getDefault(), "%,.2f", txn.amount)}"
        amountTv.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
        amountTv.textStyle = android.graphics.Typeface.BOLD
        if (txn.type == "Outgoing") {
            amountTv.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        } else {
            amountTv.setTextColor(resources.getColor(R.color.primary, null))
        }
        container.addView(amountTv)

        container.setOnClickListener {
            val bundle = Bundle().apply {
                putString("title", txn.title)
                val amountStr = if (txn.type == "Outgoing") "- R ${String.format(Locale.getDefault(), "%,.2f", txn.amount)}" 
                                else "+ R ${String.format(Locale.getDefault(), "%,.2f", txn.amount)}"
                putString("amount", amountStr)
                putString("date", txn.date)
                putString("photoUri", txn.photoUri)
            }
            findNavController().navigate(R.id.action_StatementViewFragment_to_TransactionDetailFragment, bundle)
        }

        return container
    }

    // Helper extension property
    private var TextView.textStyle: Int
        get() = typeface.style
        set(value) {
            setTypeface(typeface, value)
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}