package com.example.signinregister.data

import android.location.Location
import android.os.Looper
import com.example.signinregister.domain.LocationEntity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocationTrackerImpl @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationTracker {

    @Suppress("MissingPermission")
    override suspend fun getCurrentLocation(): LocationEntity? {
        return try {
            val locationRequest = LocationRequest.Builder(0)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            val location: Location? = fusedLocationClient.getCurrentLocation(
                locationRequest.priority,
                CancellationTokenSource().token
            ).await()

            location?.toDomainEntity()

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}