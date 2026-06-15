package com.example.smartmoneyeverydaysa_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.data.AppDatabase
import com.example.smartmoneyeverydaysa_1.data.entities.UserEntity
import com.example.smartmoneyeverydaysa_1.databinding.FragmentEditProfileBinding
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private val viewModel: MainViewModel by activityViewModels()
    private var currentUser: UserEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        loadUserData()

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadUserData() {
        val email = viewModel.currentUserEmail.value ?: "john.doe@example.com"
        viewLifecycleOwner.lifecycleScope.launch {
            val users = db.userDao().getUserByEmail(email) 
            
            if (users != null) {
                currentUser = users
                binding.etFullName.setText(users.fullName)
                binding.etIdNumber.setText(users.idNumber)
                binding.etPhoneNumber.setText(users.phoneNumber)
                binding.etEmail.setText(users.email)
            } else {
                // Pre-fill with defaults if no user found
                binding.etFullName.setText("John Doe")
                binding.etIdNumber.setText("9001015000081")
                binding.etPhoneNumber.setText("712345678")
                binding.etEmail.setText("john.doe@example.com")
            }
        }
    }

    private fun saveProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val idNumber = binding.etIdNumber.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (fullName.isEmpty() || idNumber.isEmpty() || phoneNumber.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all mandatory fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isNotEmpty() && password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val finalPasswordHash = if (password.isNotEmpty()) password else (currentUser?.passwordHash ?: "mock_hash")

            val userToUpdate = currentUser?.copy(
                fullName = fullName,
                idNumber = idNumber,
                phoneNumber = phoneNumber,
                email = email,
                passwordHash = finalPasswordHash
            ) ?: UserEntity(
                fullName = fullName,
                idNumber = idNumber,
                phoneNumber = phoneNumber,
                email = email,
                passwordHash = finalPasswordHash,
                memberSince = "January 2023"
            )

            if (currentUser != null) {
                db.userDao().updateUser(userToUpdate)
            } else {
                db.userDao().insertUser(userToUpdate)
            }
            
            // Update the session email if the user changed their email
            if (email != viewModel.currentUserEmail.value) {
                viewModel.setCurrentUserEmail(email)
            }

            Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
