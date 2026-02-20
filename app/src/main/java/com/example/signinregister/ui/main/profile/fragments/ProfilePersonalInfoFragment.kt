package com.example.signinregister.ui.main.profile.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.signinregister.R
import com.example.signinregister.databinding.FragmentProfilePersonalInfoBinding
import com.example.signinregister.ui.login.LoginActivity
import com.example.signinregister.ui.main.profile.ProfileViewModel
import com.example.signinregister.ui.main.profile.fragments.personalfragments.GenderActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfilePersonalInfoFragment : Fragment() {

    private var _binding: FragmentProfilePersonalInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()
        setupNavigationListeners()
        observeLogoutEvent()
    }

    private fun loadUserData() {
        viewModel.username.observe(viewLifecycleOwner) { name ->
            binding.tvNameSubtitle.text = name
        }

        viewModel.gender.observe(viewLifecycleOwner) { gender ->
            binding.tvGenderSubtitle.text =
                if (gender.isNullOrEmpty() || gender == "Remove") "Add your gender" else gender
        }

        viewModel.phone.observe(viewLifecycleOwner) { phone ->
            binding.tvPhoneSubtitle.text =
                if (phone.isNullOrEmpty()) "Add your phone number" else phone
            binding.ivPhoneAlert.visibility =
                if (phone.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.email.observe(viewLifecycleOwner) { email ->
            binding.tvEmailSubtitle.text = email
            binding.ivEmailCheck.visibility =
                if (email.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun setupNavigationListeners() {

        binding.rowName.setOnClickListener {
            findNavController().navigate(R.id.action_personalInfoFragment_to_nameFragment)
        }

        binding.rowGender.setOnClickListener {
            val intent = Intent(requireActivity(), GenderActivity::class.java)
            startActivity(intent)
        }

        binding.rowPhone.setOnClickListener {
            findNavController().navigate(R.id.action_personalInfoFragment_to_phoneFragment)
        }

        binding.rowEmail.setOnClickListener {
            findNavController().navigate(R.id.action_personalInfoFragment_to_emailFragment)
        }

        binding.rowLanguage.setOnClickListener {
            Toast.makeText(requireContext(), "Abrir configuración de Idioma", Toast.LENGTH_SHORT).show()
        }

        binding.frameAvatar.setOnClickListener {
            Toast.makeText(requireContext(), "Abrir selector de Avatar", Toast.LENGTH_SHORT).show()
        }

        binding.logoutBtn.setOnClickListener {
            viewModel.performLogout()
        }
    }

    private fun observeLogoutEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutEvent.collect { isLoggedOut ->
                    if (isLoggedOut) {
                        val intent = Intent(requireActivity(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            }
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}