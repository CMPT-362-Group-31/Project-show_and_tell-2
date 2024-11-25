package com.abdinajib_idle.worksheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.ui.worksheet.dataclass.WorksheetListItem

class WorksheetAdapter(
    private val onItemClick: (WorksheetListItem) -> Unit
) : ListAdapter<WorksheetListItem, WorksheetAdapter.ViewHolder>(WorksheetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worksheet, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        view: View,
        private val onItemClick: (WorksheetListItem) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val worksheetType: TextView = view.findViewById(R.id.worksheetType)
        private val worksheetId: TextView = view.findViewById(R.id.worksheetId)
        private val date: TextView = view.findViewById(R.id.date)
        private val time: TextView = view.findViewById(R.id.time)
        private val status: TextView = view.findViewById(R.id.status)

        fun bind(worksheet: WorksheetListItem) {
            // Set the data to views
            worksheetType.text = "${worksheet.type} - ${worksheet.count}"
            worksheetId.text = worksheet.id
            date.text = worksheet.date
            time.text = worksheet.time

            // Set status with appropriate background
            status.text = if (worksheet.status == "In-Progress") {
                "${worksheet.status} ${worksheet.assignedTo}"
            } else {
                worksheet.status
            }

            // Set status background color based on status
            status.setBackgroundResource(
                when (worksheet.status) {
                    "In-Progress" -> R.drawable.status_in_progress_background
                    else -> R.drawable.status_background
                }
            )

            itemView.setOnClickListener { onItemClick(worksheet) }
        }
    }
}

class WorksheetDiffCallback : DiffUtil.ItemCallback<WorksheetListItem>() {
    override fun areItemsTheSame(oldItem: WorksheetListItem, newItem: WorksheetListItem) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: WorksheetListItem, newItem: WorksheetListItem) =
        oldItem == newItem
}