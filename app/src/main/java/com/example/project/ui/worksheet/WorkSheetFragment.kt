package com.example.project.ui.worksheet

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.ui.worksheet.dataclass.WorksheetListItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

public class WorksheetFragment : Fragment() {
    private lateinit var adapter: WorksheetAdapter
    private lateinit var searchInput: TextInputEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddWorksheet: FloatingActionButton
    private lateinit var emptyStateView: View

    // In-memory storage of worksheets
    private val worksheets = mutableListOf<WorksheetListItem>()

    // Register activity result handler
    private val worksheetEditorResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            result.data?.let { data ->
                // Handle deletion
                data.getStringExtra("DELETE_WORKSHEET")?.let { worksheetId ->
                    worksheets.removeAll { it.id == worksheetId }
                    updateUIState()
                    return@let
                }

                // Handle save/update
                data.getParcelableExtra<WorksheetListItem>("WORKSHEET_RESULT")?.let { worksheet ->
                    val existingIndex = worksheets.indexOfFirst { it.id == worksheet.id }
                    if (existingIndex != -1) {
                        worksheets[existingIndex] = worksheet
                    } else {
                        worksheets.add(0, worksheet) // Add new worksheets at the top
                    }
                    updateUIState()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_worksheet, container, false)
        setupViews(rootView)
        setupEdgeToEdge(rootView)
        updateUIState()
        return rootView
    }

    private fun setupViews(rootView: View) {
        // Initialize views
        recyclerView = rootView.findViewById(R.id.worksheetList)
        fabAddWorksheet = rootView.findViewById(R.id.fabAddWorksheet)
        searchInput = rootView.findViewById(R.id.searchInput)
        emptyStateView = rootView.findViewById(R.id.emptyState)

        // Setup RecyclerView
        adapter = WorksheetAdapter { worksheet ->
            openWorksheetEditor(worksheet)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@WorksheetFragment.adapter
        }

        // Setup FAB
        fabAddWorksheet.setOnClickListener {
            openWorksheetEditor(null)
        }

        // Setup search
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterWorksheets(s?.toString() ?: "")
            }
        })
    }

    private fun setupEdgeToEdge(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun openWorksheetEditor(worksheet: WorksheetListItem?) {
        val intent = Intent(requireContext(), EditWorksheetActivity::class.java)
        worksheet?.let {
            intent.putExtra("WORKSHEET_DATA", it)
        }
        worksheetEditorResult.launch(intent)
    }

    private fun updateUIState() {
        if (worksheets.isEmpty()) {
            recyclerView.visibility = View.GONE
            searchInput.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            searchInput.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
            adapter.submitList(ArrayList(worksheets)) // Create new list to trigger DiffUtil
        }
    }

    private fun filterWorksheets(query: String) {
        if (query.isEmpty()) {
            adapter.submitList(ArrayList(worksheets))
            return
        }

        val filteredList = worksheets.filter { worksheet ->
            worksheet.pickupDetails.let {
                it.ship.contains(query, ignoreCase = true) ||
                        it.name.contains(query, ignoreCase = true) ||
                        it.from.contains(query, ignoreCase = true) ||
                        it.to.contains(query, ignoreCase = true) ||
                        worksheet.id.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filteredList)

        // Show/hide empty state based on filtered results
        if (filteredList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
        }
    }

    companion object {
        private const val TAG = "WorksheetFragment"
    }
}