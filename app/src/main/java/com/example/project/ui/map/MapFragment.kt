package com.example.project.ui.map

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.project.R
import com.example.project.TrackingService
import com.example.project.Util.toFirebaseLatLngList
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var isMapReady = false
    private var isInitialZoomDone = false

    private var trackingService: TrackingService? = null
    private var isServiceBound = false

    private var currentLocation: Location? = null
    private var pathPoints = mutableListOf<LatLng>()
    private var startMarker: Marker? = null
    private var currentMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var pendingPolyline: List<LatLng>? = null
    private val directionsViewModel: DirectionsViewModel by viewModels()
    private lateinit var listView: ListView
    private lateinit var startButton: View
    private lateinit var updateButton: View
    private lateinit var completeButton: View

    private lateinit var sharedPreferences: SharedPreferences
    private val firestore = FirebaseFirestore.getInstance()
    private var driverID: String = ""

    private var mapData = MapData(
        name = "",
        routeInfoList = mutableListOf(
            RouteInfo(destination = ""),
            RouteInfo(destination = "")
        )
    )

    private val mapDataViewModel: MapDataViewModel by viewModels {
        MapDataViewModelFactory(MapDataRepository(MapDataDatabase.getDatabase(requireContext()).mapDataDao()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        driverID = sharedPreferences.getString("username", "") ?: ""
        Log.d("SharePreference", "DriverID: $driverID")
        fetchWorksheetData()
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkLocationPermissions()
        observeViewModel()

        listView = view.findViewById(R.id.ListView)
        startButton = view.findViewById(R.id.StartButton)
        updateButton = view.findViewById(R.id.UpdateButton)
        completeButton = view.findViewById(R.id.CompleteButton)
        populateRouteInfoTable()
        setupStartButton()
        setupUpdateButton()
        setupCompleteButton()
    }
    private fun fetchWorksheetData() {
        Log.d("Firestore", "Fetching worksheets from Firestore for driverId: $driverID")

        if (driverID.isEmpty()) {
            Log.e("Firestore", "DriverID is empty. Cannot fetch worksheet data.")
            return
        }

        firestore.collection("worksheets")
            .whereEqualTo("driverId", driverID) // Use username as driverID
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("Firestore", "No documents found for driverId: $driverID")
                } else {
                    for (document in result) {
                        val fetchedDriverID = document.getString("driverId") ?: ""
                        val driverName = document.getString("driverName") ?: ""
                        val to = document.getString("dropoffLocation") ?: ""
                        val from = document.getString("pickupLocation") ?: ""

                        // Log details for debugging
                        Log.d("Firestore", "DriverID: $fetchedDriverID")
                        Log.d("Firestore", "DriverName: $driverName")
                        Log.d("Firestore", "Route From: $from")
                        Log.d("Firestore", "Route To: $to")

                        // Build routeInfoList from 'from' and 'to'
                        val routeInfoList = mutableListOf<RouteInfo>().apply {
                            add(RouteInfo(destination = from))
                            add(RouteInfo(destination = to))
                        }

                        // Create a MapData object
                        mapData = MapData(
                            name = driverName,
                            routeInfoList = routeInfoList,
                        )

                        // Log the mapData object for further debugging
                        Log.d("Firestore", "MapData: $mapData")
                        Log.d("Firestore", "Worksheet data successfully fetched and displayed.")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching worksheet data: ${e.message}")
            }
    }


    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startTrackingService()
        }
    }

    private fun observeViewModel() {
        directionsViewModel.routePolyline.observe(viewLifecycleOwner) { polyline ->
            if (isMapReady) {
                drawRouteOnMap(polyline)
            } else {
                pendingPolyline = polyline
            }
        }
    }

    private fun startTrackingService() {
        val intent = Intent(requireContext(), TrackingService::class.java)
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        requireContext().startService(intent)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TrackingService.LocalBinder
            trackingService = binder.getService()
            isServiceBound = true
            observeLocationUpdates()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            isServiceBound = false
        }
    }

    private fun observeLocationUpdates() {
        trackingService?.locationLiveData?.observe(viewLifecycleOwner) { location ->
            location?.let {
                Log.d("MapFragment", "Observed location: ${it.latitude}, ${it.longitude}")
                onLocationUpdate(it)
            }
        }
    }

    private fun onLocationUpdate(location: Location) {
        currentLocation = location
        updateMapLocation()
        // Initialize map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this) // Asynchronously load the map
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isMapReady = true

        // Fetch current location and set up markers for the route
        fetchCurrentLocation { location ->
            if (location != null) {
                currentLocation = location

                val origin = "${currentLocation!!.latitude},${currentLocation!!.longitude}"

                // Assuming the route information is already populated in `mapData.routeInfoList`
                if (mapData.routeInfoList.isNotEmpty()) {
                    val waypoints = mapData.routeInfoList.dropLast(1).map { it.destination }
                    val destinationName = mapData.routeInfoList.last().destination
                    Log.d("MapFragment", "Waypoints: ${waypoints.joinToString(", ")}")
                    Log.d("MapFragment", "DestinationName: $destinationName")
                    if (waypoints.isEmpty() || destinationName.isEmpty()) {
                        Log.e("MapFragment", "Waypoints or destinationName are invalid. Skipping route resolution.")
                        return@fetchCurrentLocation
                    } else {
                        // Log waypoints and destinationName for debugging
                        Log.d("MapFragment", "Waypoints: ${waypoints.joinToString(", ")}")
                        Log.d("MapFragment", "DestinationName: $destinationName")
                    }
                    resolvePlacesToCoordinates(waypoints, destinationName) { resolvedWaypoints, resolvedDestination ->
                        if (resolvedWaypoints.isNotEmpty() && resolvedDestination != null) {
                            // Fetch route and draw polyline
                            directionsViewModel.fetchRoute(origin, resolvedDestination, resolvedWaypoints.joinToString("|"))
                            addMarkersForRoute(resolvedWaypoints, resolvedDestination)
                        } else {
                            Log.e("MapFragment", "Failed to resolve route details.")
                        }
                    }
                } else {
                    Log.e("MapFragment", "No route info available in mapData.")
                }
            } else {
                Log.e("MapFragment", "Failed to get current location.")
            }
        }
    }


    private fun fetchCurrentLocation(callback: (Location?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            callback(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            callback(location ?: null)
        }.addOnFailureListener {
            Log.e("MapFragment", "Failed to get location: ${it.message}")
            callback(null)
        }
    }

    private fun resolvePlacesToCoordinates(
        waypointNames: List<String>,
        destinationName: String,
        callback: (waypoints: List<String>, destination: String?) -> Unit
    ) {
        if (waypointNames.isEmpty() || destinationName.isEmpty()) {
            Log.e("MapFragment", "Invalid input: waypoints or destination is empty")
            callback(emptyList(), null)
            return
        }

        val resolvedWaypoints = mutableListOf<String>()
        var resolvedDestination: String? = null
        var resolvedCount = 0
        val totalToResolve = waypointNames.size + 1

        waypointNames.forEach { placeName ->
            if (placeName.isEmpty()) {
                Log.e("MapFragment", "Empty waypoint name provided")
                resolvedCount++
                if (resolvedCount == totalToResolve) {
                    callback(resolvedWaypoints, resolvedDestination)
                }
                return@forEach
            }

            directionsViewModel.getLatLngForPlace(placeName) { latLng ->
                if (latLng != null) {
                    resolvedWaypoints.add("${latLng.latitude},${latLng.longitude}")
                    Log.d("MapFragment", "Resolved waypoint: $placeName -> $latLng")
                } else {
                    Log.e("MapFragment", "Failed to resolve waypoint: $placeName")
                }
                resolvedCount++
                if (resolvedCount == totalToResolve) {
                    callback(resolvedWaypoints, resolvedDestination)
                }
            }
        }

        directionsViewModel.getLatLngForPlace(destinationName) { latLng ->
            if (latLng != null) {
                resolvedDestination = "${latLng.latitude},${latLng.longitude}"
                Log.d("MapFragment", "Resolved destination: $destinationName -> $latLng")
            } else {
                Log.e("MapFragment", "Failed to resolve destination: $destinationName")
            }
            resolvedCount++
            if (resolvedCount == totalToResolve) {
                callback(resolvedWaypoints, resolvedDestination)
            }
        }
    }


    private fun updateMapLocation() {
        currentLocation?.let { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            pathPoints.add(latLng)

            mMap.addPolyline(
                PolylineOptions()
                    .addAll(pathPoints)
                    .color(Color.BLUE)
                    .width(5f)
            )

            if (startMarker == null) {
                startMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Starting Point")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
            }

            currentMarker?.remove()
            currentMarker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            if (!isInitialZoomDone) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                isInitialZoomDone = true
            }
        }
    }

    private fun drawRouteOnMap(decodedPolyline: List<LatLng>) {
        if (!isMapReady) {
            Log.w("MapFragment", "Map is not ready. Storing polyline for later.")
            pendingPolyline = decodedPolyline
            return
        }
        mMap.addPolyline(
            PolylineOptions()
                .addAll(decodedPolyline)
                .color(Color.GREEN)
                .width(8f)
        )

        if (decodedPolyline.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            decodedPolyline.forEach { boundsBuilder.include(it) }
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
        }
    }

    private fun addMarkersForRoute(waypoints: List<String>, destination: String) {
        waypoints.forEachIndexed { index, waypoint ->
            val waypointLatLng = waypoint.split(",").let {
                LatLng(it[0].toDouble(), it[1].toDouble())
            }
            mMap.addMarker(
                MarkerOptions()
                    .position(waypointLatLng)
                    .title("Waypoint ${index + 1}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )
        }

        val destinationLatLng = destination.split(",").let {
            LatLng(it[0].toDouble(), it[1].toDouble())
        }
        destinationMarker?.remove()
        destinationMarker = mMap.addMarker(
            MarkerOptions()
                .position(destinationLatLng)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
    }

    private fun populateRouteInfoTable() {
        // Convert the data into displayable strings for the ListView
        val displayData = mapData.routeInfoList.map { routeInfo ->
            "${routeInfo.destination}\n" +
                    "Departure: ${routeInfo.departureTime}, ETA: ${routeInfo.estimatedArrivalTime}, " +
                    "Arrival: ${routeInfo.arrivalTime}, Status: ${routeInfo.status}"
        }

        // Set the adapter for the ListView
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, displayData)
        listView.adapter = adapter
    }


    private fun setupStartButton() {
        startButton.setOnClickListener {
            // Handle only the first item for the Start button
            val firstItem = mapData.routeInfoList.firstOrNull() ?: return@setOnClickListener

            if (firstItem.status == "Not Started") {
                // Set Departure Time
                val departureTime = System.currentTimeMillis().let { time ->
                    android.text.format.DateFormat.format("hh:mm a", time).toString()
                }
                firstItem.departureTime = departureTime
                firstItem.status = "Ongoing"

                // Fetch and set ETA
                currentLocation?.let { location ->
                    val origin = "${location.latitude},${location.longitude}"
                    directionsViewModel.getEstimatedArrivalTime(
                        origin = origin,
                        destination = firstItem.destination
                    ) { estimatedTime ->
                        firstItem.estimatedArrivalTime = estimatedTime ?: "Unavailable"
                        populateRouteInfoTable()
                    }
                } ?: run {
                    firstItem.estimatedArrivalTime = "Unavailable"
                }

                // Update the table
                populateRouteInfoTable()
            }
        }
    }

    private fun setupUpdateButton() {
        updateButton.setOnClickListener {
            // Find the ongoing route
            val ongoingIndex = mapData.routeInfoList.indexOfFirst { it.status == "Ongoing" }
            if (ongoingIndex == -1) {
                Log.d("UpdateButton", "No ongoing route to update.")
                return@setOnClickListener
            }

            val ongoingRoute = mapData.routeInfoList[ongoingIndex]

            // Show a dialog for Delivered or Unable to Deliver
            AlertDialog.Builder(requireContext())
                .setTitle("Update Route")
                .setMessage("Select the delivery status:")
                .setPositiveButton("Delivered") { _, _ ->
                    // Update Arrival Time and Status for Delivered
                    val arrivalTime = System.currentTimeMillis().let { time ->
                        android.text.format.DateFormat.format("hh:mm a", time).toString()
                    }
                    ongoingRoute.arrivalTime = arrivalTime
                    ongoingRoute.status = "Completed"

                    // Move to the next route if available
                    if (ongoingIndex + 1 < mapData.routeInfoList.size) {
                        val nextRoute = mapData.routeInfoList[ongoingIndex + 1]
                        nextRoute.status = "Ongoing"

                        // Set Departure Time
                        val departureTime = System.currentTimeMillis().let { time ->
                            android.text.format.DateFormat.format("hh:mm a", time).toString()
                        }
                        nextRoute.departureTime = departureTime

                        // Fetch and set ETA for the next destination
                        currentLocation?.let { location ->
                            val origin = "${location.latitude},${location.longitude}"
                            directionsViewModel.getEstimatedArrivalTime(
                                origin = origin,
                                destination = nextRoute.destination
                            ) { estimatedTime ->
                                nextRoute.estimatedArrivalTime = estimatedTime ?: "Unavailable"
                                populateRouteInfoTable()
                            }
                        } ?: run {
                            nextRoute.estimatedArrivalTime = "Unavailable"
                        }
                    }

                    // Update the table
                    populateRouteInfoTable()
                }
                .setNegativeButton("Unable to Deliver") { _, _ ->
                    // Update Arrival Time and Status for Unable to Deliver
                    ongoingRoute.arrivalTime = "N/A"
                    ongoingRoute.status = "Unable to Deliver"

                    // Move to the next route if available
                    if (ongoingIndex + 1 < mapData.routeInfoList.size) {
                        val nextRoute = mapData.routeInfoList[ongoingIndex + 1]
                        nextRoute.status = "Ongoing"

                        // Set Departure Time
                        val departureTime = System.currentTimeMillis().let { time ->
                            android.text.format.DateFormat.format("hh:mm a", time).toString()
                        }
                        nextRoute.departureTime = departureTime

                        // Fetch and set ETA for the next destination
                        currentLocation?.let { location ->
                            val origin = "${location.latitude},${location.longitude}"
                            directionsViewModel.getEstimatedArrivalTime(
                                origin = origin,
                                destination = nextRoute.destination
                            ) { estimatedTime ->
                                nextRoute.estimatedArrivalTime = estimatedTime ?: "Unavailable"
                                populateRouteInfoTable()
                            }
                        } ?: run {
                            nextRoute.estimatedArrivalTime = "Unavailable"
                        }
                    }

                    // Update the table
                    populateRouteInfoTable()
                }
                .show()
        }
    }
    private fun setupCompleteButton() {
        completeButton.setOnClickListener {
            // Finalize mapData
            mapData = mapData.copy(pathPoints = pathPoints.toFirebaseLatLngList())

            lifecycleScope.launch(Dispatchers.IO) {
                mapData?.let {
                    it.pathPoints = pathPoints.toFirebaseLatLngList()
                    mapDataViewModel.insertMapData(it)
                }
            }
            val firebaseDatabase = Firebase.database
            val databaseReference = firebaseDatabase.reference.child("routes")
            val firebaseData = mapOf(
                "id" to mapData.id,
                "name" to mapData.name,
                "pathPoints" to pathPoints.map { mapOf("lat" to it.latitude, "lng" to it.longitude) },
                "routes" to mapData.routeInfoList.map { routeInfo ->
                    mapOf(
                        "destination" to routeInfo.destination,
                        "departureTime" to routeInfo.departureTime,
                        "estimatedArrivalTime" to routeInfo.estimatedArrivalTime,
                        "arrivalTime" to routeInfo.arrivalTime,
                        "status" to routeInfo.status
                    )
                }
            )
            // Show confirmation dialog
            databaseReference.push().setValue(firebaseData)
                .addOnSuccessListener {
                    // Show confirmation dialog
                    AlertDialog.Builder(requireContext())
                        .setTitle("Complete")
                        .setMessage("Route information saved to database and uploaded to Firebase successfully!")
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
                }
                .addOnFailureListener { error ->
                    // Show error dialog
                    AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Failed to upload data to Firebase: ${error.message}")
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            requireContext().unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

    }
}
