package com.example.signinregister.ui.main.profile.fragments.personalfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.signinregister.databinding.FragmentNameBinding
import com.example.signinregister.ui.main.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NameFragment : Fragment() {

    private var _binding: FragmentNameBinding? = null
    private val binding get() = _binding!!

    private var originalButtonText: CharSequence? = null
    private val viewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupListeners()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading

            if (isLoading) {
                binding.btnSave.text = ""
            } else {
                binding.btnSave.text = originalButtonText
            }
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { statusMessage ->

            if (statusMessage != null) {

                Toast.makeText(requireContext(), statusMessage, Toast.LENGTH_SHORT).show()

                if (statusMessage.contains("éxito", ignoreCase = true)) {
                    parentFragmentManager.popBackStack()
                }

                viewModel.clearUpdateStatus()
            }
        }

        viewModel.username.observe(viewLifecycleOwner) { currentName ->
            if (binding.etName.text.isNullOrEmpty()) {
                binding.etName.setText(currentName)
            }
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val newName = binding.etName.text.toString().trim()

            if (newName.isNotBlank()) {
                viewModel.updateUsername(newName)

            } else {
                Toast.makeText(requireContext(), "El nombre no puede estar vacío.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}