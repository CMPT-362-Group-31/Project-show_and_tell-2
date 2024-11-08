package com.example.project.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this) // Asynchronously load the map
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set a marker at Vancouver and move the camera
        val vancouver = LatLng(49.2827, -123.1207) // Coordinates for Vancouver
        mMap.addMarker(MarkerOptions().position(vancouver).title("Marker in Vancouver"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vancouver, 10f)) // Adjust zoom level as needed
    }
}
