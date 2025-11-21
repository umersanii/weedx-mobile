package com.example.weedx

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.weedx.presentation.viewmodels.LoginViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    
    private lateinit var loginTab: TextView
    private lateinit var signUpTab: TextView
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var forgotPassword: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_login)
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        loginTab = findViewById(R.id.loginTab)
        signUpTab = findViewById(R.id.signUpTab)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        forgotPassword = findViewById(R.id.forgotPassword)
        progressBar = findViewById(R.id.progressBar)

        // Observe login state
        observeLoginState()

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

    private fun observeLoginState() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginViewModel.LoginState.Idle -> {
                        progressBar.visibility = View.GONE
                        loginButton.isEnabled = true
                    }
                    is LoginViewModel.LoginState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        loginButton.isEnabled = false
                    }
                    is LoginViewModel.LoginState.Success -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        
                        // Navigate to dashboard
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is LoginViewModel.LoginState.Error -> {
                        progressBar.visibility = View.GONE
                        loginButton.isEnabled = true
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                        emailInput.error = "Invalid credentials"
                    }
                }
            }
        }
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

        // Call ViewModel to perform login
        viewModel.login(email, password)
    }

    private fun handleForgotPassword() {
        Toast.makeText(this, "Forgot Password feature coming soon", Toast.LENGTH_SHORT).show()
    }
}
