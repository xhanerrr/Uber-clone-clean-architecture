package com.example.signinregister.ui.map.searchextension

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signinregister.domain.AddressSuggestion
import com.example.signinregister.domain.LocationResult
import com.example.signinregister.domain.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BottomExtensionViewModel @Inject constructor(
    private val placesRepository: PlacesRepository
) : ViewModel() {

    private val _suggestions = MutableStateFlow<List<AddressSuggestion>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private val _locationResult = MutableStateFlow<LocationResult?>(null)
    val locationResult = _locationResult.asStateFlow()

    private var searchJob: Job? = null
    private val DEBUG_TAG = "VM_ERROR_DIAGNOSIS"

    fun searchAddress(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _suggestions.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500L)

            try {
                val result = placesRepository.getAutocompleteSuggestions(query)
                _suggestions.value = result
            } catch (e: Exception) {
                Log.e(DEBUG_TAG, "Fallo de API o red después de debounce: ${e.message}", e)
                _suggestions.value = emptyList()
            }
        }
    }

    fun getPlaceDetails(suggestion: AddressSuggestion) {
        viewModelScope.launch {
            try {
                val result = placesRepository.fetchPlaceCoordinates(
                    suggestion.placeId,
                    suggestion.mainText
                )
                _locationResult.value = result
            } catch (e: Exception) {
                Log.e(DEBUG_TAG, "Fallo al obtener detalles del lugar: ${e.message}", e)
            }
        }
    }

    fun clearLocationResult() {
        _locationResult.value = null
    }
}
