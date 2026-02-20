package com.example.signinregister.domain.repository

import com.example.signinregister.domain.AddressSuggestion
import com.example.signinregister.domain.LocationResult
import com.google.android.gms.maps.model.LatLng

interface PlacesRepository {
    suspend fun getAutocompleteSuggestions(query: String): List<AddressSuggestion>
    suspend fun fetchPlaceCoordinates(placeId: String, placeName: String): LocationResult
    suspend fun reverseGeocodeLocation(latLng: LatLng): LocationResult
}