package com.example.weedx

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weedx.presentation.viewmodels.AssistantViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AssistantActivity : AppCompatActivity() {

    private val viewModel: AssistantViewModel by viewModels()

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var initialContentScrollView: ScrollView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var suggestionCard1: CardView
    private lateinit var suggestionCard2: CardView
    private lateinit var suggestionCard3: CardView
    private lateinit var suggestionCard4: CardView

    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_assistant)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        initialContentScrollView = findViewById(R.id.initialContentScrollView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        suggestionCard1 = findViewById(R.id.suggestionCard1)
        suggestionCard2 = findViewById(R.id.suggestionCard2)
        suggestionCard3 = findViewById(R.id.suggestionCard3)
        suggestionCard4 = findViewById(R.id.suggestionCard4)

        setupRecyclerView()
        setupClickListeners()
        setupBottomNavigation()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chatMessages)
        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AssistantActivity)
            adapter = chatAdapter
            visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.messages.collectLatest { messages ->
                chatMessages.clear()
                chatMessages.addAll(messages)
                chatAdapter.notifyDataSetChanged()
                
                if (messages.isNotEmpty()) {
                    initialContentScrollView.visibility = View.GONE
                    chatRecyclerView.visibility = View.VISIBLE
                    chatRecyclerView.scrollToPosition(messages.size - 1)
                } else {
                    initialContentScrollView.visibility = View.VISIBLE
                    chatRecyclerView.visibility = View.GONE
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                sendButton.isEnabled = !isLoading
                messageInput.isEnabled = !isLoading
            }
        }
        
        lifecycleScope.launch {
            viewModel.chatState.collectLatest { state ->
                when (state) {
                    is AssistantViewModel.ChatState.Error -> {
                        Toast.makeText(
                            this@AssistantActivity,
                            "Error: ${state.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> { /* Handle other states if needed */ }
                }
            }
        }
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            sendMessage()
        }

        suggestionCard1.setOnClickListener {
            handleSuggestedQuestion(getString(R.string.question_weeds_today))
        }

        suggestionCard2.setOnClickListener {
            handleSuggestedQuestion(getString(R.string.question_battery_status))
        }

        suggestionCard3.setOnClickListener {
            handleSuggestedQuestion(getString(R.string.question_robot_doing))
        }

        suggestionCard4.setOnClickListener {
            handleSuggestedQuestion(getString(R.string.question_weekly_summary))
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
                    // Already on assistant
                    true
                }
                R.id.nav_images -> {
                    val intent = Intent(this, ImageGalleryActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        // Set assistant as selected
        bottomNavigation.selectedItemId = R.id.nav_assistant
    }

    private fun sendMessage() {
        val message = messageInput.text.toString().trim()
        if (message.isNotEmpty()) {
            handleSuggestedQuestion(message)
            messageInput.text.clear()
        }
    }

    private fun handleSuggestedQuestion(question: String) {
        // Hide initial content and show chat
        initialContentScrollView.visibility = View.GONE
        chatRecyclerView.visibility = View.VISIBLE

        // Send query to backend via ViewModel
        viewModel.sendQuery(question)
    }
}

