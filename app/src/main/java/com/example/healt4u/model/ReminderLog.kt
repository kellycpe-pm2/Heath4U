package com.example.healt4u.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// One row = one dose slot for one medicine on one day.
// id is a composite key so it stays stable across reloads: "{medicineId}_{date}_{slotIndex}"
@Serializable
data class ReminderLog(
    val id: String,

    @SerialName("medicine_id")
    val medicineId: Int,

    @SerialName("medicine_name")
    val medicineName: String,

    // yyyy-MM-dd
    val date: String,

    // HH:mm, 24-hour
    val time: String,

    // "PENDING" | "TAKEN" | "MISSED"
    val status: String = "PENDING"
)
