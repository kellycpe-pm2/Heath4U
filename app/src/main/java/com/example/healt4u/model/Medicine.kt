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

    val ic: String? = "1"
)