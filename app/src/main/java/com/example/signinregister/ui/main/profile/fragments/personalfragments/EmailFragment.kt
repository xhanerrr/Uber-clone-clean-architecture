package com.example.signinregister.ui.main.profile.fragments.personalfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.signinregister.databinding.FragmentEmailBinding
import com.example.signinregister.ui.main.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmailFragment : Fragment() {

    private var _binding: FragmentEmailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupListeners()
    }

    private fun observeViewModel() {
        viewModel.email.observe(viewLifecycleOwner) { currentEmail ->
            if (binding.etEmail.text.toString().isEmpty()) {
                binding.etEmail.setText(currentEmail)
            }
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { message ->
            if (message != null && (message.contains("Email") || message.contains("Error"))) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val newEmail = binding.etEmail.text.toString().trim()

            if (newEmail.isNotBlank()) {
                viewModel.updateEmail(newEmail)
            } else {
                Toast.makeText(requireContext(), "El email no puede estar vacío.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
