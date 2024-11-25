package com.example.project.ui.worksheet

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.project.R
import com.example.project.ui.worksheet.dataclass.CrewInfo
import com.example.project.ui.worksheet.dataclass.FlightInfo
import com.example.project.ui.worksheet.dataclass.LocationInfo
import com.example.project.ui.worksheet.dataclass.ShipInfo
import com.example.project.ui.worksheet.dataclass.WorksheetData
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class EditWorksheetActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "EditWorksheetActivity"
    }

    private lateinit var worksheetNumber: TextView
    private lateinit var btnClose: ImageButton
    private lateinit var editDate: TextInputEditText
    private lateinit var editTime: TextInputEditText
    private lateinit var pickupTypeGroup: ChipGroup
    private lateinit var editShipName: TextInputEditText
    private lateinit var editAgentName: TextInputEditText
    private lateinit var editCompany: TextInputEditText
    private lateinit var editDock: TextInputEditText
    private lateinit var crewTypeGroup: ChipGroup
    private lateinit var editNumberPeople: TextInputEditText
    private lateinit var editFlightNumber: TextInputEditText
    private lateinit var editArrivalTime: TextInputEditText
    private lateinit var editDepartureTime: TextInputEditText
    private lateinit var domUsIntSpinner: AutoCompleteTextView
    private lateinit var editPickupLocation: TextInputEditText
    private lateinit var editDropoffLocation: TextInputEditText
    private lateinit var editDeliveryRemark: TextInputEditText
    private lateinit var btnAdditionalStops: Button
    private lateinit var additionalStopsContainer: LinearLayout
    private lateinit var editJobStartTime: TextInputEditText
    private lateinit var editJobFinishTime: TextInputEditText
    private lateinit var btnDelete: Button
    private lateinit var btnSavePicture: Button
    private lateinit var btnReset: Button
    private lateinit var btnSave: Button
    private lateinit var btnEmail: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_worksheet)

        initializeComponents()
        setupListeners()
        setDefaultDateTime()
    }

    private fun initializeComponents() {
        worksheetNumber = findViewById(R.id.worksheet_number)
        btnClose = findViewById(R.id.btn_close)
        editDate = findViewById(R.id.edit_date)
        editTime = findViewById(R.id.edit_time)
        pickupTypeGroup = findViewById(R.id.pickup_type_group)
        editShipName = findViewById(R.id.edit_ship_name)
        editAgentName = findViewById(R.id.edit_agent_name)
        editCompany = findViewById(R.id.edit_company)
        editDock = findViewById(R.id.edit_dock)
        crewTypeGroup = findViewById(R.id.crew_type_group)
        editNumberPeople = findViewById(R.id.edit_number_people)
        editFlightNumber = findViewById(R.id.edit_flight_number)
        editArrivalTime = findViewById(R.id.edit_arrival_time)
        editDepartureTime = findViewById(R.id.edit_departure_time)
        domUsIntSpinner = findViewById(R.id.dom_us_int_dropdown)
        editPickupLocation = findViewById(R.id.edit_pickup_location)
        editDropoffLocation = findViewById(R.id.edit_dropoff_location)
        editDeliveryRemark = findViewById(R.id.edit_delivery_remark)
        btnAdditionalStops = findViewById(R.id.btn_additional_stops)
        additionalStopsContainer = findViewById(R.id.additional_stops_container)
        editJobStartTime = findViewById(R.id.edit_job_start_time)
        editJobFinishTime = findViewById(R.id.edit_job_finish_time)
        btnDelete = findViewById(R.id.btn_delete)
        btnSavePicture = findViewById(R.id.btn_save_picture)
        btnReset = findViewById(R.id.btn_reset)
        btnSave = findViewById(R.id.btn_save)
        btnEmail = findViewById(R.id.btn_email)


        setupDomUsIntSpinner()
    }

    private fun setupListeners() {
        // Close button
        btnClose.setOnClickListener {
            finish()
        }

        // Date and Time input listeners
        findViewById<TextInputLayout>(R.id.date_input_layout).setOnClickListener {
            showDatePicker()
        }
        editDate.setOnClickListener {
            Log.d(TAG, "Date field clicked")
            showDatePicker()
        }

        findViewById<TextInputLayout>(R.id.time_input_layout).setOnClickListener {
            showTimePicker(editTime)
        }
        editTime.setOnClickListener {
            Log.d(TAG, "Time field clicked")
            showTimePicker(editTime)
        }

        // Flight time listeners
        editArrivalTime.setOnClickListener {
            Log.d(TAG, "Arrival time field clicked")
            showTimePicker(editArrivalTime)
        }
        (editArrivalTime.parent.parent as? TextInputLayout)?.setOnClickListener {
            showTimePicker(editArrivalTime)
        }

        editDepartureTime.setOnClickListener {
            Log.d(TAG, "Departure time field clicked")
            showTimePicker(editDepartureTime)
        }
        (editDepartureTime.parent.parent as? TextInputLayout)?.setOnClickListener {
            showTimePicker(editDepartureTime)
        }

        // Job time listeners
        editJobStartTime.setOnClickListener {
            Log.d(TAG, "Job start time field clicked")
            showTimePicker(editJobStartTime)
        }
        (editJobStartTime.parent.parent as? TextInputLayout)?.setOnClickListener {
            showTimePicker(editJobStartTime)
        }

        editJobFinishTime.setOnClickListener {
            Log.d(TAG, "Job finish time field clicked")
            showTimePicker(editJobFinishTime)
        }
        (editJobFinishTime.parent.parent as? TextInputLayout)?.setOnClickListener {
            showTimePicker(editJobFinishTime)
        }

        // Additional stops button
        btnAdditionalStops.setOnClickListener {
            addNewLocationInput()
            Log.d(TAG, "Add additional stop button clicked")
        }

        // Action buttons
        btnSave.setOnClickListener {
            saveWorksheet()
            Log.d(TAG, "Save button clicked")
        }

        btnDelete.setOnClickListener {
            deleteWorksheet()
            Log.d(TAG, "Delete button clicked")
        }

        btnReset.setOnClickListener {
            resetForm()
            Log.d(TAG, "Reset button clicked")
        }
        btnEmail.setOnClickListener {
            Log.d(TAG, "Email button clicked")
            //Horo --> you can add the email functionality here
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                editDate.setText(selectedDate)
                Log.d(TAG, "Date selected: $selectedDate")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                editText.setText(selectedTime)
                Log.d(TAG, "Time selected: $selectedTime")
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun setDefaultDateTime() {
        val calendar = Calendar.getInstance()
        val currentDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
        val currentTime = String.format(Locale.getDefault(), "%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        editDate.setText(currentDate)
        editTime.setText(currentTime)
        Log.d(TAG, "Default date and time set: $currentDate, $currentTime")
    }

    private fun addNewLocationInput() {
        val newStopLayout = LayoutInflater.from(this).inflate(
            R.layout.item_additional_stop,
            additionalStopsContainer,
            false
        )
        additionalStopsContainer.addView(newStopLayout)
    }

    private fun setupDomUsIntSpinner() {
        val items = arrayOf("DOM", "US", "INT")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        domUsIntSpinner.setAdapter(adapter)
        domUsIntSpinner.setText("INT", false)
        Log.d(TAG, "Default DOM/US/INT set to INT")
    }

    private fun saveWorksheet() {
        val worksheetData = WorksheetData(
            dateTime = Pair(editDate.text.toString(), editTime.text.toString()),
            pickupType = when (pickupTypeGroup.checkedChipId) {
                R.id.chip_crew -> "Crew"
                R.id.chip_package -> "Package"
                else -> "Unknown"
            },
            shipInfo = ShipInfo(
                shipName = editShipName.text.toString(),
                agentName = editAgentName.text.toString(),
                company = editCompany.text.toString(),
                dock = editDock.text.toString()
            ),
            crewType = CrewInfo(
                type = when (crewTypeGroup.checkedChipId) {
                    R.id.chip_off_signers -> "Off Signers"
                    R.id.chip_on_signers -> "On Signers"
                    R.id.chip_other_kind -> "Other Kind"
                    else -> "Unknown"
                },
                numberOfPeople = editNumberPeople.text.toString().toIntOrNull() ?: 1
            ),
            flightDetails = FlightInfo(
                flightNumber = editFlightNumber.text.toString(),
                arrivalTime = editArrivalTime.text.toString(),
                departureTime = editDepartureTime.text.toString()
            ),
            locationDetails = LocationInfo(
                pickupLocation = editPickupLocation.text.toString(),
                dropoffLocation = editDropoffLocation.text.toString()
            ),
            additionalDetails = editDeliveryRemark.text.toString(),
            jobTimes = Pair(editJobStartTime.text.toString(), editJobFinishTime.text.toString())
        )
        // Save worksheet data
        Log.d(TAG, "Worksheet data saved: $worksheetData")
    }

    private fun deleteWorksheet() {
        AlertDialog.Builder(this)
            .setTitle("Delete Worksheet")
            .setMessage("Are you sure you want to delete this worksheet?")
            .setPositiveButton("Delete") { _, _ ->
                Log.d(TAG, "Worksheet deleted")
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetForm() {
        setDefaultDateTime()
        pickupTypeGroup.check(R.id.chip_crew)
        editShipName.text?.clear()
        editAgentName.text?.clear()
        editCompany.text?.clear()
        editDock.text?.clear()
        crewTypeGroup.check(R.id.chip_other_kind)
        editNumberPeople.setText("1")
        editFlightNumber.text?.clear()
        editArrivalTime.text?.clear()
        editDepartureTime.text?.clear()
        domUsIntSpinner.setText("INT", false)
        editPickupLocation.text?.clear()
        editDropoffLocation.text?.clear()
        editDeliveryRemark.text?.clear()
        additionalStopsContainer.removeAllViews()
        editJobStartTime.text?.clear()
        editJobFinishTime.text?.clear()
        Log.d(TAG, "Form reset to default values")
    }
}



