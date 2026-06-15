package com.example.smartmoneyeverydaysa_1

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.databinding.FragmentAddExpenseBinding
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private var currentPhotoUri: String? = null

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                binding.attachedImageView.setImageBitmap(bitmap)
                showImage()
                // In a real app, we'd save this bitmap to a file and get a URI
                currentPhotoUri = "camera_temp_uri" 
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            binding.attachedImageView.setImageURI(uri)
            showImage()
            currentPhotoUri = uri.toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Account Dropdown
        val accounts = arrayOf("Cheque Account", "Smart Saver", "Visa Credit Card")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, accounts)
        binding.accountDropdown.setAdapter(adapter)
        binding.accountDropdown.setText(accounts[0], false)

        binding.startTimeInput.setOnClickListener {
            showTimePicker { time -> binding.startTimeInput.setText(time) }
        }

        binding.endTimeInput.setOnClickListener {
            showTimePicker { time -> binding.endTimeInput.setText(time) }
        }

        binding.btnTakePhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePhotoLauncher.launch(intent)
        }

        binding.btnUploadPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        binding.btnAddExpense.setOnClickListener {
            val amountStr = binding.amountInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val selectedAccount = binding.accountDropdown.text.toString()
            val selectedChipId = binding.categoryChipGroup.checkedChipId
            val category = if (selectedChipId != View.NO_ID) {
                binding.categoryChipGroup.findViewById<Chip>(selectedChipId).text.toString()
            } else {
                "Expense"
            }

            if (amountStr.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                
                // Show confirmation dialog before adding
                AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Expense")
                    .setMessage("Are you sure you want to add this expense of R $amountStr from $selectedAccount?")
                    .setPositiveButton("Add Expense") { _, _ ->
                        val title = if (description.isNotEmpty()) description else category
                        
                        viewModel.addTransaction(
                            title = title,
                            amount = amount,
                            date = "Today, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}",
                            category = category,
                            type = "Outgoing",
                            photoUri = currentPhotoUri,
                            accountName = selectedAccount
                        )

                        Toast.makeText(requireContext(), "Expense of R $amountStr saved successfully!", Toast.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, h, m ->
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            onTimeSelected(formattedTime)
        }, hour, minute, true).show()
    }

    private fun showImage() {
        binding.attachedImageView.visibility = View.VISIBLE
        binding.placeholderLayout.visibility = View.GONE
    }

    private fun showAddCategoryDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add New Category")
        val input = EditText(requireContext())
        input.hint = "Category Name"
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val name = input.text.toString()
            if (name.isNotEmpty()) {
                addNewChip(name)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun addNewChip(name: String) {
        val chip = Chip(requireContext())
        chip.text = name
        chip.isCheckable = true
        chip.isChecked = true
        binding.categoryChipGroup.addView(chip)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}