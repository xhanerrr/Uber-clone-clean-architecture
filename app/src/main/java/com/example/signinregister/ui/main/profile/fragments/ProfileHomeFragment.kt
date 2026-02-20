package com.example.signinregister.ui.main.profile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.signinregister.databinding.FragmentProfileHomeBinding
import com.example.signinregister.ui.main.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileHomeFragment : Fragment() {

    private var _binding: FragmentProfileHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.username.observe(viewLifecycleOwner) { username ->
            binding.tvProfileName.text = username ?: "Add your username"
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
