package com.example.project.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DirectionsViewModel : ViewModel() {

    private val apiKey = "AIzaSyB8JBH49M5Bx6pbUblMBzsvT7i-aYdp0lU" // Replace with your actual API key

    private val _routePolyline = MutableLiveData<List<LatLng>>()
    val routePolyline: LiveData<List<LatLng>> get() = _routePolyline

    fun fetchRoute(origin: String, destination: String, waypoints: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getDirections(
                    origin = origin,
                    destination = destination,
                    waypoints = waypoints,
                    apiKey = apiKey
                )

                if (response.isSuccessful) {
                    val polyline = response.body()?.routes?.firstOrNull()?.overview_polyline?.points
                    if (polyline != null) {
                        Log.d("DirectionsAPI", "Encoded polyline: $polyline")
                        val decodedPolyline = decodePolyline(polyline)
                        _routePolyline.postValue(decodedPolyline)
                    } else {
                        Log.e("DirectionsAPI", "No polyline found in response")
                    }
                } else {
                    Log.e("DirectionsAPI", "API Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("DirectionsAPI", "Exception: ${e.message}")
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
            val dLat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dLat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dLng

            val latLng = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(latLng)
        }

        return poly
    }
    fun getLatLngForPlace(placeName: String, callback: (LatLng?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.geocodingApi.getCoordinates(placeName, apiKey)
                if (response.isSuccessful && response.body()?.results?.isNotEmpty() == true) {
                    val location = response.body()!!.results.first().geometry.location
                    val latLng = LatLng(location.lat, location.lng)
                    withContext(Dispatchers.Main) {
                        callback(latLng)
                    }
                } else {
                    Log.e("Geocoding", "Failed to fetch coordinates for $placeName")
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("Geocoding", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }
    fun getEstimatedArrivalTime(
        origin: String,
        destination: String,
        waypoints: String? = null,
        callback: (String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getDirections(
                    origin = origin,
                    destination = destination,
                    waypoints = waypoints,
                    apiKey = apiKey
                )

                if (response.isSuccessful) {
                    val duration = response.body()?.routes
                        ?.firstOrNull()
                        ?.legs
                        ?.firstOrNull()
                        ?.duration
                        ?.text
                    withContext(Dispatchers.Main) {
                        callback(duration)
                    }
                } else {
                    Log.e("DirectionsAPI", "Failed to fetch estimated arrival time: ${response.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("DirectionsAPI", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

}
