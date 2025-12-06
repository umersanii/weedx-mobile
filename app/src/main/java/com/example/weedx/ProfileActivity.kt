package com.example.weedx

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.weedx.data.models.response.FarmInfo
import com.example.weedx.data.models.response.ProfileResponse
import com.example.weedx.data.models.response.UserProfile
import com.example.weedx.presentation.viewmodels.ProfileViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var notificationsItem: LinearLayout
    private lateinit var notificationsToggle: SwitchMaterial
    private lateinit var helpSupportItem: LinearLayout
    private lateinit var logoutButton: CardView
    private lateinit var editProfileButton: MaterialButton
    
    // Profile views
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var memberSince: TextView
    private lateinit var farmName: TextView
    private lateinit var totalArea: TextView
    private lateinit var farmLocation: TextView
    private var loadingIndicator: ProgressBar? = null
    
    // Activity result launcher for edit profile
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Profile was updated, refresh the data
            viewModel.refresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_profile)
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupClickListeners()
        setupBottomNavigation()
        observeProfileState()
        
        // Load profile data from API
        viewModel.loadProfile()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        notificationsItem = findViewById(R.id.notificationsItem)
        notificationsToggle = findViewById(R.id.notificationsToggle)
        helpSupportItem = findViewById(R.id.helpSupportItem)
        logoutButton = findViewById(R.id.logoutButton)
        editProfileButton = findViewById(R.id.editProfileButton)
        
        // Profile data views
        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEmail)
        memberSince = findViewById(R.id.memberSince)
        farmName = findViewById(R.id.farmName)
        totalArea = findViewById(R.id.totalArea)
        farmLocation = findViewById(R.id.farmLocation)
        loadingIndicator = findViewById(R.id.loadingIndicator)
    }
    
    private fun observeProfileState() {
        lifecycleScope.launch {
            viewModel.profileState.collect { state ->
                when (state) {
                    is ProfileViewModel.ProfileState.Idle -> {
                        // Initial state - show default data
                        showDefaultData()
                    }
                    is ProfileViewModel.ProfileState.Loading -> {
                        showLoading(true)
                    }
                    is ProfileViewModel.ProfileState.Success -> {
                        showLoading(false)
                        updateUI(state.data)
                    }
                    is ProfileViewModel.ProfileState.Error -> {
                        showLoading(false)
                        // Show default data on error
                        showDefaultData()
                        Toast.makeText(this@ProfileActivity, "Using offline data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        loadingIndicator?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    
    private fun updateUI(profile: ProfileResponse) {
        // Update user info
        updateUserInfo(profile.user)
        
        // Update farm info
        profile.farm?.let { updateFarmInfo(it) } ?: updateFarmInfo(ProfileViewModel.DEFAULT_FARM)
        
        // Update settings
        profile.settings?.let { 
            notificationsToggle.isChecked = it.notifications
        }
    }
    
    private fun updateUserInfo(user: UserProfile) {
        userName.text = user.name
        userEmail.text = user.email
        memberSince.text = formatMemberSince(user.createdAt)
    }
    
    private fun updateFarmInfo(farm: FarmInfo) {
        farmName.text = farm.name
        totalArea.text = "${farm.area?.toInt() ?: 0} hectares"
        farmLocation.text = farm.location ?: "Not set"
    }
    
    private fun showDefaultData() {
        updateUserInfo(ProfileViewModel.DEFAULT_USER)
        updateFarmInfo(ProfileViewModel.DEFAULT_FARM)
        farmLocation.text = "Not set"
    }
    
    private fun formatMemberSince(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            "Member since ${outputFormat.format(date!!)}"
        } catch (e: Exception) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                "Member since ${outputFormat.format(date!!)}"
            } catch (e2: Exception) {
                "Member since Jan 2024"
            }
        }
    }

    private fun setupClickListeners() {
        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)
        }
        
        notificationsItem.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }
        
        notificationsToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateNotificationSetting(isChecked)
        }

        helpSupportItem.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://umersanii.vercel.app"))
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            // Navigate back to login screen
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_weather -> {
                    val intent = Intent(this, WeatherActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_assistant -> {
                    val intent = Intent(this, AssistantActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_images -> {
                    val intent = Intent(this, ImageGalleryActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    // Already on profile screen
                    true
                }
                else -> false
            }
        }

        // Set profile as selected
        bottomNavigation.selectedItemId = R.id.nav_profile
    }
}
