package com.example.project.ui.email.adapter

import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.ui.email.Email

class EmailAdapter : ListAdapter<com.example.project.ui.email.Email, EmailAdapter.EmailViewHolder>(EmailDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_email, parent, false)
        return EmailViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        val email = getItem(position)
        holder.bind(email)
    }

    class EmailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subject: TextView = itemView.findViewById(R.id.textSubject)
        private val from: TextView = itemView.findViewById(R.id.textFrom)
        private val content: TextView = itemView.findViewById(R.id.textContent)

        fun bind(email: com.example.project.ui.email.Email) { // Use fully qualified name
            subject.text = email.subject
            from.text = email.from
            content.text = email.content
        }
    }

    class EmailDiffCallback : DiffUtil.ItemCallback<com.example.project.ui.email.Email>() {
        override fun areItemsTheSame(oldItem: com.example.project.ui.email.Email, newItem: com.example.project.ui.email.Email): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: com.example.project.ui.email.Email, newItem: com.example.project.ui.email.Email): Boolean {
            return oldItem == newItem
        }
    }
}
