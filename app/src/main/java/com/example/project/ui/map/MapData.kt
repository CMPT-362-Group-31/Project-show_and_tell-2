package com.example.project.ui.map

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng

// Represents individual route information
data class RouteInfo(
    val destination: String = "", // Default value for Firebase
    var departureTime: String = "",
    var estimatedArrivalTime: String = "",
    var arrivalTime: String = "",
    var status: String = "Not Started" // Default status
) {
    // No-argument constructor required for Firebase
    constructor() : this(
        destination = "",
        departureTime = "",
        estimatedArrivalTime = "",
        arrivalTime = "",
        status = "Not Started"
    )
}
data class FirebaseLatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    // No-argument constructor required for Firebase
    constructor() : this(0.0, 0.0)
    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }
}
// Represents the complete map data, including route information and metadata
@Entity(tableName = "MapData_table")
@TypeConverters(MapDataConverters::class) // Attach the custom converters
data class MapData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "", // Default value for Firebase
    var routeInfoList: MutableList<RouteInfo> = mutableListOf(), // Dynamic list of RouteInfo
    var pathPoints: List<FirebaseLatLng> = emptyList(), // Replace LatLng with FirebaseLatLng
    val another_column_name: Int = 0 // New column
) {
    // No-argument constructor required for Firebase
    constructor() : this(
        id = 0,
        name = "",
        routeInfoList = mutableListOf(),
        pathPoints = emptyList()
    )

    // Add a new route to the list
    fun addRouteInfo(destination: String) {
        routeInfoList.add(RouteInfo(destination = destination))
    }
}
