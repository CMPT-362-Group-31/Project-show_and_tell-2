package com.example.project.ui.worksheet.dataclass

import com.example.project.ui.worksheet.dataclass.CrewInfo
import com.example.project.ui.worksheet.dataclass.FlightInfo
import com.example.project.ui.worksheet.dataclass.LocationInfo
import com.example.project.ui.worksheet.dataclass.ShipInfo

data class WorksheetData(
    val dateTime: Pair<String, String>,
    val pickupType: String,
    val shipInfo: ShipInfo,
    val crewType: CrewInfo,
    val flightDetails: FlightInfo,
    val locationDetails: LocationInfo,
    val additionalDetails: String,
    val jobTimes: Pair<String, String>
)
