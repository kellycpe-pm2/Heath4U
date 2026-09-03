package com.example.healt4u.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Medicine(
    val id: Int,

    val name_medicine: String,

    val category: String,

    val dosage: Int,

    val quantity: Int,

    @SerialName("quantity_left")
    val quantityLeft: Int? = 0,

    val remark: String? = "",

    @SerialName("expired_date")
    val expiredDate: Long? = null,

    @SerialName("after_eat")
    val afterEat: Boolean? = true,

    @SerialName("create_date")
    val createDate: Long? = null,

    val priority: Float? = 0f,

    // First dose time of the day, e.g. "08:00". Later doses are spaced evenly
    // across the day based on timesPerDay. Used by the Smart Reminder / Schedule module.
    @SerialName("reminder_time")
    val reminderTime: String? = "08:00",

    // How many times a day this medicine should be taken (e.g. 3 = 08:00, 13:00, 18:00)
    @SerialName("times_per_day")
    val timesPerDay: Int? = 1,

    // Which patient this medicine belongs to. Nullable/defaulted so existing
    // rows without the column still decode fine; used to scope cloud fetches
    // per-account instead of pulling every patient's medicines.
    @SerialName("patient_id")
    val patientId: Int?
)