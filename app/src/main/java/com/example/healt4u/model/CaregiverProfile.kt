package com.example.healt4u.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CaregiverProfile(
    val id: String,

    val name: String,

    val phone: String,

    val relationship: String,

    @SerialName("patient_phone")
    val patientPhone: String = "",

    @SerialName("patient_id")
    val patientId: String = "p001",

    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
