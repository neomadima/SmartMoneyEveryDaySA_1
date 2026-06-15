package com.example.smartmoneyeverydaysa_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.databinding.FragmentAccountsBinding
import com.example.smartmoneyeverydaysa_1.databinding.ItemAccountBinding
import java.text.NumberFormat
import java.util.Locale

class AccountsFragment : Fragment() {
    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardCheque.setOnClickListener {
            navigateToDetail("Cheque Account")
        }
        binding.cardSavings.setOnClickListener {
            navigateToDetail("Smart Saver")
        }
        binding.cardCredit.setOnClickListener {
            navigateToDetail("Visa Credit Card")
        }

        binding.cardAddAccount.setOnClickListener {
            findNavController().navigate(R.id.action_AccountsFragment_to_AddAccountFragment)
        }

        binding.actionPay.setOnClickListener {
            findNavController().navigate(R.id.action_AccountsFragment_to_PayFragment)
        }
        binding.actionTransfer.setOnClickListener {
            findNavController().navigate(R.id.action_AccountsFragment_to_TransferFragment)
        }
        binding.actionStatements.setOnClickListener {
            findNavController().navigate(R.id.action_AccountsFragment_to_StatementsFragment)
        }

        setupObservers()
    }

    private fun setupObservers() {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        
        viewModel.accounts.observe(viewLifecycleOwner) { accounts ->
            binding.dynamicAccountsContainer.removeAllViews()
            
            accounts.forEach { account ->
                val itemBinding = ItemAccountBinding.inflate(layoutInflater, binding.dynamicAccountsContainer, false)
                itemBinding.accountName.text = account.name
                itemBinding.accountNumber.text = account.accountNumber
                itemBinding.accountBalance.text = currencyFormat.format(account.balance)
                itemBinding.accountType.text = account.type
                
                itemBinding.root.setOnClickListener {
                    navigateToDetail(account.name)
                }
                
                binding.dynamicAccountsContainer.addView(itemBinding.root)
            }
        }
    }

    private fun navigateToDetail(name: String) {
        val bundle = Bundle().apply {
            putString("accountName", name)
        }
        findNavController().navigate(R.id.action_AccountsFragment_to_AccountDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}