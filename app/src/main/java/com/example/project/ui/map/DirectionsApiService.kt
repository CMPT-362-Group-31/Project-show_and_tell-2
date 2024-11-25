package com.example.project.ui.map

import DirectionsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApiService {
    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,         // Starting location
        @Query("destination") destination: String, // Ending location
        @Query("waypoints") waypoints: String?,    // Intermediate points (optional)
        @Query("key") apiKey: String              // Your Google API Key
    ): Response<DirectionsResponse>
}