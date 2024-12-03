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
import com.example.project.ui.worksheet.dataclass.PickupDetails
import com.example.project.ui.worksheet.dataclass.WorksheetListItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query



class WorksheetFragment : Fragment() {
    private var _binding: FragmentWorksheetBinding? = null
    private val binding get() = _binding!!

    private var destinationChangedListener: NavController.OnDestinationChangedListener? = null
    private val db = FirebaseFirestore.getInstance()
    private val worksheets = mutableListOf<WorksheetListItem>()
    private lateinit var adapter: WorksheetAdapter

    companion object {
        private const val TAG = "WorksheetFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorksheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated started")

        setupRecyclerView()
        setupFabVisibility()
        fetchWorksheets()
    }

    private fun setupRecyclerView() {
        adapter = WorksheetAdapter { worksheet ->
            findNavController().navigate(
                WorksheetFragmentDirections.actionWorksheetFragmentToEditWorksheetFragment(worksheet.id)
            )
        }

        binding.worksheetList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@WorksheetFragment.adapter
        }
    }

    private fun setupFabVisibility() {
        try {
            Log.d(TAG, "Setting up FAB visibility")

            destinationChangedListener?.let { listener ->
                findNavController().removeOnDestinationChangedListener(listener)
            }

            destinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
                binding.fabAddWorksheet.visibility = when (destination.id) {
                    R.id.editWorksheetFragment -> View.GONE
                    R.id.worksheetFragment -> View.VISIBLE
                    else -> View.VISIBLE
                }
            }

            findNavController().addOnDestinationChangedListener(destinationChangedListener!!)

            binding.fabAddWorksheet.setOnClickListener {
                findNavController().navigate(
                    WorksheetFragmentDirections.actionWorksheetFragmentToEditWorksheetFragment(null)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up FAB visibility", e)
        }
    }

    private fun fetchWorksheets() {
        Log.d(TAG, "Fetching worksheets from Firestore")
        db.collection("worksheets")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching worksheets", error)
                    return@addSnapshotListener
                }

                // Ensure the fragment's view is still valid
                if (_binding == null) {
                    Log.w(TAG, "fetchWorksheets called after view was destroyed")
                    return@addSnapshotListener
                }

                worksheets.clear()
                snapshot?.documents?.forEach { doc ->
                    try {
                        val worksheet = WorksheetListItem(
                            id = doc.getString("id") ?: doc.id,
                            type = doc.getString("type") ?: "",
                            count = (doc.get("count") as? Number)?.toInt() ?: 1,
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            status = doc.getString("status") ?: "Unassigned",
                            driverId = doc.getString("driverId") ?: "",
                            driverName = doc.getString("driverName") ?: "",
                            pickupDetails = doc.get("pickupDetails")?.let { details ->
                                val map = details as Map<*, *>
                                PickupDetails(
                                    name = map["name"] as? String ?: "",
                                    dock = map["dock"] as? String ?: "",
                                    ship = map["ship"] as? String ?: "",
                                    flightNumber = map["flightNumber"] as? String ?: "",
                                    flightArrival = map["flightArrival"] as? String ?: "",
                                    from = map["from"] as? String ?: "",
                                    stop = map["stop"] as? String ?: "",
                                    to = map["to"] as? String ?: ""
                                )
                            } ?: PickupDetails()
                        )
                        worksheets.add(worksheet)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing worksheet document: ${doc.id}", e)
                    }
                }

                // Update UI safely
                binding.worksheetList.adapter?.let {
                    adapter.submitList(ArrayList(worksheets))
                }
                binding.emptyState.visibility = if (worksheets.isEmpty()) View.VISIBLE else View.GONE
            }
    }




    override fun onDestroyView() {
        destinationChangedListener?.let { listener ->
            try {
                findNavController().removeOnDestinationChangedListener(listener)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing destination listener", e)
            }
        }

        _binding = null
        super.onDestroyView()
    }
}
