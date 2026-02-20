package com.example.signinregister.domain.repository

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.example.signinregister.domain.AddressSuggestion
import com.example.signinregister.domain.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlacesRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PlacesRepository {

    private val placesClient = Places.createClient(context)
    private val sessionToken = AutocompleteSessionToken.newInstance()
    private val geocoder = Geocoder(context)

    private val PERU_COUNTRY_CODE = "PE"
    private val DEBUG_TAG = "PLACES_REPO_DEBUG"

    override suspend fun getAutocompleteSuggestions(query: String): List<AddressSuggestion> {
        if (query.isBlank()) {
            return emptyList()
        }

        val currentSessionToken = AutocompleteSessionToken.newInstance()

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries(listOf(PERU_COUNTRY_CODE))
            .setSessionToken(currentSessionToken)
            .build()

        return try {
            val response = placesClient.findAutocompletePredictions(request).await()

            Log.d(DEBUG_TAG, "Sugerencias obtenidas correctamente: ${response.autocompletePredictions.size}")

            response.autocompletePredictions.map { prediction ->
                AddressSuggestion(
                    placeId = prediction.placeId,
                    mainText = prediction.getPrimaryText(null).toString(),
                    secondaryText = prediction.getSecondaryText(null).toString()
                )
            }
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "Error durante la obtención de sugerencias: ${e.message}")
            emptyList()
        }
    }

    override suspend fun fetchPlaceCoordinates(placeId: String, placeName: String): LocationResult {
        val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)

        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        return try {
            val response = placesClient.fetchPlace(request).await()
            val place = response.place

            val latLng = place.latLng ?: throw Exception("Coordinates not found for place.")

            LocationResult(
                addressName = placeName,
                latLng = latLng
            )
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "Fallo al obtener detalles del lugar para PlaceID: $placeId. Mensaje: ${e.message}", e)
            throw e
        }
    }

    override suspend fun reverseGeocodeLocation(latLng: LatLng): LocationResult = withContext(Dispatchers.IO) {
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            val addressName = if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) ?: "Ubicación desconocida"
            } else {
                "Dirección no encontrada"
            }

            LocationResult(
                addressName = addressName,
                latLng = latLng
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LocationResult(
                addressName = "Error al obtener la dirección",
                latLng = latLng
            )
        }
    }
}
