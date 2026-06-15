package com.example.smartmoneyeverydaysa_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.databinding.FragmentLimitsBinding

class LimitsFragment : Fragment() {

    private var _binding: FragmentLimitsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLimitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize with current values
        viewModel.minMonthlySpend.observe(viewLifecycleOwner) { min ->
            binding.minMonthlySpendInput.setText(min?.toString() ?: "")
        }
        viewModel.maxMonthlySpend.observe(viewLifecycleOwner) { max ->
            binding.monthlyLimitInput.setText(max?.toString() ?: "")
        }
        viewModel.dailyAtmWithdrawalLimit.observe(viewLifecycleOwner) { atm ->
            binding.dailyAtmInput.setText(atm?.toString() ?: "")
        }
        viewModel.dailyOnlinePurchaseLimit.observe(viewLifecycleOwner) { online ->
            binding.dailyOnlineInput.setText(online?.toString() ?: "")
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSaveLimits.setOnClickListener {
            val minMonthly = binding.minMonthlySpendInput.text.toString().toDoubleOrNull()
            val maxMonthly = binding.monthlyLimitInput.text.toString().toDoubleOrNull()
            val dailyAtm = binding.dailyAtmInput.text.toString().toDoubleOrNull()
            val dailyOnline = binding.dailyOnlineInput.text.toString().toDoubleOrNull()
            val minBalance = binding.minBalanceInput.text.toString()
            
            // Allow saving if at least one field is valid or we are updating the safety net
            if (minBalance.isNotEmpty()) {
                viewModel.setMonthlySpendingGoals(minMonthly, maxMonthly)
                viewModel.setDailyLimits(dailyAtm, dailyOnline)
                Toast.makeText(requireContext(), "Your financial goals and limits have been successfully updated!", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Please ensure the Minimum Balance Alert is completed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}