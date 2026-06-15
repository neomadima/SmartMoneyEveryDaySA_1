package com.example.smartmoneyeverydaysa_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.databinding.FragmentAddGoalBinding

class AddGoalFragment : Fragment() {

    private var _binding: FragmentAddGoalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private var editingGoalId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            if (it.containsKey("goalId")) {
                editingGoalId = it.getLong("goalId")
                binding.toolbar.title = "Edit Saving Goal"
                binding.saveGoalButton.text = "Update Goal"
                binding.goalNameInput.setText(it.getString("name"))
                binding.targetAmountInput.setText(it.getDouble("target").toString())
                binding.initialContributionInput.setText(it.getDouble("saved").toString())
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.saveGoalButton.setOnClickListener {
            val name = binding.goalNameInput.text.toString()
            val targetStr = binding.targetAmountInput.text.toString()
            val initialStr = binding.initialContributionInput.text.toString()

            if (name.isNotEmpty() && targetStr.isNotEmpty()) {
                val target = targetStr.toDoubleOrNull() ?: 0.0
                val initial = initialStr.toDoubleOrNull() ?: 0.0
                
                if (editingGoalId != null) {
                    viewModel.updateGoal(editingGoalId!!, name, target, initial)
                    Toast.makeText(requireContext(), "Goal updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addGoal(name, target, initial)
                    Toast.makeText(requireContext(), "Goal '$name' for R $target set successfully!", Toast.LENGTH_LONG).show()
                }
                
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