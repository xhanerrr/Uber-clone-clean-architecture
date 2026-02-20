package com.example.signinregister.data

import android.location.Location
import com.example.signinregister.domain.LocationEntity
import com.google.android.gms.maps.model.LatLng

fun Location.toDomainEntity(): LocationEntity {
    return LocationEntity(
        latitude = this.latitude,
        longitude = this.longitude
    )
}

fun LocationEntity.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}
