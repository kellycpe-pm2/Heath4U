package com.example.healt4u.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CaregiverLink(
    val id: Int = 0,
    val username: String = "",
    val name: String = "",
    val phone: String = "",
    val relationship: String = "",
    val status: String = "ACCEPTED",
    @SerialName("caregiver_user_id")
    val caregiverUserId: Int = 0,
    @SerialName("patient_user_id")
    val patientUserId: Int = 0,
    @SerialName("caregiver_name")
    val caregiverName: String = "",
    @SerialName("caregiver_phone")
    val caregiverPhone: String = "",
    @SerialName("patient_name")
    val patientName: String = "",
    @SerialName("patient_phone")
    val patientPhone: String = "",
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
