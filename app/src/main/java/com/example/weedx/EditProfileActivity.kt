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
import com.example.weedx.data.models.response.FarmInfo
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
    private lateinit var farmNameInput: TextInputEditText
    private lateinit var farmLocationInput: TextInputEditText
    private lateinit var farmSizeInput: TextInputEditText
    private lateinit var cropTypesInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var loadingIndicator: ProgressBar

    private var currentUser: UserProfile? = null
    private var currentFarm: FarmInfo? = null

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
        farmNameInput = findViewById(R.id.farmNameInput)
        farmLocationInput = findViewById(R.id.farmLocationInput)
        farmSizeInput = findViewById(R.id.farmSizeInput)
        cropTypesInput = findViewById(R.id.cropTypesInput)
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
                        currentFarm = state.data.farm
                        populateFields(state.data.user, state.data.farm)
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
                        // Check if farm update is also complete
                        checkAndFinish()
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
        
        lifecycleScope.launch {
            viewModel.farmUpdateState.collect { state ->
                when (state) {
                    is ProfileViewModel.FarmUpdateState.Success -> {
                        // Check if user update is also complete
                        checkAndFinish()
                    }
                    is ProfileViewModel.FarmUpdateState.Error -> {
                        showLoading(false)
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Farm: ${state.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun checkAndFinish() {
        val userState = viewModel.updateState.value
        val farmState = viewModel.farmUpdateState.value
        
        if (userState is ProfileViewModel.UpdateState.Success && 
            (farmState is ProfileViewModel.FarmUpdateState.Success || farmState is ProfileViewModel.FarmUpdateState.Idle)) {
            showLoading(false)
            Toast.makeText(
                this@EditProfileActivity,
                "Profile updated successfully",
                Toast.LENGTH_SHORT
            ).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun populateFields(user: UserProfile, farm: FarmInfo?) {
        nameInput.setText(user.name)
        emailInput.setText(user.email)
        phoneInput.setText(user.phone ?: "")
        
        farm?.let {
            farmNameInput.setText(it.name)
            farmLocationInput.setText(it.location ?: "")
            farmSizeInput.setText(it.area?.toString() ?: "")
            cropTypesInput.setText(it.cropTypes ?: "")
        }
    }

    private fun saveProfileChanges() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val farmName = farmNameInput.text.toString().trim()
        val farmLocation = farmLocationInput.text.toString().trim()
        val farmSize = farmSizeInput.text.toString().trim()
        val cropTypes = cropTypesInput.text.toString().trim()

        // Validation - User fields
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

        // Build user update map
        val userUpdates = mutableMapOf<String, Any>(
            "name" to name,
            "email" to email
        )

        if (phone.isNotEmpty()) {
            userUpdates["phone"] = phone
        }

        // Call ViewModel to update profile
        viewModel.updateProfile(userUpdates)
        
        // Update farm if fields are provided
        if (farmName.isNotEmpty() || farmLocation.isNotEmpty() || farmSize.isNotEmpty()) {
            val farmUpdates = mutableMapOf<String, Any>()
            
            if (farmName.isNotEmpty()) {
                farmUpdates["name"] = farmName
            }
            
            if (farmLocation.isNotEmpty()) {
                farmUpdates["location"] = farmLocation
            }
            
            if (farmSize.isNotEmpty()) {
                try {
                    val size = farmSize.toDouble()
                    if (size > 0) {
                        farmUpdates["size"] = size
                    } else {
                        farmSizeInput.error = "Size must be positive"
                        farmSizeInput.requestFocus()
                        return
                    }
                } catch (e: NumberFormatException) {
                    farmSizeInput.error = "Invalid number"
                    farmSizeInput.requestFocus()
                    return
                }
            }
            
            if (cropTypes.isNotEmpty()) {
                farmUpdates["crop_types"] = cropTypes
            }
            
            if (farmUpdates.isNotEmpty()) {
                viewModel.updateFarmInfo(farmUpdates)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        saveButton.isEnabled = !isLoading
        cancelButton.isEnabled = !isLoading
        nameInput.isEnabled = !isLoading
        emailInput.isEnabled = !isLoading
        phoneInput.isEnabled = !isLoading
        farmNameInput.isEnabled = !isLoading
        farmLocationInput.isEnabled = !isLoading
        farmSizeInput.isEnabled = !isLoading
        cropTypesInput.isEnabled = !isLoading
    }

    companion object {
        const val REQUEST_EDIT_PROFILE = 1001
    }
}
