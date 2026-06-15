package com.example.smartmoneyeverydaysa_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.data.AppDatabase
import com.example.smartmoneyeverydaysa_1.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        observeUserData()

        binding.signOutButton.setOnClickListener {
            Toast.makeText(requireContext(), "Signed Out", Toast.LENGTH_SHORT).show()
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.AuthFragment, null, navOptions)
        }

        binding.btnLimits.setOnClickListener {
            findNavController().navigate(R.id.action_SettingsFragment_to_LimitsFragment)
        }
    }

    private fun observeUserData() {
        viewModel.currentUserEmail.observe(viewLifecycleOwner) { email ->
            viewLifecycleOwner.lifecycleScope.launch {
                db.userDao().observeUserByEmail(email).collectLatest { user ->
                    user?.let {
                        binding.settingsName.text = it.fullName
                        binding.settingsEmail.text = it.email
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