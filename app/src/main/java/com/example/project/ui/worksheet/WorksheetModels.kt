package com.example.project.ui.worksheet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorksheetListItem(
    val id: String = "",
    val type: String = "",
    val count: Int = 1,
    val date: String = "",
    val time: String = "",
    val status: String = "Unassigned",
    val pickupDetails: PickupDetails = PickupDetails(),
    val timestamp: Long = System.currentTimeMillis(),
    val driverId: String = "",
    val driverName: String = "",
    val pickupType: String = "",  // New field for selected pickup type
    val crewType: String = ""     // New field for selected crew type
) : Parcelable

@Parcelize
data class PickupDetails(
    val name: String = "",      // agent name
    val dock: String = "",
    val ship: String = "",
    val flightNumber: String = "",
    val flightArrival: String = "",
    val from: String = "",      // pickup location
    val stop: String = "",      // additional stops (comma separated)
    val to: String = ""         // dropoff location
) : Parcelable
