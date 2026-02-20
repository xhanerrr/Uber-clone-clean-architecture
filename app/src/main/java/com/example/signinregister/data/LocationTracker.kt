package com.example.signinregister.data

import com.example.signinregister.domain.LocationEntity

interface LocationTracker {
    suspend fun getCurrentLocation(): LocationEntity?
}
