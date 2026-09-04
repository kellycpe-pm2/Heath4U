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

@RequiresApi(Build.VERSION_CODES.O)
class ConversationViewModel : ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadConversationsForPatient(patientId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val list = getConversationsByPatient(patientId)
                _conversations.update { list }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load conversations"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadConversationsForDoctor(doctorId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val list = getConversationsByDoctor(doctorId)
                _conversations.update { list }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load conversations"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMessages(conversationId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val list = getMessagesByConversation(conversationId)
                _messages.update { list }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load messages"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _messages.update { emptyList() }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}