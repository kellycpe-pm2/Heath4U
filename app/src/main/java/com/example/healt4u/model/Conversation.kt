package com.example.healt4u.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: String,

    @SerialName("doctor_id")
    val doctorId: Int,

    @SerialName("doctor_name")
    val doctorName: String,

    @SerialName("patient_id")
    val patientId: Int,

    @SerialName("patient_name")
    val patientName: String,

    @SerialName("hospital_id")
    val hospitalId: String,

    @SerialName("hospital_name")
    val hospitalName: String,

    @SerialName("last_message")
    val lastMessage: String,

    @SerialName("last_message_time")
    val lastMessageTime: String,

    @SerialName("unread_count")
    val unreadCount: Int,

    @SerialName("is_active")
    val isActive: Boolean
)