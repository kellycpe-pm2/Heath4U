package com.example.healt4u.model

data class Conversation(
    val id: String = "",
    val doctorId: Int = 0,
    val doctorName: String = "",
    val doctorSpecialty: String = "",
    val hospitalId: Int = 0,
    val hospitalName: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
