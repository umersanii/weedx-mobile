package com.example.weedx

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.weedx.presentation.viewmodels.SignUpViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private val viewModel: SignUpViewModel by viewModels()
    
    // Step indicators
    private lateinit var step1Indicator: TextView
    private lateinit var step2Indicator: TextView
    private lateinit var step3Indicator: TextView
    private lateinit var step1Label: TextView
    private lateinit var step2Label: TextView
    private lateinit var step3Label: TextView
    private lateinit var line1: View
    private lateinit var line2: View
    
    // View flipper
    private lateinit var viewFlipper: ViewFlipper
    
    // Step 1: User Info
    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    
    // Step 2: Farm Info
    private lateinit var farmNameInput: TextInputEditText
    private lateinit var farmLocationInput: TextInputEditText
    private lateinit var farmSizeInput: TextInputEditText
    private lateinit var cropChipGroup: ChipGroup
    
    // Step 3: Settings
    private lateinit var notificationsSwitch: SwitchMaterial
    private lateinit var emailAlertsSwitch: SwitchMaterial
    private lateinit var languageDropdown: AutoCompleteTextView
    private lateinit var themeRadioGroup: RadioGroup
    private lateinit var termsCheckbox: CheckBox
    
    // Navigation
    private lateinit var backButton: ImageButton
    private lateinit var prevButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var loginLink: TextView
    
    private var currentStep = 0
    private val cropChips = mutableMapOf<String, Chip>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_signup)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        setupLanguageDropdown()
        observeSignUpState()
        updateStepUI()
    }

    private fun initViews() {
        // Step indicators
        step1Indicator = findViewById(R.id.step1Indicator)
        step2Indicator = findViewById(R.id.step2Indicator)
        step3Indicator = findViewById(R.id.step3Indicator)
        step1Label = findViewById(R.id.step1Label)
        step2Label = findViewById(R.id.step2Label)
        step3Label = findViewById(R.id.step3Label)
        line1 = findViewById(R.id.line1)
        line2 = findViewById(R.id.line2)
        
        viewFlipper = findViewById(R.id.viewFlipper)
        
        // Step 1
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        
        // Step 2
        farmNameInput = findViewById(R.id.farmNameInput)
        farmLocationInput = findViewById(R.id.farmLocationInput)
        farmSizeInput = findViewById(R.id.farmSizeInput)
        cropChipGroup = findViewById(R.id.cropChipGroup)
        
        // Initialize crop chips
        cropChips["Wheat"] = findViewById(R.id.chipWheat)
        cropChips["Corn"] = findViewById(R.id.chipCorn)
        cropChips["Rice"] = findViewById(R.id.chipRice)
        cropChips["Soybeans"] = findViewById(R.id.chipSoybeans)
        cropChips["Cotton"] = findViewById(R.id.chipCotton)
        cropChips["Vegetables"] = findViewById(R.id.chipVegetables)
        
        // Step 3
        notificationsSwitch = findViewById(R.id.notificationsSwitch)
        emailAlertsSwitch = findViewById(R.id.emailAlertsSwitch)
        languageDropdown = findViewById(R.id.languageDropdown)
        themeRadioGroup = findViewById(R.id.themeRadioGroup)
        termsCheckbox = findViewById(R.id.termsCheckbox)
        
        // Navigation
        backButton = findViewById(R.id.backButton)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)
        progressBar = findViewById(R.id.progressBar)
        loginLink = findViewById(R.id.loginLink)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { finish() }
        
        prevButton.setOnClickListener {
            if (currentStep > 0) {
                currentStep--
                viewFlipper.showPrevious()
                updateStepUI()
            }
        }
        
        nextButton.setOnClickListener {
            if (validateCurrentStep()) {
                saveCurrentStepData()
                if (currentStep < 2) {
                    currentStep++
                    viewFlipper.showNext()
                    updateStepUI()
                } else {
                    performRegistration()
                }
            }
        }
        
        loginLink.setOnClickListener {
            finish()
        }
    }

    private fun setupLanguageDropdown() {
        val languages = arrayOf(
            getString(R.string.language_english),
            getString(R.string.language_spanish),
            getString(R.string.language_french),
            getString(R.string.language_german)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, languages)
        languageDropdown.setAdapter(adapter)
    }

    private fun validateCurrentStep(): Boolean {
        return when (currentStep) {
            0 -> validateStep1()
            1 -> validateStep2()
            2 -> validateStep3()
            else -> false
        }
    }

    private fun validateStep1(): Boolean {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        if (name.isEmpty()) {
            nameInput.error = getString(R.string.error_name_required)
            return false
        }
        if (email.isEmpty()) {
            emailInput.error = getString(R.string.error_email_required)
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = getString(R.string.error_invalid_email)
            return false
        }
        if (password.isEmpty()) {
            passwordInput.error = getString(R.string.error_password_required)
            return false
        }
        if (password.length < 6) {
            passwordInput.error = getString(R.string.error_password_length)
            return false
        }
        if (password != confirmPassword) {
            confirmPasswordInput.error = getString(R.string.error_password_mismatch)
            return false
        }
        return true
    }

    private fun validateStep2(): Boolean {
        val farmName = farmNameInput.text.toString().trim()
        val farmLocation = farmLocationInput.text.toString().trim()
        val farmSize = farmSizeInput.text.toString().trim()

        if (farmName.isEmpty()) {
            farmNameInput.error = getString(R.string.error_farm_name_required)
            return false
        }
        if (farmLocation.isEmpty()) {
            farmLocationInput.error = getString(R.string.error_farm_location_required)
            return false
        }
        if (farmSize.isEmpty()) {
            farmSizeInput.error = getString(R.string.error_farm_size_required)
            return false
        }
        if (farmSize.toDoubleOrNull() == null || farmSize.toDouble() <= 0) {
            farmSizeInput.error = getString(R.string.error_invalid_farm_size)
            return false
        }
        return true
    }

    private fun validateStep3(): Boolean {
        if (!termsCheckbox.isChecked) {
            Toast.makeText(this, R.string.error_accept_terms, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveCurrentStepData() {
        when (currentStep) {
            0 -> {
                viewModel.name = nameInput.text.toString().trim()
                viewModel.email = emailInput.text.toString().trim()
                viewModel.phone = phoneInput.text.toString().trim()
                viewModel.password = passwordInput.text.toString()
                viewModel.confirmPassword = confirmPasswordInput.text.toString()
            }
            1 -> {
                viewModel.farmName = farmNameInput.text.toString().trim()
                viewModel.farmLocation = farmLocationInput.text.toString().trim()
                viewModel.farmSize = farmSizeInput.text.toString().trim()
                
                // Get selected crops
                viewModel.selectedCropTypes.clear()
                cropChips.forEach { (cropName, chip) ->
                    if (chip.isChecked) {
                        viewModel.selectedCropTypes.add(cropName)
                    }
                }
            }
            2 -> {
                viewModel.notificationsEnabled = notificationsSwitch.isChecked
                viewModel.emailAlerts = emailAlertsSwitch.isChecked
                
                // Language
                viewModel.selectedLanguage = when (languageDropdown.text.toString()) {
                    getString(R.string.language_spanish) -> "es"
                    getString(R.string.language_french) -> "fr"
                    getString(R.string.language_german) -> "de"
                    else -> "en"
                }
                
                // Theme
                viewModel.selectedTheme = when (themeRadioGroup.checkedRadioButtonId) {
                    R.id.radioDark -> "dark"
                    R.id.radioSystem -> "system"
                    else -> "light"
                }
            }
        }
    }

    private fun updateStepUI() {
        // Update step indicators
        updateStepIndicator(step1Indicator, step1Label, currentStep >= 0, currentStep > 0)
        updateStepIndicator(step2Indicator, step2Label, currentStep >= 1, currentStep > 1)
        updateStepIndicator(step3Indicator, step3Label, currentStep >= 2, currentStep > 2)
        
        // Update lines
        line1.setBackgroundColor(getColor(if (currentStep > 0) R.color.green_primary else R.color.gray_border))
        line2.setBackgroundColor(getColor(if (currentStep > 1) R.color.green_primary else R.color.gray_border))
        
        // Update buttons
        prevButton.visibility = if (currentStep > 0) View.VISIBLE else View.GONE
        nextButton.text = if (currentStep == 2) getString(R.string.create_account) else getString(R.string.next)
        
        // Adjust button weights when prev is visible
        val prevParams = prevButton.layoutParams as android.widget.LinearLayout.LayoutParams
        val nextParams = nextButton.layoutParams as android.widget.LinearLayout.LayoutParams
        
        if (currentStep > 0) {
            prevParams.weight = 1f
            nextParams.weight = 1f
        } else {
            prevParams.weight = 0f
            nextParams.weight = 1f
        }
        
        prevButton.layoutParams = prevParams
        nextButton.layoutParams = nextParams
    }

    private fun updateStepIndicator(indicator: TextView, label: TextView, isActive: Boolean, isCompleted: Boolean) {
        when {
            isCompleted -> {
                indicator.setBackgroundResource(R.drawable.bg_step_completed)
                indicator.text = "✓"
                indicator.setTextColor(getColor(R.color.white))
                label.setTextColor(getColor(R.color.green_primary))
            }
            isActive -> {
                indicator.setBackgroundResource(R.drawable.bg_step_active)
                indicator.setTextColor(getColor(R.color.white))
                label.setTextColor(getColor(R.color.green_primary))
            }
            else -> {
                indicator.setBackgroundResource(R.drawable.bg_step_inactive)
                indicator.setTextColor(getColor(R.color.gray_text))
                label.setTextColor(getColor(R.color.gray_text))
            }
        }
    }

    private fun performRegistration() {
        saveCurrentStepData()
        viewModel.register()
    }

    private fun observeSignUpState() {
        lifecycleScope.launch {
            viewModel.signUpState.collect { state ->
                when (state) {
                    is SignUpViewModel.SignUpState.Idle -> {
                        progressBar.visibility = View.GONE
                        nextButton.isEnabled = true
                    }
                    is SignUpViewModel.SignUpState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        nextButton.isEnabled = false
                    }
                    is SignUpViewModel.SignUpState.Success -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@SignUpActivity, R.string.registration_success, Toast.LENGTH_SHORT).show()
                        
                        // Navigate to dashboard
                        val intent = Intent(this@SignUpActivity, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    is SignUpViewModel.SignUpState.Error -> {
                        progressBar.visibility = View.GONE
                        nextButton.isEnabled = true
                        Toast.makeText(this@SignUpActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
