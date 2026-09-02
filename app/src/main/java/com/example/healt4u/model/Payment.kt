package com.example.healt4u.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: String? = "",

    @SerialName("patient_id")
    val patientId: Int = 0,

    @SerialName("doctor_id")
    val doctorId: Int = 0,

    @SerialName("doctor_name")
    val doctorName: String = "",

    val amount: Double = 0.0,

    val date: String = "",

    val time: String = "",

    val status: String = "PENDING",

    @SerialName("method")
    val paymentMethod: String = "",

)