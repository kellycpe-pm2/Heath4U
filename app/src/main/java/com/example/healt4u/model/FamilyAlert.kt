package com.example.healt4u.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FamilyAlert(
    val id: String,

    @SerialName("medicine_name")
    val medicineName: String,

    @SerialName("scheduled_time")
    val scheduledTime: String,

    val date: String,

    @SerialName("patient_phone")
    val patientPhone: String,

    // "PENDING" | "CALLED" | "RESOLVED"
    val status: String = "PENDING",

    @SerialName("caregiver_name")
    val caregiverName: String,

    @SerialName("caregiver_phone")
    val caregiverPhone: String = "",

    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @SerialName("resolved_at")
    val resolvedAt: Long? = null
)
