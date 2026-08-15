package com.example.healt4u.model

data class Message(
    val id: String,
    val content: String,
    val senderId: String,
    val senderName: String,
    val timestamp:Long = System.currentTimeMillis(),
    val type: String,
    )
