package com.example.project.ui.email

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmailActivity: AppCompatActivity() {
    private var account: GoogleSignInAccount? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 注册登录回调
        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
            var emailList by remember { mutableStateOf<List<String>>(emptyList()) }
            var loading by remember { mutableStateOf(false) }

            Scaffold(
                topBar = { TopAppBar(title = { Text("Email Viewer") }) },
                content = { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (emailList.isEmpty() && !loading) {
                            Button(onClick = {
                                if (account == null) {
                                    // 如果未登录，启动 Google 登录
                                    val gso =
                                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestEmail()
                                            .requestScopes(Scope(GmailScopes.GMAIL_READONLY)) // 请求 Gmail 只读权限
                                            .build()
                                    val client = GoogleSignIn.getClient(this@MainActivity, gso)
                                    launcher.launch(client.signInIntent)
                                } else {
                                    // 如果已登录，获取邮件列表
                                    loading = true
                                    fetchEmails { emails ->
                                        emailList = emails
                                        loading = false
                                    }
                                }
                            }) {
                                Text(text = if (account == null) "Login with Gmail" else "Show Emails")
                            }
                        } else if (loading) {
                            CircularProgressIndicator()
                        } else {
                            LazyColumn {
                                items(emailList) { email ->
                                    EmailItem(subject = email)
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    /**
     * 从 Gmail API 获取邮件列表
     */
    private fun fetchEmails(callback: (List<String>) -> Unit) {
        val credential = GoogleAccountCredential.usingOAuth2(
            this, listOf(GmailScopes.GMAIL_READONLY)
        )
        credential.selectedAccount = account?.account

        val gmailService = Gmail.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("Email Viewer").build()

        // 在后台线程中执行网络请求
        Thread {
            try {
                val messages =
                    gmailService.users().messages().list("me").execute().messages ?: emptyList()
                val emails = messages.take(10).map { message: Message ->
                    val fullMessage =
                        gmailService.users().messages().get("me", message.id).execute()
                    fullMessage.payload.headers.find { it.name == "Subject" }?.value ?: "No Subject"
                }
                runOnUiThread {
                    callback(emails)
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
     * 单个邮件项的 UI 组件
     */
    @Composable
    fun EmailItem(subject: String) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = 4.dp
        ) {
            Text(
                text = subject,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}