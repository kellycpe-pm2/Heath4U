package com.example.healt4u.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Hospital(
    val id: Int = 0,
    val name: String,
    val address: String,
    val phone: String,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Doctor(
    val id: Int = 0,
    val name: String,
    val ic: String,
    val password: String = "",
    val phone: String,
    val email: String,
    @SerialName("consultation_fee") val consultationFee: Double,
    val specialization: String,
    val status: String = "available",
    @SerialName("hospital_id") val hospitalId: Int?,
    @SerialName("qualification_doc_url") val qualificationDocUrl: String? = null,
    @SerialName("verification_status") val verificationStatus: String = "pending",
    @SerialName("created_at") val createdAt: String?= null
)

