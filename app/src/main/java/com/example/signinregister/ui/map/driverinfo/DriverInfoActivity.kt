package com.example.signinregister.ui.map.driverinfo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.privacysandbox.ads.adservices.adid.AdId
import coil.load
import com.example.signinregister.R
import com.example.signinregister.databinding.ActivityDriverInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDriverInfoBinding

    private val viewModel: DriverInfoViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDriverInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()

        val vehicleId   = intent.getStringExtra("VEHICLE_ID")

        if (vehicleId.isNullOrEmpty()) {
            Log.e("DRIVER_INFO", "Error: No se recibió un ID de vehículo válido.")
            Toast.makeText(this, "No se pudieron cargar los detalles del vehículo.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Log.d("DRIVER_INFO", "ID del Vehículo recibido: $vehicleId")

        viewModel.fetchVehicleDetails(vehicleId)
        observeVehicleData()
    }

    private fun setupListeners(){
        binding.backtomap.setOnClickListener {
            finish()
        }
    }

    private fun observeVehicleData(){
        viewModel.vehicleDetails.observe(this) { vehicle ->
            if (vehicle != null) {

                binding.carModelTxtView.text = "${vehicle.brand} ${vehicle.model}"
                binding.driverNameTxtView.text = "Conductor: ${vehicle.driverName}"
                binding.driverRatingTxtView.text = String.format("%.1f", vehicle.rating)

                binding.licensePlateTxtView.text = "Placa: ${vehicle.plate}"
                binding.carColorTxtView.text = "Color: ${vehicle.color}"

                binding.imageCarView.load(vehicle.imageUrl) {
                    crossfade(true)
                }
            } else {
                Toast.makeText(this, "Detalles del vehículo no disponibles.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


}