package com.example.smartmoneyeverydaysa_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.databinding.FragmentAccountDetailBinding
import java.util.Locale

class AccountDetailFragment : Fragment() {

    private var _binding: FragmentAccountDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val accountName = arguments?.getString("accountName") ?: "Account"
        binding.accountNameDetail.text = accountName

        setupFilters()
        
        // Setup Account Balance observation
        viewModel.transactions.observe(viewLifecycleOwner) { txns ->
            val initialBalance = when (accountName) {
                "Cheque Account" -> 12450.00
                "Smart Saver" -> 32800.50
                "Visa Credit Card" -> 428.40
                else -> 0.0
            }
            
            val accountTxns = txns.filter { it.accountName == accountName }
            val netChange = accountTxns.sumOf { 
                if (it.type == "Income") it.amount else -it.amount 
            }
            
            // Only adjust mock balance for new transactions added during this session
            // (In a real app, the DB would handle the source of truth)
            val currentBalance = initialBalance + accountTxns.filter { it.timestamp > 1716595200000L }.sumOf {
                 if (it.type == "Income") it.amount else -it.amount 
            }

            binding.accountBalanceDetail.text = String.format(Locale.getDefault(), "R %,.2f", initialBalance + netChange)
            updateUI()
        }
    }

    private fun setupFilters() {
        val types = arrayOf("All", "Income", "Outgoing")
        binding.typeFilterDropdown.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types))
        binding.typeFilterDropdown.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ -> updateUI() }

        val categories = arrayOf("All Categories", "Groceries", "Transport", "Utilities", "Food", "Entertainment", "Shopping")
        binding.categoryFilterDropdown.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories))
        binding.categoryFilterDropdown.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ -> updateUI() }
    }

    private fun updateUI() {
        val selectedType = binding.typeFilterDropdown.text.toString()
        val selectedCategory = binding.categoryFilterDropdown.text.toString()
        val accountName = binding.accountNameDetail.text.toString()
        val allTransactions = viewModel.transactions.value.orEmpty()

        val filtered = allTransactions.filter { txn ->
            (txn.accountName == accountName) &&
            (selectedType == "All" || txn.type == selectedType) &&
            (selectedCategory == "All Categories" || txn.category == selectedCategory)
        }

        // Calculate Totals
        val categoryTotal = filtered.filter { it.type == "Outgoing" }.sumOf { it.amount }
        val grandTotalAll = allTransactions.filter { it.type == "Outgoing" }.sumOf { it.amount }

        binding.categoryTotalLabel.text = if (selectedCategory == "All Categories") "Total Spend (Current Filter)" else "Total for $selectedCategory"
        binding.categoryTotalValue.text = String.format(Locale.getDefault(), "- R %.2f", categoryTotal)
        binding.grandTotalValue.text = String.format(Locale.getDefault(), "R %.2f", grandTotalAll)

        // Dynamically build transaction list
        binding.transactionListContainer.removeAllViews()
        filtered.forEachIndexed { index, txn ->
            val txnView = createTransactionView(txn)
            binding.transactionListContainer.addView(txnView)
            if (index < filtered.size - 1) {
                val divider = View(context)
                divider.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
                divider.setBackgroundColor(android.graphics.Color.LTGRAY)
                binding.transactionListContainer.addView(divider)
            }
        }
    }

    private fun createTransactionView(txn: Transaction): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_transaction, binding.transactionListContainer, false)
        // Bind data to the layout (reusing item_transaction)
        val title = view.findViewById<android.widget.TextView>(R.id.transactionTitle)
        val date = view.findViewById<android.widget.TextView>(R.id.transactionDate)
        val amount = view.findViewById<android.widget.TextView>(R.id.transactionAmount)
        val icon = view.findViewById<android.widget.ImageView>(R.id.transactionIcon)

        title.text = txn.title
        date.text = txn.date
        if (txn.type == "Income") {
            amount.text = String.format(Locale.getDefault(), "+ R %.2f", txn.amount)
            val color = resources.getColor(R.color.income_green, null)
            amount.setTextColor(color)
            icon.setColorFilter(color)
        } else {
            amount.text = String.format(Locale.getDefault(), "- R %.2f", txn.amount)
            val color = resources.getColor(R.color.expense_red, null)
            amount.setTextColor(color)
            icon.setColorFilter(color)
        }

        view.setOnClickListener {
            val bundle = Bundle().apply {
                putString("title", txn.title)
                putString("amount", amount.text.toString())
                putString("date", txn.date)
            }
            findNavController().navigate(R.id.action_AccountDetailFragment_to_TransactionDetailFragment, bundle)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}