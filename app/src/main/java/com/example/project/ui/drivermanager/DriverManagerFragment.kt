package com.example.project.ui.drivermanager

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.project.R
import com.example.project.ui.map.MapData
import com.example.project.ui.map.MapDataDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DriverManagerFragment : Fragment() {

    private lateinit var driverListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val driverList = mutableListOf<String>() // List to store driver names
    private val driverIdList = mutableListOf<Long>() // List to store driver IDs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_drivermanager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accountType = 0 // For testing purposes only

        if (accountType != 0) {
            // Show unauthorized dialog and navigate back
            showUnauthorizedDialog()
            return
        }

        driverListView = view.findViewById(R.id.driverListView)

        // Initialize the adapter for the ListView
        adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            driverList
        )
        driverListView.adapter = adapter

        // Fetch drivers from the database
        fetchDrivers()

        // Set click listener for each driver
        driverListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDriverId = driverIdList[position]
            navigateToDriverRoute(selectedDriverId)
        }
    }

    private fun fetchDrivers() {
        val dao = MapDataDatabase.getDatabase(requireContext()).mapDataDao()

        lifecycleScope.launch(Dispatchers.IO) {
            // Collect the Flow<List<MapData>> from the database
            dao.getAllMapData().collect { dataList ->
                withContext(Dispatchers.Main) {
                    // Clear existing lists before adding new data
                    driverList.clear()
                    driverIdList.clear()

                    // Populate the lists with the fetched data
                    dataList.forEach { mapData ->
                        driverList.add(mapData.name) // Add driver names
                        driverIdList.add(mapData.id) // Add corresponding IDs
                    }

                    // Notify the adapter of data changes
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun navigateToDriverRoute(driverId: Long) {
        // Start DriverRouteActivity with the driverId
        val intent = Intent(requireContext(), DriverRouteActivity::class.java).apply {
            putExtra("driverId", driverId)
        }
        startActivity(intent)
    }

    private fun showUnauthorizedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Unauthorized Access")
            .setMessage("Only authorized users can access this section.")
            .setPositiveButton("OK") { _, _ ->
                // Navigate back to HomeFragment or any appropriate fragment
                findNavController().navigate(R.id.action_driverManagerFragment_to_homeFragment)
            }
            .setCancelable(false)
            .show()
    }
}
