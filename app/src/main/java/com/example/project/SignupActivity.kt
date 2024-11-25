package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val usernameInput: EditText = findViewById(R.id.signup_username)
        val passwordInput: EditText = findViewById(R.id.signup_password)
        val displayNameInput: EditText = findViewById(R.id.signup_display_name)
        val emailInput: EditText = findViewById(R.id.signup_email)
        val businessPhoneInput: EditText = findViewById(R.id.signup_businessPhone)
        val accountTypeGroup: RadioGroup = findViewById(R.id.account_type_group)

        val cancelButton: Button = findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            finish()
        }

        val signupButton: Button = findViewById(R.id.register_button)
        signupButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val displayName = displayNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val businessPhone = businessPhoneInput.text.toString().trim()

            val accountType = when (accountTypeGroup.checkedRadioButtonId) {
                R.id.agent_radio -> 0
                R.id.driver_radio -> 1
                else -> -1
            }

            if (username.isEmpty()) {
                Toast.makeText(this, "Please fill the username", Toast.LENGTH_SHORT).show()
            }else if(password.isEmpty()) {
                Toast.makeText(this, "Please fill the password", Toast.LENGTH_SHORT).show()
            }else if(displayName.isEmpty()) {
                Toast.makeText(this, "Please fill the password", Toast.LENGTH_SHORT).show()
            }else if(email.isEmpty()) {
                Toast.makeText(this, "Please fill the email", Toast.LENGTH_SHORT).show()
            }else if(businessPhone.isEmpty()) {
                Toast.makeText(this, "Please fill the business phone", Toast.LENGTH_SHORT).show()
            }else if(accountType == -1) {
                Toast.makeText(this, "Please choose your account Type", Toast.LENGTH_SHORT).show()
            }else {
                registerUser(username, password, displayName, email, businessPhone, accountType)
            }
        }
    }

    private fun registerUser(username: String, password: String, displayName: String, email: String, businessPhone: String, accountType: Int) {

        Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}
