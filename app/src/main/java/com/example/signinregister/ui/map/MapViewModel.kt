package com.example.signinregister.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signinregister.data.LocationTracker
import com.example.signinregister.domain.LocationResult
import com.example.signinregister.domain.repository.PlacesRepository
import com.example.signinregister.domain.repository.RouteRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationTracker: LocationTracker,
    private val placesRepository: PlacesRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _userLocation = MutableLiveData<LatLng>()
    val userLocation: LiveData<LatLng> = _userLocation

    private val _reverseGeocodeResult = MutableLiveData<LocationResult>()
    val reverseGeocodeResult: LiveData<LocationResult> = _reverseGeocodeResult

    private val _routePolyline = MutableLiveData<List<LatLng>>()
    val routePolyline: LiveData<List<LatLng>> = _routePolyline

    fun fetchLastKnownLocation() {
        viewModelScope.launch {
            val location = locationTracker.getCurrentLocation()
            location?.let {
                _userLocation.value = LatLng(it.latitude, it.longitude)
            }
        }
    }

    fun reverseGeocodeLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val latLng = LatLng(latitude, longitude)
                    val result = placesRepository.reverseGeocodeLocation(latLng)
                    _reverseGeocodeResult.postValue(result)
                } catch (e: Exception) {
                    val errorResult = LocationResult(
                        addressName = "Error de conexión o geocodificación",
                        latLng = LatLng(latitude, longitude)
                    )
                    _reverseGeocodeResult.postValue(errorResult)
                }
            }
        }
    }

    fun fetchRoute(origin: LatLng, destination: LatLng) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    Log.d("RUTA_VM_DEBUG", "Llamando a getRoutePolyline...")
                    val encodedPolyline = routeRepository.getRoutePolyline(origin, destination)

                    val decodedPoints = decodePolyline(encodedPolyline)

                    Log.d("RUTA_POLYLINE_POINTS", "Puntos decodificados: ${decodedPoints.size}")
                    _routePolyline.postValue(decodedPoints)

                } catch (e: Exception) {
                    e.printStackTrace()

                    Log.e("RUTA_CRASH", "Excepción al obtener ruta: ${e.message}", e)
                    _routePolyline.postValue(emptyList())
                }
            }
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }
}