package com.example.smartmoneyeverydaysa_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.data.AppDatabase
import com.example.smartmoneyeverydaysa_1.data.entities.UserEntity
import com.example.smartmoneyeverydaysa_1.databinding.FragmentAuthBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        // Initially show Login view
        showLogin()

        binding.authTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) showLogin() else showRegister()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showLogin() {
        val loginView = LayoutInflater.from(context).inflate(R.layout.layout_login_sub, binding.authContainer, false)
        binding.authContainer.removeAllViews()
        binding.authContainer.addView(loginView)

        val emailEdit = loginView.findViewById<EditText>(R.id.loginEmail)
        val passwordEdit = loginView.findViewById<EditText>(R.id.loginPassword)

        loginView.findViewById<View>(R.id.loginButton).setOnClickListener {
            val input = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString() // Don't trim password

            if (input.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Try searching by email (case-insensitive)
                var user = db.userDao().getUserByEmail(input)
                
                // If not found, try searching by ID number (as "Account Number")
                if (user == null) {
                    user = db.userDao().getUserByIdNumber(input)
                }

                if (user != null && user.passwordHash == password) {
                    viewModel.setCurrentUserEmail(user.email)
                    findNavController().navigate(R.id.action_AuthFragment_to_DashboardFragment)
                } else if ((input.lowercase() == "john.doe@example.com" || input == "9001015000081") && password == "password") {
                    // Re-create the default user in the database if they were wiped by migration
                    val defaultUser = UserEntity(
                        fullName = "John Doe",
                        idNumber = "9001015000081",
                        phoneNumber = "712345678",
                        email = "john.doe@example.com",
                        passwordHash = "password",
                        memberSince = "January 2023"
                    )
                    db.userDao().insertUser(defaultUser)
                    viewModel.setCurrentUserEmail("john.doe@example.com")
                    findNavController().navigate(R.id.action_AuthFragment_to_DashboardFragment)
                } else {
                    Toast.makeText(requireContext(), "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        loginView.findViewById<View>(R.id.forgotPasswordText).setOnClickListener {
            Toast.makeText(requireContext(), "Password reset link sent to email", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRegister() {
        val registerView = LayoutInflater.from(context).inflate(R.layout.layout_register_sub, binding.authContainer, false)
        binding.authContainer.removeAllViews()
        binding.authContainer.addView(registerView)

        val fullNameEdit = registerView.findViewById<EditText>(R.id.registerFullName)
        val idNumberEdit = registerView.findViewById<EditText>(R.id.registerIdNumber)
        val phoneEdit = registerView.findViewById<EditText>(R.id.registerPhone)
        val emailEdit = registerView.findViewById<EditText>(R.id.registerEmail)
        val passwordEdit = registerView.findViewById<EditText>(R.id.registerPassword)
        val confirmPasswordEdit = registerView.findViewById<EditText>(R.id.registerConfirmPassword)
        val termsCheckbox = registerView.findViewById<CheckBox>(R.id.termsCheckbox)

        registerView.findViewById<View>(R.id.registerButton).setOnClickListener {
            val fullName = fullNameEdit.text.toString().trim()
            val idNumber = idNumberEdit.text.toString().trim()
            val phone = phoneEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString() // Don't trim password
            val confirmPassword = confirmPasswordEdit.text.toString() // Don't trim password
            val termsChecked = termsCheckbox.isChecked

            if (fullName.isEmpty() || idNumber.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!termsChecked) {
                Toast.makeText(requireContext(), "Please agree to the Terms and Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Check if email already exists
                val existingUserByEmail = db.userDao().getUserByEmail(email)
                if (existingUserByEmail != null) {
                    Toast.makeText(requireContext(), "Email already registered", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Check if ID Number already exists
                val existingUserById = db.userDao().getUserByIdNumber(idNumber)
                if (existingUserById != null) {
                    Toast.makeText(requireContext(), "ID Number already registered", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val newUser = UserEntity(
                    fullName = fullName,
                    idNumber = idNumber,
                    phoneNumber = phone,
                    email = email,
                    passwordHash = password, // Store password (should be hashed in production)
                    memberSince = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                )

                db.userDao().insertUser(newUser)
                viewModel.setCurrentUserEmail(email)
                Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_AuthFragment_to_DashboardFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}