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
import com.example.smartmoneyeverydaysa_1.databinding.FragmentPayBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PayFragment : Fragment() {

    private var _binding: FragmentPayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Setup Account Dropdown
        val accounts = arrayOf("Cheque Account", "Smart Saver", "Visa Credit Card")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, accounts)
        binding.accountDropdown.setAdapter(adapter)
        binding.accountDropdown.setText(accounts[0], false)

        binding.confirmPaymentButton.setOnClickListener {
            val name = binding.recipientName.text.toString()
            val amountStr = binding.paymentAmount.text.toString()
            val selectedAccount = binding.accountDropdown.text.toString()

            if (name.isNotEmpty() && amountStr.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                
                viewModel.addTransaction(
                    title = name,
                    amount = amount,
                    date = "Today, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}",
                    category = "Payment",
                    type = "Outgoing",
                    accountName = selectedAccount
                )

                Toast.makeText(requireContext(), "Payment of R $amount to $name from $selectedAccount was successful!", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}