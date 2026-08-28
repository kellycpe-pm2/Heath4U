package com.example.healt4u.ViewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Storage.getConversationsByDoctor
import com.example.healt4u.Storage.getConversationsByPatient
import com.example.healt4u.Storage.getMessagesByConversation
import com.example.healt4u.model.Conversation
import com.example.healt4u.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConversationViewModel : ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()


    @RequiresApi(Build.VERSION_CODES.O)
    fun loadConversations(patientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val list = getConversationsByPatient(patientId)
                _conversations.value = list
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load conversations"
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadConversationsForDoctor(doctorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val list = getConversationsByDoctor(doctorId)
                _conversations.value = list
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load conversations"
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val list = getMessagesByConversation(conversationId)
                _messages.value = list
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load messages"
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(message: Message, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _messages.update { currentList ->
                    if (currentList.none { it.id == message.id }) {
                        currentList + message
                    } else {
                        currentList
                    }
                }

                val success = com.example.healt4u.Storage.sendMessage(message)

                if (success) {
                    loadMessages(message.conversationId)
                    val patientId = message.conversationId.split("_").lastOrNull() ?: ""
                    if (patientId.isNotEmpty()) {
                        loadConversations(patientId)
                    }
                    onSuccess?.invoke()
                } else {
                    _messages.update { currentList ->
                        currentList.filter { it.id != message.id }
                    }
                    _errorMessage.value = "Failed to send message"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to send message"
                _messages.update { currentList ->
                    currentList.filter { it.id != message.id }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun clearConversations() {
        _conversations.value = emptyList()
    }
}