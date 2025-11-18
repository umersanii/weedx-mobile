package com.example.weedx

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var loginTab: TextView
    private lateinit var signUpTab: TextView
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var forgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        loginTab = findViewById(R.id.loginTab)
        signUpTab = findViewById(R.id.signUpTab)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        forgotPassword = findViewById(R.id.forgotPassword)

        // Set up tab click listeners
        loginTab.setOnClickListener {
            selectLoginTab()
        }

        signUpTab.setOnClickListener {
            selectSignUpTab()
        }

        // Login button click listener
        loginButton.setOnClickListener {
            performLogin()
        }

        // Forgot password click listener
        forgotPassword.setOnClickListener {
            handleForgotPassword()
        }

        // Set login tab as selected by default
        selectLoginTab()
    }

    private fun selectLoginTab() {
        // Update tab styling
        loginTab.setBackgroundColor(Color.WHITE)
        loginTab.setTextColor(Color.BLACK)
        
        signUpTab.setBackgroundColor(getColor(R.color.gray_light))
        signUpTab.setTextColor(getColor(R.color.gray_text))
    }

    private fun selectSignUpTab() {
        // Update tab styling
        signUpTab.setBackgroundColor(Color.WHITE)
        signUpTab.setTextColor(Color.BLACK)
        
        loginTab.setBackgroundColor(getColor(R.color.gray_light))
        loginTab.setTextColor(getColor(R.color.gray_text))
        
        // TODO: Navigate to sign up screen or show sign up form
        Toast.makeText(this, "Sign Up feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Basic validation
        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            return
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            return
        }

        // Check demo credentials
        if (email == "admin@weedx.com" && password == "admin123") {
            // Login successful
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            
            // Navigate to dashboard
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Show error
            Toast.makeText(this, "Invalid credentials. Please use demo credentials.", Toast.LENGTH_SHORT).show()
            emailInput.error = "Invalid credentials"
        }
    }

    private fun handleForgotPassword() {
        Toast.makeText(this, "Forgot Password feature coming soon", Toast.LENGTH_SHORT).show()
    }
}
