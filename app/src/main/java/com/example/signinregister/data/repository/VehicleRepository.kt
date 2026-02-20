package com.example.signinregister.data.repository

import com.example.signinregister.data.remote.Vehicle
import com.example.signinregister.data.remote.VehicleService
import jakarta.inject.Inject

class VehicleRepository @Inject constructor(private val service: VehicleService){
    private val GIST_URL = "https://gist.githubusercontent.com/xhanerrr/15a1661227835de64b12b00a96bc9778/raw/0c4d6b3ba1f654a62ddd475d022af0d8d109b55c/cars.json"

    suspend fun fetchVehicles(): List<Vehicle> {
        val response = service.getVehicles(GIST_URL)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        }
        throw Exception("Error fetching vehicles: ${response.code()}")
    }

    suspend fun fetchVehicleDetails(vehicleId: String): Vehicle? {
        val allVehicles = fetchVehicles()

        return allVehicles.find { it.id == vehicleId }
    }
}