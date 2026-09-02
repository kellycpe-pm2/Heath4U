package com.example.healt4u.Storage

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.healt4u.model.Payment
import com.example.healt4u.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
private fun getCurrentDateText(): String {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getCurrentTimeText(): String {
    return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun createPayment(payment: Payment): Payment? {
    return try {
        val record = payment.copy(id = null)

        val saved = SupabaseClient.supabase
            .from("payments")
            .insert(record) { select() }
            .decodeList<Map<String, JsonElement>>()

        if (saved.isEmpty()) return null
        val row = saved.first()

        payment.copy(
            id = row["id"]?.jsonPrimitive?.content
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error creating payment: ${e.message}", e)
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getPaymentsByDoctor(doctorId: String): List<Payment> {
    return try {
        val response = SupabaseClient.supabase
            .from("payments")
            .select {
                filter { eq("doctor_id", doctorId) }
            }
        Json.decodeFromString<List<Payment>>(response.data).sortedByDescending { it.date }
    } catch (e: Exception) {
        Log.e(TAG, "Error loading payments: ${e.message}", e)
        emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getDoctorRevenueStats(doctorId: String): Map<String, Double> {
    return try {
        val payments = getPaymentsByDoctor(doctorId)
            .filter { it.status.equals("completed", ignoreCase = true) && it.amount > 0 }

        val todayDate = getCurrentDateText()
        val today = payments.filter { it.date == todayDate }.sumOf { it.amount }

        val total = payments.sumOf { it.amount }

        val thisMonth = LocalDate.now().monthValue
        val thisYear = LocalDate.now().year
        val month = payments.filter { p ->
            try {
                val d = LocalDate.parse(p.date)
                d.monthValue == thisMonth && d.year == thisYear
            } catch (e: Exception) { false }
        }.sumOf { it.amount }

        mapOf(
            "total" to total,
            "today" to today,
            "month" to month
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error calculating revenue: ${e.message}", e)
        mapOf("total" to 0.0, "today" to 0.0, "month" to 0.0)
    }
}