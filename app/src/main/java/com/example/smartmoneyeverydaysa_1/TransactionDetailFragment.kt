package com.example.smartmoneyeverydaysa_1

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartmoneyeverydaysa_1.databinding.FragmentTransactionDetailBinding

class TransactionDetailFragment : Fragment() {

    private var _binding: FragmentTransactionDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val title = arguments?.getString("title") ?: "Transaction"
        val amount = arguments?.getString("amount") ?: "R 0.00"
        val date = arguments?.getString("date") ?: ""
        val photoUri = arguments?.getString("photoUri")

        binding.detailTitle.text = title
        binding.detailAmount.text = amount
        if (date.isNotEmpty()) {
            binding.detailDate.text = date
        }

        if (!photoUri.isNullOrEmpty()) {
            binding.photoCard.visibility = View.VISIBLE
            if (photoUri == "camera_temp_uri") {
                binding.detailPhoto.setImageResource(android.R.drawable.ic_menu_camera)
            } else {
                binding.detailPhoto.setImageURI(Uri.parse(photoUri))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}