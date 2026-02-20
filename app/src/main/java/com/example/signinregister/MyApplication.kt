package com.example.signinregister

import android.app.Application
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val apiKey = getString(R.string.google_maps_key)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
    }
}
