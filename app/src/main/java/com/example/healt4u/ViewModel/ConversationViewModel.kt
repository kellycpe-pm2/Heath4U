package com.example.healt4u.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Storage.getConversationsByPatient
import com.example.healt4u.Storage.getMessagesByConversation
import com.example.healt4u.Storage.sendMessage
import com.example.healt4u.model.Conversation
import com.example.healt4u.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ConversationViewModel.kt
class ConversationViewModel(
    private val context: Context
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun loadConversations(patientId: String) {
        viewModelScope.launch {
            val list = getConversationsByPatient(context, patientId)
            _conversations.value = list
        }
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            val list = getMessagesByConversation(context, conversationId)
            _messages.value = list
        }
    }

    fun sendMessage(message: Message) {
        viewModelScope.launch {
            sendMessage(context, message)
            loadMessages(message.conversationId)
            loadConversations("p001")
        }
    }
}