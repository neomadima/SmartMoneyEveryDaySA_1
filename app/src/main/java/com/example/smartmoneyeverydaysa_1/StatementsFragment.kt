package com.example.smartmoneyeverydaysa_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.databinding.FragmentStatementsBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatementsFragment : Fragment() {

    private var _binding: FragmentStatementsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.statementsContainer.getChildAt(0).setOnClickListener {
            navigateToStatement("MAY 2024")
        }
        binding.statementsContainer.getChildAt(2).setOnClickListener {
            navigateToStatement("APRIL 2024")
        }
        binding.statementsContainer.getChildAt(4).setOnClickListener {
            navigateToStatement("MARCH 2024")
        }

        binding.btnCustomRange.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Period")
            .setSelection(
                Pair(
                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds()
                )
            )
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val start = selection.first
            val end = selection.second
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val rangeStr = "${sdf.format(Date(start))} - ${sdf.format(Date(end))}"
            
            val bundle = Bundle().apply {
                putString("month", rangeStr)
                putLong("startDate", start)
                putLong("endDate", end)
            }
            findNavController().navigate(R.id.action_StatementsFragment_to_StatementViewFragment, bundle)
        }
        dateRangePicker.show(parentFragmentManager, "date_range_picker")
    }

    private fun navigateToStatement(month: String) {
        val bundle = Bundle().apply {
            putString("month", month)
        }
        findNavController().navigate(R.id.action_StatementsFragment_to_StatementViewFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}