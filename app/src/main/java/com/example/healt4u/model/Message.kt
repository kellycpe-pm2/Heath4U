package com.example.healt4u.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Int,

    @SerialName("conversation_id")
    val conversationId: Int,

    val content: String,

    @SerialName("sender_id")
    val senderId: Int,

    @SerialName("sender_name")
    val senderName: String,

    val timestamp: String,

    val type: String = "text"
)