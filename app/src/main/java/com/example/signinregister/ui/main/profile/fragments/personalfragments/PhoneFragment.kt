package com.example.signinregister.ui.main.profile.fragments.personalfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.signinregister.databinding.FragmentPhoneBinding
import com.example.signinregister.ui.main.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhoneFragment : Fragment() {

    private var _binding: FragmentPhoneBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupListeners()
    }

    private fun observeViewModel() {
        viewModel.phone.observe(viewLifecycleOwner) { currentPhone ->
            if (binding.etPhone.text.toString().isEmpty()) {
                binding.etPhone.setText(currentPhone)
            }
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { message ->
            if (message != null && (message.contains("Teléfono") || message.contains("Error"))) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val newPhone = binding.etPhone.text.toString().trim()

            if (newPhone.isNotBlank()) {
                viewModel.updatePhone(newPhone)
            } else {
                Toast.makeText(requireContext(), "El número de teléfono no puede estar vacío.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
