package com.example.project.ui.drivermanager

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.example.project.R
import com.example.project.Util.toLatLng
import com.example.project.ui.map.FirebaseLatLng
import com.example.project.ui.map.MapData
import com.example.project.ui.map.MapDataDatabase
import com.example.project.ui.map.MapDataRepository
import com.example.project.ui.map.MapDataViewModel
import com.example.project.ui.map.MapDataViewModelFactory
import com.example.project.ui.map.RouteInfo
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DriverRouteActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var driverId: Long = -1
    private lateinit var deleteButton: Button
    private lateinit var routeListView: ListView
    private val routeInfoList = mutableListOf<String>()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var cancelButton: Button

    private val mapDataViewModel: MapDataViewModel by viewModels {
        MapDataViewModelFactory(MapDataRepository(MapDataDatabase.getDatabase(this).mapDataDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_route)

        // Initialize the views
        deleteButton = findViewById(R.id.DeleteButton)
        routeListView = findViewById(R.id.ListView)
        cancelButton = findViewById(R.id.CancelButton)

        // Get the driver ID passed via Intent
        driverId = intent.getLongExtra("driverId", -1)

        // Setup the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val sharedPreferences: SharedPreferences =
            getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val accountType = sharedPreferences.getInt("accountType", -1)

        // Fetch the driver route data
        if (accountType == 0) {
            fetchDriverDataFromFirebase()
        } else if (accountType == 1) {
            fetchDriverDataFromLocalDatabase()
        }

        // Set up delete button click
        deleteButton.setOnClickListener {
            deleteDriverData()
        }
        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchDriverDataFromLocalDatabase() {
        lifecycleScope.launch {
            val mapData = withContext(Dispatchers.IO) {
                mapDataViewModel.getMapDataById(driverId)
            }
            mapData?.let {
                populateRouteList(it)
                drawRouteOnMap(it)
            } ?: run {
                // Handle case where no data is found
                routeInfoList.clear()
                routeInfoList.add("No route data found for this driver.")
                updateRouteListView()
            }
        }
    }

    private fun fetchDriverDataFromFirebase() {
        val driversRef = firebaseDatabase.reference.child("routes").orderByChild("id").equalTo(driverId.toDouble())

        driversRef.get().addOnSuccessListener { snapshot ->
            Log.d("FirebaseData", "Query executed for driverId: $driverId")
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    val mapDataSnapshot = child.value as? Map<String, Any>
                    if (mapDataSnapshot != null) {
                        val mapData = parseMapData(mapDataSnapshot)
                        if (mapData != null) {
                            populateRouteList(mapData)
                            drawRouteOnMap(mapData)
                        } else {
                            Log.e("FirebaseData", "Failed to parse MapData.")
                            showNoDataFound()
                        }
                    } else {
                        Log.e("FirebaseData", "Snapshot is null or not a valid Map.")
                        showNoDataFound()
                    }
                }
            } else {
                Log.d("FirebaseData", "No matching data found for driverId: $driverId")
                showNoDataFound()
            }
        }.addOnFailureListener { error ->
            Log.e("FirebaseData", "Error fetching data from Firebase: ${error.message}")
            routeInfoList.clear()
            routeInfoList.add("Error fetching data from Firebase.")
            updateRouteListView()
        }
    }

    private fun parseMapData(snapshot: Map<String, Any>): MapData? {
        return try {
            val id = (snapshot["id"] as? Number)?.toLong() ?: 0L
            val name = snapshot["name"] as? String ?: ""
            val routesRaw = snapshot["routes"] as? List<Map<String, Any>>
            Log.d("FirebaseData", "Routes raw data: $routesRaw")

            val routes = routesRaw?.map {
                RouteInfo(
                    destination = it["destination"] as String,
                    departureTime = it["departureTime"] as String,
                    estimatedArrivalTime = it["estimatedArrivalTime"] as String,
                    arrivalTime = it["arrivalTime"] as String,
                    status = it["status"] as String
                )
            }?.toMutableList() ?: mutableListOf()
            val pathPoints = (snapshot["pathPoints"] as List<Map<String, Any>>).map {
                FirebaseLatLng(
                    latitude = (it["lat"] as Number).toDouble(),
                    longitude = (it["lng"] as Number).toDouble()
                )
            }
            Log.d("FirebaseData", "Parsed Routes: $routes")
            MapData(id = id, name = name,  routeInfoList = routes, pathPoints = pathPoints)
        } catch (e: Exception) {
            Log.e("FirebaseData", "Error parsing MapData: ${e.message}")
            null
        }
    }

    private fun showNoDataFound() {
        routeInfoList.clear()
        routeInfoList.add("No route data found for this driver.")
        updateRouteListView()
    }

    private fun populateRouteList(mapData: MapData) {
        // Convert route information into a displayable format
        routeInfoList.clear()
        mapData.routeInfoList.forEach { routeInfo ->
            val info = "Destination: ${routeInfo.destination}\n" +
                    "Departure: ${routeInfo.departureTime}\n" +
                    "ETA: ${routeInfo.estimatedArrivalTime}\n" +
                    "Arrival: ${routeInfo.arrivalTime}\n" +
                    "Status: ${routeInfo.status}"
            routeInfoList.add(info)
        }
        updateRouteListView()
    }

    private fun updateRouteListView() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, routeInfoList)
        routeListView.adapter = adapter
    }

    private fun drawRouteOnMap(mapData: MapData) {
        if (mapData.pathPoints.isNotEmpty()) {
            // Convert FirebaseLatLng to LatLng for Google Maps
            val latLngPoints = mapData.pathPoints.map { it.toLatLng() }

            // Draw polyline
            val polylineOptions = PolylineOptions().addAll(latLngPoints).color(android.graphics.Color.BLUE).width(5f)
            mMap.addPolyline(polylineOptions)

            // Add markers for start and end points
            val startPoint = latLngPoints.first()
            val endPoint = latLngPoints.last()

            mMap.addMarker(
                MarkerOptions()
                    .position(startPoint)
                    .title("Start")
            )
            mMap.addMarker(
                MarkerOptions()
                    .position(endPoint)
                    .title("End")
            )

            // Move camera to start point
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 12f))
        }
    }


    private fun deleteDriverData() {
        lifecycleScope.launch(Dispatchers.IO) {
            mapDataViewModel.deleteMapDataById(driverId)
            withContext(Dispatchers.Main) {
                finish()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
}
