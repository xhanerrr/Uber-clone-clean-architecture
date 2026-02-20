package com.example.signinregister.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.signinregister.R
import com.example.signinregister.databinding.ActivityMapBinding
import com.example.signinregister.data.remote.Vehicle // Importación del data class Vehicle
import com.example.signinregister.domain.LocationResult
import com.example.signinregister.ui.map.searchextension.BottomDialogExtension
import com.example.signinregister.ui.map.searchextension.LocationSelectedListener
import com.example.signinregister.ui.map.searchextension.SearchField
import com.example.signinregister.ui.map.vehiclelist.OnVehicleClickListener
import com.example.signinregister.ui.map.vehiclelist.VehicleAdapter
import com.example.signinregister.ui.map.vehiclelist.VehicleViewModel
import com.example.signinregister.ui.map.driverinfo.DriverInfoActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import androidx.core.view.isVisible
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.model.Marker
import kotlin.math.max

private const val UI_ANIMATION_DURATION = 300L
private const val LOCATION_PERMISSION_REQUEST_CODE = 1
private const val DEBUG_TAG = "MAP_DEBUG"
private const val DEFAULT_SELECTION_TEXT = "Seleccionar ubicación..."
private const val SLIDE_THRESHOLD = 0.05f
private const val REQUEST_CHECK_SETTINGS = 1001

@AndroidEntryPoint
class MapActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnCameraMoveStartedListener,
    GoogleMap.OnCameraIdleListener,
    LocationSelectedListener,
    OnVehicleClickListener,
    OnInfoWindowClickListener {

    private val mapViewModel: MapViewModel by viewModels()
    private val viewModel: VehicleViewModel by viewModels()

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var vehicleAdapter: VehicleAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var hasMapCenteredOnUser = false

    private var currentSearchField: SearchField? = null

    private var originLocation: LocationResult? = null
    private var destinationLocation: LocationResult? = null

    private var isUserInteractingWithMap = true
    private var isDialogShowing = false
    private var isLaunchingDialog = false

    private var currentRoutePolyline: Polyline? = null

    private var lastKnownUserLocation: LatLng? = null

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_HIDDEN -> {
                    isLaunchingDialog = true
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    bottomSheet.alpha = 1.0f
                    isLaunchingDialog = false
                    isDialogShowing = false
                }
                else -> {}
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            bottomSheet.alpha = max(0.0f, slideOffset)

            if (slideOffset < SLIDE_THRESHOLD) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            else if (slideOffset > SLIDE_THRESHOLD && bottomSheetBehavior.state != BottomSheetBehavior.STATE_DRAGGING) {
                if (!isDialogShowing) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupListeners()
        initMap()
        initRecyclerView()
        observeViewModel()
        setupPersistentBottomSheet()

        currentSearchField = SearchField.FROM

        binding.mapPinImage.visibility = View.VISIBLE

        binding.ubicationButton.setImageResource(R.drawable.ic_actualubimark)
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun initRecyclerView() {
        vehicleAdapter = VehicleAdapter(this)
        binding.carsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MapActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = vehicleAdapter
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnCameraMoveStartedListener(this)
        mMap.setOnCameraIdleListener(this)
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnInfoWindowClickListener(this)

        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))

        checkLocationPermissionAndFetch()
        viewModel.loadVehicles()

        if (originLocation == null && destinationLocation == null) {
            checkLocationSettings()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    if (!hasMapCenteredOnUser) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        hasMapCenteredOnUser = true
                        lastKnownUserLocation = latLng
                        binding.ubicationButton.setImageResource(R.drawable.ic_actualubi)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (::mMap.isInitialized) checkLocationPermissionAndFetch()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_LONG).show()
                binding.ubicationButton.setImageResource(R.drawable.ic_actualubimark)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                binding.ubicationButton.setImageResource(R.drawable.ic_actualubi)
                mapViewModel.fetchLastKnownLocation()
                Toast.makeText(this, "Ubicación activada", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    binding.ubicationButton.performClick()
                }, 2000)

            } else {
                Toast.makeText(this, "La ubicación debe estar activada para centrar el mapa.", Toast.LENGTH_SHORT).show()
                binding.ubicationButton.setImageResource(R.drawable.ic_actualubimark)
            }
        }
    }

    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false
            mapViewModel.fetchLastKnownLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun observeViewModel() {
        mapViewModel.userLocation.observe(this) { latLng ->
            lastKnownUserLocation = latLng

            if (::mMap.isInitialized && (originLocation == null && destinationLocation == null)) {
                if (!hasMapCenteredOnUser) {
                    isUserInteractingWithMap = false
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    hasMapCenteredOnUser = true
                }

                binding.ubicationButton.setImageResource(R.drawable.ic_actualubi)
            }
        }

        viewModel.vehicles.observe(this) { vehicles ->
            if (::vehicleAdapter.isInitialized) {
                vehicleAdapter.submitList(vehicles)
            }

            if (::mMap.isInitialized) {
                mMap.clear()

                vehicles.forEach { vehicle ->
                    val carLocation = LatLng(vehicle.currentLat, vehicle.currentLng)

                    val carTitle = "${vehicle.driverName} - ${vehicle.brand} ${vehicle.model}"

                    val p0 = mMap.addMarker(
                        MarkerOptions()
                            .position(carLocation)
                            .title(carTitle)
                            .snippet("Toca para ver detalles.")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                    )

                    p0?.tag = vehicle
                }

                mapViewModel.routePolyline.value?.let { points ->
                    if (points.isNotEmpty()) {
                        drawRoutePolyline(points, redraw = true)
                    }
                }
            }
        }

        mapViewModel.reverseGeocodeResult.observe(this) { locationResult ->
            when (currentSearchField) {
                SearchField.FROM -> {
                    binding.textView4.text = locationResult.addressName
                    originLocation = locationResult
                }
                SearchField.TO -> {
                    binding.textView5.text = locationResult.addressName
                    destinationLocation = locationResult
                }
                null -> {}
            }

            if (originLocation != null && destinationLocation != null) {
                binding.mapPinImage.visibility = View.GONE
                Log.d(DEBUG_TAG, "Ambas ubicaciones seleccionadas por mapa. Iniciando búsqueda de ruta.")
                mapViewModel.fetchRoute(originLocation!!.latLng, destinationLocation!!.latLng)
            }
        }

        mapViewModel.routePolyline.observe(this) { polylinePoints ->
            if (::mMap.isInitialized && polylinePoints.isNotEmpty()) {
                drawRoutePolyline(polylinePoints)
            } else if (polylinePoints.isEmpty()) {
                currentRoutePolyline?.remove()
                currentRoutePolyline = null
                if (originLocation != null && destinationLocation != null) {
                    Toast.makeText(this, "No se encontró una ruta válida.", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                Timber.e(it)
            }
        }
    }

    override fun onCameraMoveStarted(reason: Int) {
        isUserInteractingWithMap = reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE

        binding.persistentBottomSheet.animate()
            .alpha(0f)
            .translationY(binding.persistentBottomSheet.height.toFloat())
            .setDuration(UI_ANIMATION_DURATION)
            .withEndAction { binding.persistentBottomSheet.visibility = View.INVISIBLE }
    }

    override fun onCameraIdle() {
        if (!isUserInteractingWithMap) {
            isUserInteractingWithMap = true
            return
        }

        if (currentSearchField != null && binding.mapPinImage.isVisible) {
            val centerLatLng = mMap.cameraPosition.target
            mapViewModel.reverseGeocodeLocation(centerLatLng.latitude, centerLatLng.longitude)

            binding.persistentBottomSheet.apply {
                visibility = View.VISIBLE
                animate().alpha(1f).translationY(0f).setDuration(UI_ANIMATION_DURATION)
            }

            val loadingText = "Buscando ubicación..."
            when (currentSearchField) {
                SearchField.FROM -> binding.textView4.text = loadingText
                SearchField.TO -> binding.textView5.text = loadingText
                null -> {}
            }
        }
    }

    private fun setupPersistentBottomSheet() {
        val bottomSheet = binding.persistentBottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.skipCollapsed = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
    }

    private fun handleLocationCentering() {
        if (lastKnownUserLocation == null) {
            mapViewModel.fetchLastKnownLocation()
            Toast.makeText(this, "Buscando tu ubicación...", Toast.LENGTH_SHORT).show()
            binding.ubicationButton.setImageResource(R.drawable.ic_actualubimark)
            return
        }

        lastKnownUserLocation?.let { latLng ->
            isUserInteractingWithMap = false
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            binding.ubicationButton.setImageResource(R.drawable.ic_actualubi)
        }
    }

    private fun checkLocationSettings() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permite el acceso a la ubicación en los ajustes de la App.", Toast.LENGTH_LONG).show()
            binding.ubicationButton.setImageResource(R.drawable.ic_actualubimark)
            return
        }

        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            handleLocationCentering()
        }.addOnFailureListener { exception ->
            if (exception is com.google.android.gms.common.api.ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            } else {
                Toast.makeText(this, "Tu ubicación no está disponible (Falla GPS).", Toast.LENGTH_SHORT).show()
                binding.ubicationButton.setImageResource(R.drawable.ic_actualubimark)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        binding.resetButton.setOnClickListener { resetSelection() }

        binding.mapTypeButton.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.map_type_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.map_type_normal -> changeMapType(GoogleMap.MAP_TYPE_NORMAL)
                    R.id.map_type_satellite -> changeMapType(GoogleMap.MAP_TYPE_SATELLITE)
                    R.id.map_type_hybrid -> changeMapType(GoogleMap.MAP_TYPE_HYBRID)
                    R.id.map_type_terrain -> changeMapType(GoogleMap.MAP_TYPE_TERRAIN)
                    else -> false
                }
                true
            }
            popup.show()
        }

        binding.carsRecyclerView.setOnTouchListener { _, _ -> false }

        binding.ubicationButton.setOnClickListener {
            if (originLocation != null && destinationLocation != null && currentRoutePolyline != null) {

                val points = currentRoutePolyline?.points
                if (points != null && points.isNotEmpty()) {
                    val bounds = LatLngBounds.Builder()
                    points.forEach { bounds.include(it) }
                    val padding = 150
                    isUserInteractingWithMap = false
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), padding))
                }

            } else {
                checkLocationSettings()
            }
        }

        binding.textView4.setOnClickListener {
            currentSearchField = SearchField.FROM
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

            binding.persistentBottomSheet.post {
                startSearchDialog()
            }
        }

        binding.textView5.setOnClickListener {
            currentSearchField = SearchField.TO
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

            binding.persistentBottomSheet.post {
                startSearchDialog()
            }
        }

        binding.cardView2.setOnClickListener {
            currentSearchField = SearchField.FROM
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

            binding.persistentBottomSheet.post {
                startSearchDialog()
            }
        }
    }

    private fun startSearchDialog() {
        isDialogShowing = true

        val currentTv = binding.textView4
        val secondaryTv = binding.textView5

        val searchField = currentSearchField ?: SearchField.FROM

        val currentAddress = currentTv.text.toString()
        val secondaryAddress = secondaryTv.text.toString()

        BottomDialogExtension.newInstance(searchField, currentAddress, secondaryAddress)
            .show(supportFragmentManager, BottomDialogExtension.TAG)
    }

    override fun onLocationSelected(result: LocationResult) {
        if (!::mMap.isInitialized) return

        when (currentSearchField) {
            SearchField.FROM -> {
                binding.textView4.text = result.addressName
                originLocation = result
            }
            SearchField.TO -> {
                binding.textView5.text = result.addressName
                destinationLocation = result
            }
            null -> {
                binding.textView4.text = result.addressName
                originLocation = result
            }
        }

        if (originLocation != null && destinationLocation != null) {
            binding.mapPinImage.visibility = View.GONE
            mapViewModel.fetchRoute(originLocation!!.latLng, destinationLocation!!.latLng)
        } else {
            binding.mapPinImage.visibility = View.VISIBLE
            binding.mapPinImage.setImageResource(R.drawable.ic_map_pin)
            isUserInteractingWithMap = false
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(result.latLng, 16f))
        }
    }

    override fun onSearchDialogDismissed() {
        isDialogShowing = false
        isLaunchingDialog = false

        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)

        binding.persistentBottomSheet.alpha = 1.0f
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.persistentBottomSheet.postDelayed({
            bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        }, 200)

        binding.persistentBottomSheet.animate()
            .translationY(0f)
            .setDuration(UI_ANIMATION_DURATION)
            .start()
    }

    private fun resetSelection() {
        currentRoutePolyline?.remove()
        currentRoutePolyline = null
        originLocation = null
        destinationLocation = null

        binding.textView4.text = DEFAULT_SELECTION_TEXT
        binding.textView5.text = DEFAULT_SELECTION_TEXT

        currentSearchField = SearchField.FROM

        binding.mapPinImage.visibility = View.VISIBLE
        binding.mapPinImage.setImageResource(R.drawable.ic_map_pin)

        if (lastKnownUserLocation != null) {
            binding.ubicationButton.setImageResource(R.drawable.ic_actualubi)
        } else {
            binding.ubicationButton.setImageResource(R.drawable.ic_actualubimark)
        }

        lastKnownUserLocation?.let { latLng ->
            isUserInteractingWithMap = false
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    override fun onVehicleClicked(latitude: Double, longitude: Double) {
        if (::mMap.isInitialized) {
            val selectedLocation = LatLng(latitude, longitude)

            isUserInteractingWithMap = false

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 16f))
            binding.mapPinImage.animate()
                .translationY(0f)
                .setDuration(200)
                .withEndAction {
                    binding.mapPinImage.animate()
                        .translationY((-20f).dpToPx())
                        .setDuration(200)
                }
        }
    }

    private fun Float.dpToPx(): Float {
        return this * resources.displayMetrics.density
    }

    private fun drawRoutePolyline(points: List<LatLng>, redraw: Boolean = false) {
        currentRoutePolyline?.remove()
        val polylineOptions = PolylineOptions()
            .addAll(points)
            .width(15f)
            .color(ContextCompat.getColor(this, R.color.blue))
            .geodesic(true)
        currentRoutePolyline = mMap.addPolyline(polylineOptions)

        val bounds = LatLngBounds.Builder()
        points.forEach { bounds.include(it) }
        val padding = 150
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), padding))

        binding.mapPinImage.visibility = View.GONE
    }

    override fun onInfoWindowClick(p0: Marker) {

        val vehicle = p0.tag as? Vehicle
        val vehicleId = vehicle?.id

        if (vehicleId != null){
            val intent = Intent(this, DriverInfoActivity::class.java).apply {
                putExtra("VEHICLE_ID", vehicleId)
            }
            startActivity(intent)
        } else{
            Toast.makeText(this,"Error, Intentelo de nuevo mas tarde.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeMapType(type: Int) {
        if (::mMap.isInitialized) {
            mMap.mapType = type
        }
    }
}