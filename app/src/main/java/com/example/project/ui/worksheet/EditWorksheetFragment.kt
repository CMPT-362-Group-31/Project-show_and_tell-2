package com.example.project.ui.worksheet

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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

    private lateinit var worksheetNumber: TextView
    private lateinit var editDate: TextInputEditText
    private lateinit var editTime: TextInputEditText
    private lateinit var editPickupLocation: TextInputEditText
    private lateinit var editDropoffLocation: TextInputEditText
    private lateinit var pickupTypeGroup: ChipGroup // Pickup Type ChipGroup
    private lateinit var crewTypeGroup: ChipGroup // Crew Type ChipGroup
    private lateinit var btnSave: View
    private lateinit var btnDelete: View

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

        initializeViews(view)
        setupListeners()

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
        pickupTypeGroup = view.findViewById(R.id.pickup_type_group) // Initialize pickup type ChipGroup
        crewTypeGroup = view.findViewById(R.id.crew_type_group) // Initialize crew type ChipGroup
        btnSave = view.findViewById(R.id.btn_save)
        btnDelete = view.findViewById(R.id.btn_delete)

        worksheetId?.let { worksheetNumber.text = it }
    }

    private fun setupListeners() {
        editDate.setOnClickListener { showDatePicker(editDate) }
        editTime.setOnClickListener { showTimePicker(editTime) }

        btnSave.setOnClickListener { saveWorksheet() }
        btnDelete.setOnClickListener { deleteWorksheet() }

        // Listener for Pickup Type ChipGroup
        pickupTypeGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip_package -> {
                    // Automatically set Crew Type to "Other Kind"
                    crewTypeGroup.check(R.id.chip_other_kind)
                }
                R.id.chip_crew -> {
                    // Automatically set Crew Type to "Off Signers"
                    crewTypeGroup.check(R.id.chip_off_signers)
                }
            }
        }

        // Listener for Crew Type ChipGroup
        crewTypeGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedChip = group.findViewById<Chip>(checkedId)
            Log.d(TAG, "Crew type selected: ${selectedChip?.text}")
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

                    // Restore selection for pickup type
                    when (document.getString("pickupType")) {
                        "Crew" -> pickupTypeGroup.check(R.id.chip_crew)
                        "Package" -> pickupTypeGroup.check(R.id.chip_package)
                    }

                    // Restore selection for crew type
                    when (document.getString("crewType")) {
                        "Off Signers" -> crewTypeGroup.check(R.id.chip_off_signers)
                        "On Signers" -> crewTypeGroup.check(R.id.chip_on_signers)
                        "Other Kind" -> crewTypeGroup.check(R.id.chip_other_kind)
                    }
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
        val selectedPickupTypeChip = if (pickupTypeGroup.checkedChipId != View.NO_ID) {
            pickupTypeGroup.findViewById<Chip>(pickupTypeGroup.checkedChipId)
        } else null

        val selectedCrewTypeChip = if (crewTypeGroup.checkedChipId != View.NO_ID) {
            crewTypeGroup.findViewById<Chip>(crewTypeGroup.checkedChipId)
        } else null

        val pickupType = selectedPickupTypeChip?.text?.toString() ?: "None"
        val crewType = selectedCrewTypeChip?.text?.toString() ?: "None"

        Log.d("SaveDebug", "pickupType: $pickupType, crewType: $crewType")

        val worksheetData = mapOf(
            "id" to worksheetId,
            "date" to editDate.text.toString(),
            "time" to editTime.text.toString(),
            "pickupLocation" to editPickupLocation.text.toString(),
            "dropoffLocation" to editDropoffLocation.text.toString(),
            "pickupType" to (selectedPickupTypeChip?.text?.toString() ?: ""),
            "crewType" to (selectedCrewTypeChip?.text?.toString() ?: ""),
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

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
