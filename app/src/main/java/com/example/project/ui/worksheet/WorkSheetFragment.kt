//package com.example.project.ui.worksheet
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import com.example.project.R
//
//class WorkSheetFragment : Fragment() {
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val root = inflater.inflate(R.layout.fragment_worksheet, container, false)
//        return root
//    }
//
//}

package com.example.project.ui.worksheet

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.ui.worksheet.EditWorksheetActivity
import com.example.project.ui.worksheet.WorksheetAdapter
import com.example.project.R
import com.example.project.ui.worksheet.dataclass.PickupDetails
import com.example.project.ui.worksheet.dataclass.WorksheetListItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class WorkSheetFragment : Fragment() {

    private lateinit var adapter: WorksheetAdapter
    private lateinit var searchInput: TextInputEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddWorksheet: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_worksheet, container, false)

        setupViews(root)
        setupEdgeToEdge(root)
        loadWorksheets()

        return root
    }

    private fun setupViews(root: View) {
        // Initialize views
        recyclerView = root.findViewById(R.id.worksheetList)
        fabAddWorksheet = root.findViewById(R.id.fabAddWorksheet)
        searchInput = root.findViewById(R.id.searchInput)

        // Setup RecyclerView with adapter
        adapter = WorksheetAdapter { worksheet ->
            openWorksheetEditor(worksheet)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@WorkSheetFragment.adapter
        }

        // Setup FAB to open a blank worksheet editor
        fabAddWorksheet.setOnClickListener {
            openWorksheetEditor(null)
        }

        // Setup search functionality
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterWorksheets(s?.toString() ?: "")
            }
        })
    }

    private fun setupEdgeToEdge(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun openWorksheetEditor(worksheet: WorksheetListItem?) {
        val intent = Intent(requireContext(), EditWorksheetActivity::class.java)
        worksheet?.let {
            intent.putExtra("WORKSHEET_ID", it.id)
        }
        startActivity(intent)
    }

    private fun loadWorksheets() {
        // Sample data - replace with actual data source
        val worksheets = listOf(
            WorksheetListItem(
                id = "W 110724002",
                type = "Crew Pickup",
                count = 1,
                date = "Nov/07",
                time = "05:30",
                pickupDetails = PickupDetails(
                    name = "I. GARCIA",
                    dock = "LARFARGE",
                    ship = "NACC POROS",
                    from = "VANCOUVER GENERAL HOSPITAL",
                    to = "CHATEAU"
                )
            ),
            WorksheetListItem(
                id = "W 110624001",
                type = "Crew Pickup",
                count = 1,
                date = "Nov/07",
                time = "10:15",
                pickupDetails = PickupDetails(
                    name = "B.TASIS C/E",
                    dock = "",
                    ship = "YM COURAGE",
                    flightNumber = "CX838",
                    flightArrival = "11:00",
                    from = "AIRPORT",
                    to = "CHATEAU"
                ),
                status = "In-Progress",
                assignedTo = "#WILSON"
            )
        )

        adapter.submitList(worksheets)
    }

    private fun filterWorksheets(query: String) {
        val currentList = adapter.currentList
        if (query.isEmpty()) {
            adapter.submitList(currentList)
            return
        }

        val filteredList = currentList.filter { worksheet ->
            worksheet.pickupDetails.let {
                it.ship.contains(query, ignoreCase = true) ||
                        it.name.contains(query, ignoreCase = true) ||
                        it.from.contains(query, ignoreCase = true) ||
                        it.to.contains(query, ignoreCase = true) ||
                        worksheet.id.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filteredList)
    }
}
