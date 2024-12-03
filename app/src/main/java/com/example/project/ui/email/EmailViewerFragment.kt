package com.example.project.ui.email

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.ui.email.adapter.EmailAdapter
import com.google.firebase.firestore.FirebaseFirestore

class EmailViewerFragment : Fragment() {

    companion object {
        private const val TAG = "EmailViewerFragment"
        private const val ARG_WORKSHEET_ID = "worksheetId"

        fun newInstance(worksheetId: String): EmailViewerFragment {
            val fragment = EmailViewerFragment()
            val bundle = Bundle().apply {
                putString(ARG_WORKSHEET_ID, worksheetId)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var emailRecyclerView: RecyclerView
    private lateinit var emailAdapter: EmailAdapter
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var worksheetId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        worksheetId = arguments?.getString(ARG_WORKSHEET_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_email_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailRecyclerView = view.findViewById(R.id.recyclerViewEmails)
        emailRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        emailAdapter = EmailAdapter()
        emailRecyclerView.adapter = emailAdapter

        if (worksheetId != null) {
            fetchEmailsByWorksheetId(worksheetId!!)
        } else {
            Toast.makeText(requireContext(), "Worksheet ID is missing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchEmailsByWorksheetId(worksheetId: String) {
        db.collection("emails")
            .whereEqualTo("worksheetId", worksheetId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val emails = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Email::class.java)
                }
                emailAdapter.submitList(emails)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching emails: ${exception.message}", exception)
                Toast.makeText(requireContext(), "Failed to fetch emails", Toast.LENGTH_SHORT).show()
            }
    }
}
