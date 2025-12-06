package com.example.weedx

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.weedx.data.models.response.UserProfile
import com.example.weedx.presentation.viewmodels.ProfileViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var toolbar: MaterialToolbar
    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var loadingIndicator: ProgressBar

    private var currentUser: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_edit_profile)
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupClickListeners()
        observeProfileState()
        observeUpdateState()
        
        // Load current profile data
        viewModel.loadProfile()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveProfileChanges()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun observeProfileState() {
        lifecycleScope.launch {
            viewModel.profileState.collect { state ->
                when (state) {
                    is ProfileViewModel.ProfileState.Success -> {
                        currentUser = state.data.user
                        populateFields(state.data.user)
                    }
                    is ProfileViewModel.ProfileState.Error -> {
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Failed to load profile",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeUpdateState() {
        lifecycleScope.launch {
            viewModel.updateState.collect { state ->
                when (state) {
                    is ProfileViewModel.UpdateState.Idle -> {
                        showLoading(false)
                    }
                    is ProfileViewModel.UpdateState.Loading -> {
                        showLoading(true)
                    }
                    is ProfileViewModel.UpdateState.Success -> {
                        showLoading(false)
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                    is ProfileViewModel.UpdateState.Error -> {
                        showLoading(false)
                        Toast.makeText(
                            this@EditProfileActivity,
                            state.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun populateFields(user: UserProfile) {
        nameInput.setText(user.name)
        emailInput.setText(user.email)
        phoneInput.setText(user.phone ?: "")
    }

    private fun saveProfileChanges() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            nameInput.error = "Name is required"
            nameInput.requestFocus()
            return
        }

        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            emailInput.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Invalid email format"
            emailInput.requestFocus()
            return
        }

        // Build update map
        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "email" to email
        )

        if (phone.isNotEmpty()) {
            updates["phone"] = phone
        }

        // Call ViewModel to update profile
        viewModel.updateProfile(updates)
    }

    private fun showLoading(isLoading: Boolean) {
        loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        saveButton.isEnabled = !isLoading
        cancelButton.isEnabled = !isLoading
        nameInput.isEnabled = !isLoading
        emailInput.isEnabled = !isLoading
        phoneInput.isEnabled = !isLoading
    }

    companion object {
        const val REQUEST_EDIT_PROFILE = 1001
    }
}
