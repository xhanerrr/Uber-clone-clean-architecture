package com.example.signinregister.domain

import com.google.android.gms.maps.model.LatLng

data class LocationResult(
    val addressName: String,
    val latLng: LatLng
)