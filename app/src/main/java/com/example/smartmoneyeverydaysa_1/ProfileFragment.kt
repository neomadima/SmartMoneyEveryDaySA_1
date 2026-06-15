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
import com.example.smartmoneyeverydaysa_1.databinding.FragmentProfileBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        observeUserData()

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_ProfileFragment_to_EditProfileFragment)
        }

        binding.btnLogout.setOnClickListener {
            findNavController().navigate(R.id.AuthFragment)
        }
    }

    private fun observeUserData() {
        viewModel.currentUserEmail.observe(viewLifecycleOwner) { email ->
            viewLifecycleOwner.lifecycleScope.launch {
                db.userDao().observeUserByEmail(email).collectLatest { user ->
                    user?.let {
                        binding.profileName.text = it.fullName
                        binding.profileEmail.text = it.email
                        binding.profilePhone.text = "+27 ${it.phoneNumber}"
                        binding.profileIdNumber.text = it.idNumber
                        binding.profileMemberSince.text = it.memberSince
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}