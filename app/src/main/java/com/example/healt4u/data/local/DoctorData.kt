package com.example.healt4u.data.local

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import io.github.jan.supabase.auth.providers.invoke

object DoctorData {
    val statuses = listOf("AVAILABLE", "BUSY", "OFFLINE")
    val colors = listOf(Color(0xFF54D567), Color(0xFFD55456), Color(0xFFA9A6A7))
    val statusPairs: List<Pair<String, Color>> = statuses.zip(colors)

}