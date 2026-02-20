package com.example.signinregister.domain.repository

import com.example.signinregister.domain.LocationEntity

interface LocationRepository {
    suspend fun getLastKnownLocation(): LocationEntity?
}