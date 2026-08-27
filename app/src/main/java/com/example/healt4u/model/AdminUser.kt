package com.example.healt4u.model

import kotlinx.serialization.Serializable

@Serializable
data class AdminUser(
    val id: Int = 0,
    val username: String,
    val password: String
)
