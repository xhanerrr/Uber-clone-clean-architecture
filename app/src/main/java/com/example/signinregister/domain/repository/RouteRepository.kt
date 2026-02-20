package com.example.signinregister.domain.repository

import com.google.android.gms.maps.model.LatLng

interface RouteRepository {
    suspend fun getRoutePolyline(origin: LatLng, destination: LatLng): String
}
