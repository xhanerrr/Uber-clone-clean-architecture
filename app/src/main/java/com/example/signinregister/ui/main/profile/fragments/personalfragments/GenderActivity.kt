package com.example.signinregister.ui.main.profile.fragments.personalfragments

import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signinregister.R
import com.example.signinregister.databinding.ActivityGenderBinding
import com.example.signinregister.ui.main.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GenderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenderBinding

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        observeViewModel()
        setupListeners()

    }

    private fun observeViewModel() {
        viewModel.updateStatus.observe(this) { message ->
            if (message != null && (message.contains("Género") || message.contains("Error"))) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                if (!message.contains("Error")) {
                    finish()
                }
            }
        }

        viewModel.gender.observe(this) { currentGender ->
            when (currentGender) {
                "Woman" -> binding.rgGenderOptions.check(R.id.rbWoman)
                "Man" -> binding.rgGenderOptions.check(R.id.rbMan)
                "Non-Binary" -> binding.rgGenderOptions.check(R.id.rbNonBinary)
                "None of the above" -> binding.rgGenderOptions.check(R.id.rbNone)
                "Remove" -> binding.rgGenderOptions.check(R.id.rbRemove)
                else -> binding.rgGenderOptions.clearCheck()
            }
        }
    }

    private fun setupListeners() {

        binding.btnSubmit.setOnClickListener {
            val selectedId = binding.rgGenderOptions.checkedRadioButtonId

            if (selectedId != -1) {
                val selectedRadioButton = binding.root.findViewById<RadioButton>(selectedId)
                var selectedGenderText = selectedRadioButton.text.toString()

                if (selectedGenderText == "Remove my gender information") {
                    selectedGenderText = "Remove"
                }

                viewModel.updateGender(selectedGenderText)

            } else {
                Toast.makeText(this, "Por favor, selecciona una opción.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }
}
