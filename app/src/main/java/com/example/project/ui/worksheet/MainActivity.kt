//package com.abdinajib_idle.worksheet
//
//import android.content.Intent
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.google.android.material.textfield.TextInputEditText
//
//class MainActivity : AppCompatActivity() {
//    private lateinit var adapter: WorksheetAdapter
//    private lateinit var searchInput: TextInputEditText
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var fabAddWorksheet: FloatingActionButton
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//
//        setupViews()
//        setupEdgeToEdge()
//        loadWorksheets()
//    }
//
//    private fun setupViews() {
//        // Initialize views
//        recyclerView = findViewById(R.id.worksheetList)
//        fabAddWorksheet = findViewById(R.id.fabAddWorksheet)
//        searchInput = findViewById(R.id.searchInput)
//
//        // Setup RecyclerView
//        adapter = WorksheetAdapter { worksheet ->
//            openWorksheetEditor(worksheet)
//        }
//        recyclerView.apply {
//            layoutManager = LinearLayoutManager(this@MainActivity)
//            adapter = this@MainActivity.adapter
//        }
//
//        // Setup FAB
//        fabAddWorksheet.setOnClickListener {
//            // Open empty worksheet editor
//            openWorksheetEditor(null)
//        }
//
//        // Setup search
//        searchInput.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//            override fun afterTextChanged(s: Editable?) {
//                filterWorksheets(s?.toString() ?: "")
//            }
//        })
//    }
//
//    private fun setupEdgeToEdge() {
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//
//    private fun openWorksheetEditor(worksheet: WorksheetListItem?) {
//        val intent = Intent(this, EditWorksheetActivity::class.java)
//        worksheet?.let {
//            // TODO: Add worksheet data to intent
//            intent.putExtra("WORKSHEET_ID", it.id)
//        }
//        startActivity(intent)
//    }
//
//    private fun loadWorksheets() {
//        // Sample data - replace with actual data source
//        val worksheets = listOf(
//            WorksheetListItem(
//                id = "W 110724002",
//                type = "Crew Pickup",
//                count = 1,
//                date = "Nov/07",
//                time = "05:30",
//                pickupDetails = PickupDetails(
//                    name = "I. GARCIA",
//                    dock = "LARFARGE",
//                    ship = "NACC POROS",
//                    from = "VANCOUVER GENERAL HOSPITAL",
//                    to = "CHATEAU"
//                )
//            ),
//            WorksheetListItem(
//                id = "W 110624001",
//                type = "Crew Pickup",
//                count = 1,
//                date = "Nov/07",
//                time = "10:15",
//                pickupDetails = PickupDetails(
//                    name = "B.TASIS C/E",
//                    dock = "",
//                    ship = "YM COURAGE",
//                    flightNumber = "CX838",
//                    flightArrival = "11:00",
//                    from = "AIRPORT",
//                    to = "CHATEAU"
//                ),
//                status = "In-Progress",
//                assignedTo = "#WILSON"
//            )
//            // Add more sample data as needed
//        )
//
//        adapter.submitList(worksheets)
//    }
//
//    private fun filterWorksheets(query: String) {
//        // TODO: Implement filtering logic
//        val currentList = adapter.currentList
//        if (query.isEmpty()) {
//            adapter.submitList(currentList)
//            return
//        }
//
//        val filteredList = currentList.filter { worksheet ->
//            worksheet.pickupDetails.let {
//                it.ship.contains(query, ignoreCase = true) ||
//                        it.name.contains(query, ignoreCase = true) ||
//                        it.from.contains(query, ignoreCase = true) ||
//                        it.to.contains(query, ignoreCase = true) ||
//                        worksheet.id.contains(query, ignoreCase = true)
//            }
//        }
//        adapter.submitList(filteredList)
//    }
//}
//
//data class WorksheetListItem(
//    val id: String,
//    val type: String,
//    val count: Int = 1,
//    val date: String,
//    val time: String,
//    val pickupDetails: PickupDetails,
//    val status: String = "Unassigned",
//    val assignedTo: String = ""
//)
//
//data class PickupDetails(
//    val name: String,
//    val dock: String,
//    val ship: String,
//    val flightNumber: String = "",
//    val flightArrival: String = "",
//    val from: String,
//    val stop: String = "",
//    val to: String
//)