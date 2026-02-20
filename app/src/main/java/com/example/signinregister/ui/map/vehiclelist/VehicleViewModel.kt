package com.example.signinregister.ui.map.vehiclelist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signinregister.data.remote.Vehicle
import com.example.signinregister.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _vehicles = MutableLiveData<List<Vehicle>>()
    val vehicles: LiveData<List<Vehicle>> = _vehicles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        Log.d("INIT_TEST", "VehicleViewModel: Bloque INIT ejecutado. Llamando a loadVehicles().")
        loadVehicles()
    }

    fun loadVehicles() {
        Log.d("INIT_TEST", "VehicleViewModel: Función loadVehicles() ejecutada.")

        viewModelScope.launch {
            Log.d("INIT_TEST", "VehicleViewModel: Corrutina de carga lanzada.")

            _isLoading.value = true
            _errorMessage.value = null

            try {
                val loadedVehicles = repository.fetchVehicles()
                _vehicles.value = loadedVehicles
                Log.d("INIT_TEST", "SUCCESS: Vehículos cargados: ${loadedVehicles.size}")

            } catch (e: Exception) {
                _errorMessage.value = "Fallo al cargar los vehículos: ${e.message}"
                Log.e("INIT_TEST", "FAILURE: Error en la carga de datos. ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}