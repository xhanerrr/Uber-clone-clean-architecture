package com.example.signinregister.ui.map.driverinfo

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
class DriverInfoViewModel @Inject constructor(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _vehicleDetails = MutableLiveData<Vehicle?>()
    val vehicleDetails: LiveData<Vehicle?> = _vehicleDetails

    fun fetchVehicleDetails(vehicleId: String) {
        viewModelScope.launch {
            try {
                val vehicle = repository.fetchVehicleDetails(vehicleId)
                _vehicleDetails.value = vehicle

            } catch (e: Exception) {
                _vehicleDetails.value = null
            }
        }
    }
}