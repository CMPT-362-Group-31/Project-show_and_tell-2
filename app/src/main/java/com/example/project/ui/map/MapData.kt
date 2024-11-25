package com.example.project.ui.map

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng

// Represents individual route information
data class RouteInfo(
    val destination: String,
    var departureTime: String = "",
    var estimatedArrivalTime: String = "",
    var arrivalTime: String = "",
    var status: String = "Not Started" // Default status
)

// Represents the complete map data, including route information and metadata
@Entity(tableName = "MapData_table")
@TypeConverters(MapDataConverters::class) // Attach the custom converters
data class MapData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    var routeInfoList: MutableList<RouteInfo> = mutableListOf(), // Dynamic list of RouteInfo
    var pathPoints: List<LatLng> = emptyList() // List of LatLng for route points
) {
    // Add a new route to the list
    fun addRouteInfo(destination: String) {
        routeInfoList.add(RouteInfo(destination = destination))
    }
}
