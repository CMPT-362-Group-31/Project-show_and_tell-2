package com.example.project.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.project.R
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var textTotalCrew: TextView
    private lateinit var textTotalPackage: TextView
    private lateinit var textActiveDrivers: TextView
    private lateinit var textCompletedWorksheets: TextView

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        textTotalCrew = view.findViewById(R.id.textTotalCrew)
        textTotalPackage = view.findViewById(R.id.textTotalPackage)
        textActiveDrivers = view.findViewById(R.id.textActiveDrivers)
        textCompletedWorksheets = view.findViewById(R.id.textCompletedWorksheets)

        fetchStatistics()

        return view
    }

    private fun fetchStatistics() {
        db.collection("worksheets").get()
            .addOnSuccessListener { documents ->
                var totalCrew = 0
                var totalPackage = 0
                val activeDrivers = mutableSetOf<String>()
                var completedWorksheets = 0

                for (document in documents) {
                    val pickupType = document.getString("pickupType")
                    val status = document.getString("status")
                    val driverId = document.getString("driverId")

                    if (pickupType == "Crew") totalCrew++
                    if (pickupType == "Package") totalPackage++
                    if (!driverId.isNullOrEmpty()) activeDrivers.add(driverId)
                    if (status == "Complete") completedWorksheets++
                }

                // Update UI
                textTotalCrew.text = "Total Crew Worksheets: $totalCrew"
                textTotalPackage.text = "Total Package Worksheets: $totalPackage"
                textActiveDrivers.text = "Active Drivers: ${activeDrivers.size}"
                textCompletedWorksheets.text = "Completed Worksheets: $completedWorksheets"
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error fetching statistics", e)
            }
    }
}
