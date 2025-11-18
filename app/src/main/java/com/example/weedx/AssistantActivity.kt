package com.example.weedx

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AssistantActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_assistant)

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
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chatMessages)
        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AssistantActivity)
            adapter = chatAdapter
            visibility = View.GONE
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

        // Add user message
        chatMessages.add(ChatMessage(question, true))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        chatRecyclerView.scrollToPosition(chatMessages.size - 1)

        // Simulate assistant response
        simulateAssistantResponse(question)
    }

    private fun simulateAssistantResponse(question: String) {
        // Add a delay to simulate processing
        messageInput.postDelayed({
            val response = getResponseForQuestion(question)
            chatMessages.add(ChatMessage(response, false))
            chatAdapter.notifyItemInserted(chatMessages.size - 1)
            chatRecyclerView.scrollToPosition(chatMessages.size - 1)
        }, 1000)
    }

    private fun getResponseForQuestion(question: String): String {
        return when {
            question.contains("weeds", ignoreCase = true) && 
            question.contains("today", ignoreCase = true) -> {
                "Today, 142 weeds have been detected across Field A. The detection was last updated 2 minutes ago."
            }
            question.contains("battery", ignoreCase = true) -> {
                "The robot's battery level is at 87%. It has enough charge for approximately 4 more hours of operation."
            }
            question.contains("robot", ignoreCase = true) && 
            question.contains("doing", ignoreCase = true) -> {
                "The robot is currently active in Field A, located at position A-12. It's moving at a speed of 2.4 and actively detecting and treating weeds."
            }
            question.contains("weekly", ignoreCase = true) || 
            question.contains("summary", ignoreCase = true) -> {
                "This week, the robot has covered 42.5 hectares, detected 856 weeds, and used 18.6L of herbicide through targeted treatment. The average efficiency is 95.2%."
            }
            else -> {
                "I can help you with information about your robot status, weed detection data, battery levels, field coverage, and weekly summaries. What specific information would you like to know?"
            }
        }
    }
}
