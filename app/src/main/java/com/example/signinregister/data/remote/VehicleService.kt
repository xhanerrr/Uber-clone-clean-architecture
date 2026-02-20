package com.example.signinregister.data.remote

import com.example.signinregister.data.remote.Vehicle
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface VehicleService {

    @GET
    suspend fun getVehicles(@Url url: String): Response<List<Vehicle>>

}