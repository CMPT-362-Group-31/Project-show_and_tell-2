package com.example.project.ui.drivermanager

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.project.R
import com.example.project.ui.map.MapData
import com.example.project.ui.map.MapDataDatabase
import com.example.project.ui.map.MapDataRepository
import com.example.project.ui.map.MapDataViewModel
import com.example.project.ui.map.MapDataViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
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

    private val mapDataViewModel: MapDataViewModel by viewModels {
        MapDataViewModelFactory(MapDataRepository(MapDataDatabase.getDatabase(this).mapDataDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_route)

        // Initialize the views
        deleteButton = findViewById(R.id.DeleteButton)
        routeListView = findViewById(R.id.ListView)

        // Get the driver ID passed via Intent
        driverId = intent.getLongExtra("driverId", -1)

        // Setup the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Fetch the driver route data
        fetchDriverData()

        // Set up delete button click
        deleteButton.setOnClickListener {
            deleteDriverData()
        }
    }

    private fun fetchDriverData() {
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
            val polylineOptions = PolylineOptions().color(android.graphics.Color.BLUE).width(5f)

            // Add all points to the polyline
            mapData.pathPoints.forEach { latLng ->
                polylineOptions.add(latLng)
            }

            // Draw the polyline
            mMap.addPolyline(polylineOptions)

            // Add markers for the start and end points
            val startPoint = mapData.pathPoints.first()
            val endPoint = mapData.pathPoints.last()

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

            // Move the camera to the start point
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
