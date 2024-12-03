package com.example.project.ui.email

data class Email(
    val id: String = "",
    val subject: String = "",
    val from: String = "",
    val content: String = "",
    val worksheetId: String = "" // Ensure this field exists
)
