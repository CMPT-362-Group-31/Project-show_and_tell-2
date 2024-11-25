package com.example.project.ui.worksheet.dataclass


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorksheetListItem(
    val id: String,
    val type: String,
    val count: Int = 1,
    val date: String,
    val time: String,
    val pickupDetails: PickupDetails,
    val status: String = "Unassigned",
    val assignedTo: String = ""
) : Parcelable

@Parcelize
data class PickupDetails(
    val name: String,
    val dock: String,
    val ship: String,
    val flightNumber: String = "",
    val flightArrival: String = "",
    val from: String,
    val stop: String = "",
    val to: String
) : Parcelable

@Parcelize
data class WorksheetData(
    val dateTime: Pair<String, String>,
    val pickupType: String,
    val shipInfo: ShipInfo,
    val crewType: CrewInfo,
    val flightDetails: FlightInfo,
    val locationDetails: LocationInfo,
    val additionalDetails: String,
    val jobTimes: Pair<String, String>
) : Parcelable

@Parcelize
data class ShipInfo(
    val shipName: String,
    val agentName: String,
    val company: String,
    val dock: String
) : Parcelable

@Parcelize
data class CrewInfo(
    val type: String,
    val numberOfPeople: Int
) : Parcelable

@Parcelize
data class FlightInfo(
    val flightNumber: String,
    val arrivalTime: String,
    val departureTime: String
) : Parcelable

@Parcelize
data class LocationInfo(
    val pickupLocation: String,
    val dropoffLocation: String
) : Parcelable
