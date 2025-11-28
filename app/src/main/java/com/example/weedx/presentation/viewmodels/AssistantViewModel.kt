package com.example.weedx.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedx.ChatMessage
import com.example.weedx.data.models.response.AssistantHistory
import com.example.weedx.data.repositories.AssistantRepository
import com.example.weedx.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val assistantRepository: AssistantRepository
) : ViewModel() {
    
    private val _chatState = MutableStateFlow<ChatState>(ChatState.Idle)
    val chatState: StateFlow<ChatState> = _chatState
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    fun sendQuery(query: String) {
        viewModelScope.launch {
            // Add user message immediately
            val currentMessages = _messages.value.toMutableList()
            currentMessages.add(ChatMessage(query, isUser = true))
            
            // Add typing indicator
            currentMessages.add(ChatMessage("", isUser = false, isTyping = true))
            _messages.value = currentMessages
            
            _isLoading.value = true
            _chatState.value = ChatState.Loading
            
            when (val result = assistantRepository.sendQuery(query)) {
                is NetworkResult.Success -> {
                    // Remove typing indicator and add response
                    val updatedMessages = _messages.value.toMutableList()
                    updatedMessages.removeAll { it.isTyping }
                    updatedMessages.add(ChatMessage(result.data.response, isUser = false))
                    _messages.value = updatedMessages
                    _chatState.value = ChatState.Success
                }
                is NetworkResult.Error -> {
                    // Remove typing indicator and add error message
                    val updatedMessages = _messages.value.toMutableList()
                    updatedMessages.removeAll { it.isTyping }
                    updatedMessages.add(ChatMessage(
                        "Sorry, I couldn't process your request. Please try again.",
                        isUser = false
                    ))
                    _messages.value = updatedMessages
                    _chatState.value = ChatState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _chatState.value = ChatState.Loading
                }
            }
            _isLoading.value = false
        }
    }
    
    fun loadHistory() {
        viewModelScope.launch {
            _chatState.value = ChatState.Loading
            
            when (val result = assistantRepository.getHistory()) {
                is NetworkResult.Success -> {
                    // Convert history items to ChatMessages
                    val historyMessages = result.data.map { history ->
                        ChatMessage(history.message, isUser = history.isUser)
                    }
                    _messages.value = historyMessages
                    _chatState.value = if (historyMessages.isEmpty()) ChatState.Idle else ChatState.Success
                }
                is NetworkResult.Error -> {
                    _chatState.value = ChatState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _chatState.value = ChatState.Loading
                }
            }
        }
    }
    
    fun clearChat() {
        _messages.value = emptyList()
        _chatState.value = ChatState.Idle
    }
    
    fun hasMessages(): Boolean = _messages.value.isNotEmpty()
    
    sealed class ChatState {
        object Idle : ChatState()
        object Loading : ChatState()
        object Success : ChatState()
        data class Error(val message: String) : ChatState()
    }
}
