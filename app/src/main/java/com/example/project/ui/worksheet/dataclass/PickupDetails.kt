package com.example.project.ui.worksheet.dataclass

data class PickupDetails(
    val name: String,
    val dock: String,
    val ship: String,
    val flightNumber: String = "",
    val flightArrival: String = "",
    val from: String,
    val stop: String = "",
    val to: String
)
