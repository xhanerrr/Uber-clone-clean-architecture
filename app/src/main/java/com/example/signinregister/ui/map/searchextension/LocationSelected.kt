package com.example.signinregister.ui.map.searchextension

import com.example.signinregister.domain.LocationResult

interface LocationSelectedListener {
    fun onLocationSelected(result: LocationResult)
    fun onSearchDialogDismissed()

}