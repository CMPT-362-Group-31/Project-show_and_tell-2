package com.example.project.ui.worksheet

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.R
import com.example.project.ui.email.EmailActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EditWorksheetFragment : Fragment() {

    companion object {
        private const val TAG = "EditWorksheetFragment"
        private const val ARG_WORKSHEET_ID = "worksheetId"

        fun newInstance(worksheetId: String?): EditWorksheetFragment {
            val fragment = EditWorksheetFragment()
            val bundle = Bundle().apply {
                putString(ARG_WORKSHEET_ID, worksheetId)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    // UI components
    private lateinit var worksheetNumber: TextView
    private lateinit var editDate: TextInputEditText
    private lateinit var editTime: TextInputEditText
    private lateinit var editDriverId: TextInputEditText
    private lateinit var editDriverName: TextInputEditText // Added Driver Name Field
    private lateinit var editPickupLocation: TextInputEditText
    private lateinit var editDropoffLocation: TextInputEditText
    private lateinit var btnSave: View
    private lateinit var btnDelete: View
    private lateinit var btnEmail: View // Added Email button
    private lateinit var btnLinkWorksheet: MaterialButton // Added Link Worksheet button


    // Firestore reference
    private var worksheetId: String? = null
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        worksheetId = arguments?.getString(ARG_WORKSHEET_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_worksheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        initializeViews(view)
        // Set up listeners for UI interactions
        setupListeners()

        // Load or generate worksheet data
        worksheetId?.let {
            loadWorksheetData(it)
        } ?: generateNewWorksheetId()
    }

    private fun initializeViews(view: View) {
        worksheetNumber = view.findViewById(R.id.worksheet_number)
        editDate = view.findViewById(R.id.edit_date)
        editTime = view.findViewById(R.id.edit_time)
        editPickupLocation = view.findViewById(R.id.edit_pickup_location)
        editDropoffLocation = view.findViewById(R.id.edit_dropoff_location)
        editDriverId = view.findViewById(R.id.edit_driver_id) // Initialize Driver ID Field
        editDriverName = view.findViewById(R.id.edit_driver_name) // Initialize Driver Name Field
        btnSave = view.findViewById(R.id.btn_save)
        btnDelete = view.findViewById(R.id.btn_delete)
        btnEmail = view.findViewById(R.id.btn_email) // Initialize Email button
        btnLinkWorksheet = view.findViewById(R.id.btn_link_worksheet) // Initialize Link Worksheet button


        worksheetId?.let { worksheetNumber.text = it }
    }

    private fun setupListeners() {
        editDate.setOnClickListener { showDatePicker(editDate) }
        editTime.setOnClickListener { showTimePicker(editTime) }

        btnSave.setOnClickListener { saveWorksheet() }
        btnDelete.setOnClickListener { deleteWorksheet() }

        // Set click listener for Email button
        btnEmail.setOnClickListener {
            val intent = Intent(requireContext(), EmailActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for Link Worksheet button
        btnLinkWorksheet.setOnClickListener {
            // Show linked email details dialog
            showLinkedEmailDialog()
        }
    }

    private fun generateNewWorksheetId() {
        val calendar = Calendar.getInstance()
        worksheetId = "W${calendar.timeInMillis}"
        worksheetNumber.text = worksheetId
    }

    private fun showDatePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                editText.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", month + 1, day, year))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                editText.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun loadWorksheetData(id: String) {
        db.collection("worksheets").document(id).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    editDate.setText(document.getString("date"))
                    editTime.setText(document.getString("time"))
                    editPickupLocation.setText(document.getString("pickupLocation"))
                    editDropoffLocation.setText(document.getString("dropoffLocation"))
                    editDriverId.setText(document.getString("driverId")) // Load Driver ID
                    editDriverName.setText(document.getString("driverName")) // Load Driver Name
                } else {
                    Log.e(TAG, "No data found for worksheet ID: $id")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching worksheet data", e)
                showErrorDialog("Failed to load worksheet: ${e.message}")
            }
    }

    private fun saveWorksheet() {
        if (!validateForm()) {
            return
        }

        val worksheetData = mapOf(
            "id" to worksheetId,
            "date" to editDate.text.toString(),
            "time" to editTime.text.toString(),
            "pickupLocation" to editPickupLocation.text.toString(),
            "dropoffLocation" to editDropoffLocation.text.toString(),
            "driverId" to editDriverId.text.toString(), // Save Driver ID
            "driverName" to editDriverName.text.toString(), // Save Driver Name
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("worksheets").document(worksheetId!!)
            .set(worksheetData)
            .addOnSuccessListener {
                Log.d(TAG, "Worksheet saved successfully")
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving worksheet", e)
                showErrorDialog("Failed to save worksheet: ${e.message}")
            }
    }

    private fun deleteWorksheet() {
        worksheetId?.let { id ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Worksheet")
                .setMessage("Are you sure you want to delete this worksheet?")
                .setPositiveButton("Delete") { _, _ ->
                    db.collection("worksheets").document(id)
                        .delete()
                        .addOnSuccessListener {
                            Log.d(TAG, "Worksheet deleted successfully")
                            if (isAdded) { // Ensure fragment is still attached
                                findNavController().navigateUp()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error deleting worksheet", e)
                            if (isAdded) { // Ensure fragment is still attached
                                showErrorDialog("Failed to delete worksheet: ${e.message}")
                            }
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } ?: showErrorDialog("Worksheet ID is missing. Cannot delete.")
    }

    private fun validateForm(): Boolean {
        var isValid = true
        if (editPickupLocation.text.isNullOrBlank()) {
            editPickupLocation.error = "Pickup location is required"
            isValid = false
        }
        if (editDropoffLocation.text.isNullOrBlank()) {
            editDropoffLocation.error = "Dropoff location is required"
            isValid = false
        }
        if (editDriverId.text.isNullOrBlank()) { // Validate Driver ID
            editDriverId.error = "Driver ID is required"
            isValid = false
        }
        if (editDriverName.text.isNullOrBlank()) { // Validate Driver Name
            editDriverName.error = "Driver Name is required"
            isValid = false
        }
        return isValid
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLinkedEmailDialog() {
        db.collection("emails")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // No linked email found
                    showMessageDialog("No Linked Email", "You have not linked any email yet.")
                } else {
                    val emailDocument = result.documents.first()
                    val subject = emailDocument.getString("subject") ?: "No Subject"
                    val from = emailDocument.getString("from") ?: "Unknown Sender"
                    val snippet = emailDocument.getString("snippet") ?: "No Content"

                    // Display email content in a dialog
                    showMessageDialog(
                        "Linked Email Details",
                        "Subject: $subject\n\nFrom: $from\n\nContent:\n$snippet"
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching linked email", e)
                showMessageDialog("Error", "Failed to fetch linked email: ${e.message}")
            }
    }

    private fun showMessageDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
