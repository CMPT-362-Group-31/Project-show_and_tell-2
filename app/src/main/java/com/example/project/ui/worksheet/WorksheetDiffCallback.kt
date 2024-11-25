package com.example.project.ui.worksheet

import androidx.recyclerview.widget.DiffUtil
import com.example.project.ui.worksheet.dataclass.WorksheetListItem

class WorksheetDiffCallback : DiffUtil.ItemCallback<WorksheetListItem>() {
    override fun areItemsTheSame(oldItem: WorksheetListItem, newItem: WorksheetListItem) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: WorksheetListItem, newItem: WorksheetListItem) =
        oldItem == newItem
}