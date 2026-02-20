package com.example.signinregister.ui.map

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.signinregister.R
import com.example.signinregister.data.remote.Vehicle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    private val window: View = LayoutInflater.from(context).inflate(R.layout.vehicle_info_window, null)

    private fun render(marker: Marker, view: View) {
        val vehicle = marker.tag as? Vehicle

        val driverTextView = view.findViewById<TextView>(R.id.carDriverIW)
        val plateTextView = view.findViewById<TextView>(R.id.carPlateIW)
        val rateTextView = view.findViewById<TextView>(R.id.carRateIW)

        if (vehicle != null) {
            val profileDrawable = ContextCompat.getDrawable(context, R.drawable.ic_profile)
            driverTextView.setCompoundDrawablesWithIntrinsicBounds(profileDrawable, null, null, null)
            driverTextView.text = vehicle.driverName

            val plateDrawable = ContextCompat.getDrawable(context, R.drawable.ic_licenseplate)
            plateTextView.setCompoundDrawablesWithIntrinsicBounds(plateDrawable, null, null, null)
            plateTextView.text = vehicle.plate

            val starDrawable = ContextCompat.getDrawable(context, R.drawable.ic_star)
            if (starDrawable != null) {
                val colorYellow = ContextCompat.getColor(context, R.color.yellow)
                starDrawable.setColorFilter(colorYellow, PorterDuff.Mode.SRC_IN)
            }
            rateTextView.setCompoundDrawablesWithIntrinsicBounds(starDrawable, null, null, null)
            rateTextView.text = String.format("%.1f", vehicle.rating)

        } else {
            driverTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            plateTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            rateTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

            driverTextView.text = "Error"
            plateTextView.text = "Datos no disponibles"
            rateTextView.text = ""
        }
    }

    override fun getInfoWindow(marker: Marker): View? {
        render(marker, window)
        return window
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }
}