package com.example.project.ui.email

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project.ui.worksheet.EditWorksheetFragment
import com.example.project.ui.worksheet.EditWorksheetFragment.Companion
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.GmailScopes
import com.google.firebase.firestore.FirebaseFirestore

class EmailActivity : AppCompatActivity() {
    private var account: GoogleSignInAccount? = null
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var worksheetId: String? = null
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        worksheetId = intent?.getStringExtra("WORKSHEET_ID")
        if (worksheetId == null && savedInstanceState != null) {
            worksheetId = savedInstanceState.getString("WORKSHEET_ID")
        }
        Log.d("EmailActivity", "Final worksheetId: $worksheetId")


        // Google sign-in launcher
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                account = task.getResult(ApiException::class.java)
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
            } catch (e: ApiException) {
                e.printStackTrace()
                Toast.makeText(this, "Login Failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        }

        setContent {
            var emailList by remember { mutableStateOf<List<Message>>(emptyList()) }
            var selectedEmail by remember { mutableStateOf<Message?>(null) }
            var loading by remember { mutableStateOf(false) }

            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("Email Viewer") })
                },
                content = { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when {
                            selectedEmail != null -> {
                                // Show detailed view for the selected email
                                DetailedEmailView(email = selectedEmail!!) {
                                    selectedEmail = null
                                }
                            }
                            emailList.isEmpty() && !loading -> {
                                Button(onClick = {
                                    if (account == null) {
                                        // If not signed in, launch Google sign-in
                                        val gso =
                                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                .requestEmail()
                                                .requestScopes(Scope(GmailScopes.GMAIL_READONLY)) // Request Gmail read-only scope
                                                .build()
                                        val client = GoogleSignIn.getClient(this@EmailActivity, gso)
                                        launcher.launch(client.signInIntent)
                                    } else {
                                        // If signed in, fetch email list
                                        loading = true
                                        fetchEmails { emails ->
                                            emailList = emails
                                            loading = false
                                        }
                                    }
                                }) {
                                    Text(text = if (account == null) "Login with Gmail" else "Show Emails")
                                }
                            }
                            loading -> {
                                CircularProgressIndicator()
                            }
                            else -> {
                                LazyColumn {
                                    items(emailList) { email ->
                                        EmailItem(
                                            subject = email.payload.headers.find { it.name == "Subject" }?.value ?: "No Subject",
                                            onClick = { selectedEmail = email }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("WORKSHEET_ID", worksheetId)
    }
    /**
     * Fetches email list from Gmail API
     */
    private fun fetchEmails(callback: (List<Message>) -> Unit) {
        val credential = GoogleAccountCredential.usingOAuth2(
            this, listOf(GmailScopes.GMAIL_READONLY)
        )
        credential.selectedAccount = account?.account

        val gmailService = Gmail.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("Email Viewer").build()

        // Perform network request in a background thread
        Thread {
            try {
                val messages = gmailService.users().messages().list("me").execute().messages ?: emptyList()
                val detailedMessages = messages.take(10).map { message ->
                    gmailService.users().messages().get("me", message.id).execute()
                }
                runOnUiThread {
                    callback(detailedMessages)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Failed to fetch emails", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    /**
     * UI component for a single email item
     */
    @Composable
    fun EmailItem(subject: String, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onClick() }
        ) {
            Text(
                text = subject,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    /**
     * UI component for showing detailed email view
     */
    @Composable
    fun DetailedEmailView(email: Message, onBack: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(onClick = onBack, modifier = Modifier.padding(bottom = 16.dp)) {
                Text("Back")
            }
            Text(
                text = "Subject: ${email.payload.headers.find { it.name == "Subject" }?.value ?: "No Subject"}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "From: ${email.payload.headers.find { it.name == "From" }?.value ?: "Unknown"}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = email.snippet ?: "No content available",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = {
                    if (worksheetId != null) {
                        updateLinkedEmail(email)
                    } else {
                        Toast.makeText(this@EmailActivity, "Worksheet ID is missing", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Link This Email")
            }
        }
    }

    /**
     * Update the linked email in Firestore
     */
    private fun updateLinkedEmail(email: Message) {
        if (worksheetId == null) {
            Toast.makeText(this, "Worksheet ID is missing", Toast.LENGTH_LONG).show()
            return
        }

        val emailContent = email.snippet ?: "No content available"
        val emailSubject = email.payload.headers.find { it.name == "Subject" }?.value ?: "No Subject"
        val emailSender = email.payload.headers.find { it.name == "From" }?.value ?: "Unknown"

        val emailData = mapOf(
            "worksheetId" to worksheetId,
            "subject" to emailSubject,
            "from" to emailSender,
            "content" to emailContent,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("emails")
            .add(emailData)
            .addOnSuccessListener {
                Toast.makeText(this, "Email linked successfully to Worksheet ID: $worksheetId", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to link email: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    /**
     * Save the selected email to Firestore
     */
    private fun saveEmailToFirestore(email: Message) {
        if (worksheetId == null) {
            Toast.makeText(this, "Worksheet ID is missing", Toast.LENGTH_LONG).show()
            return
        }

        val emailData = mapOf(
            "worksheetId" to worksheetId,
            "subject" to (email.payload.headers.find { it.name == "Subject" }?.value ?: "No Subject"),
            "from" to (email.payload.headers.find { it.name == "From" }?.value ?: "Unknown"),
            "snippet" to (email.snippet ?: ""),
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("emails")
            .add(emailData)
            .addOnSuccessListener {
                Toast.makeText(this, "Email linked successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to link email: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        val TAG: String = "EmailActivity"
    }

}