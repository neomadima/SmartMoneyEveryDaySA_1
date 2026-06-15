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
import com.example.smartmoneyeverydaysa_1.databinding.FragmentAddAccountBinding

class AddAccountFragment : Fragment() {

    private var _binding: FragmentAddAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val types = arrayOf("Cheque", "Savings", "Credit Card", "Investment")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.accountTypeDropdown.setAdapter(adapter)

        binding.saveAccountButton.setOnClickListener {
            val name = binding.accountNameInput.text.toString()
            val balanceStr = binding.initialBalanceInput.text.toString()
            val type = binding.accountTypeDropdown.text.toString()

            if (name.isNotEmpty() && balanceStr.isNotEmpty() && type.isNotEmpty()) {
                val balance = balanceStr.toDoubleOrNull() ?: 0.0
                viewModel.addAccount(name, balance, type)
                Toast.makeText(requireContext(), "Account '$name' created successfully!", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Please fill in all details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}