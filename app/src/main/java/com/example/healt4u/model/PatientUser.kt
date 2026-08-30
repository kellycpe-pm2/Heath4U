package com.example.healt4u.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatientUser(
    val id: Int = 0,
    val ic: String = "",
    @SerialName("patient_name")
    val name: String = "",
    val email: String? = null,
    val phone: String? = null,
    val password: String = "",
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
