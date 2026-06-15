package com.example.smartmoneyeverydaysa_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.databinding.FragmentTransferBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransferFragment : Fragment() {

    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.accounts.observe(viewLifecycleOwner) { dynamicAccounts ->
            val hardcodedAccounts = listOf(
                getString(R.string.cheque_account),
                getString(R.string.smart_saver),
                getString(R.string.visa_credit_card)
            )
            val allAccountNames = hardcodedAccounts + dynamicAccounts.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, allAccountNames)
            binding.fromAccountDropdown.setAdapter(adapter)
            binding.toAccountDropdown.setAdapter(adapter)
        }

        binding.confirmTransferButton.setOnClickListener {
            val from = binding.fromAccountDropdown.text.toString()
            val to = binding.toAccountDropdown.text.toString()
            val amountStr = binding.transferAmount.text.toString()

            if (from == to) {
                Toast.makeText(requireContext(), R.string.error_same_account, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amountStr.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val dateStr = "Today, $currentTime"
                
                // Add a transaction for the transfer (Outgoing from source account)
                viewModel.addTransaction(
                    title = getString(R.string.transfer_to, to),
                    amount = amount,
                    date = dateStr,
                    category = getString(R.string.category_transfer),
                    type = getString(R.string.type_outgoing),
                    accountName = from,
                )

                // Add a transaction for the transfer (Income for destination account)
                viewModel.addTransaction(
                    title = getString(R.string.transfer_from, from),
                    amount = amount,
                    date = dateStr,
                    category = getString(R.string.category_transfer),
                    type = getString(R.string.type_income),
                    accountName = to,
                )

                val successMsg = getString(R.string.transfer_success, String.format(Locale.getDefault(), "%,.2f", amount), from, to)
                Toast.makeText(requireContext(), successMsg, Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), R.string.error_enter_amount, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}