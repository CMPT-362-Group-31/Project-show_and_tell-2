package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import java.util.ArrayList

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)


        // Check and request necessary permissions
        if (!Util.hasAllPermissions(this)) {
            Util.requestPermissions(this)
        }
        setupUI()
    }

    private fun authenticateUser(): Boolean {
        // Your authentication logic
        return true // Assume success for now
    }

    private fun setupUI() {

        val loginButton: Button = findViewById(R.id.login_button)
        loginButton.setOnClickListener {
            if (authenticateUser()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        val signupButton: Button = findViewById(R.id.signup_button)
        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Util.handlePermissionResult(
            requestCode,
            grantResults,
            onPermissionsGranted = {
                setupUI()
            }
        )
    }
}
