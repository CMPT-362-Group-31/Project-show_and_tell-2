package com.example.project.ui.worksheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.R
import com.example.project.databinding.FragmentWorksheetBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class WorksheetFragment : Fragment() {
    private var _binding: FragmentWorksheetBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val worksheets = mutableListOf<WorksheetListItem>()
    private lateinit var adapter: WorksheetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorksheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchWorksheets()
        setupFilterChips()
    }

    private fun setupRecyclerView() {
        adapter = WorksheetAdapter { worksheet ->
            // Handle item click
        }

        binding.worksheetList.layoutManager = LinearLayoutManager(requireContext())
        binding.worksheetList.adapter = adapter
    }

    private fun fetchWorksheets() {
        db.collection("worksheets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("WorksheetFragment", "Error fetching worksheets", error)
                    return@addSnapshotListener
                }

                worksheets.clear()
                snapshot?.documents?.forEach { doc ->
                    try {
                        val worksheet = WorksheetListItem(
                            id = doc.getString("id") ?: "",
                            pickupType = doc.getString("pickupType") ?: "",
                            crewType = doc.getString("crewType") ?: "",
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            status = doc.getString("status") ?: "Unassigned",
                            driverId = doc.getString("driverId") ?: "",
                            driverName = doc.getString("driverName") ?: ""
                        )
                        worksheets.add(worksheet)
                    } catch (e: Exception) {
                        Log.e("WorksheetFragment", "Error parsing document", e)
                    }
                }

                applyFilters() // Apply filters immediately after fetching data
            }
    }

    private fun setupFilterChips() {
        val filterListener = { _: View ->
            applyFilters()
        }

        binding.chipDateAscending.setOnClickListener(filterListener)
        binding.chipDateDescending.setOnClickListener(filterListener)
        binding.chipShowCrew.setOnClickListener(filterListener)
        binding.chipShowPackage.setOnClickListener(filterListener)
    }

    private fun applyFilters() {
        val filteredList = mutableListOf<WorksheetListItem>()

        // Start with the original list
        var currentList = worksheets

        // Apply sorting
        when {
            binding.chipDateAscending.isChecked -> {
                currentList = currentList.sortedBy { it.date }.toMutableList()
            }
            binding.chipDateDescending.isChecked -> {
                currentList = currentList.sortedByDescending { it.date }.toMutableList()
            }
        }

        // Apply filtering by pickupType
        if (binding.chipShowCrew.isChecked) {
            filteredList.addAll(currentList.filter { it.pickupType == "Crew" })
        }
        if (binding.chipShowPackage.isChecked) {
            filteredList.addAll(currentList.filter { it.pickupType == "Package" })
        }

        // If no specific filters are selected, show all
        if (!binding.chipShowCrew.isChecked && !binding.chipShowPackage.isChecked) {
            filteredList.addAll(currentList)
        }

        // Update the adapter
        adapter.submitList(filteredList)

        // Toggle empty state visibility
        binding.emptyState.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


