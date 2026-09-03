package com.example.healt4u.screen.Medicine

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Helper functions
fun getCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "A" -> Color(0xFFE53935) // Red - Controlled medicines
        "B" -> Color(0xFF1E88E5) // Blue - Natural Products with Therapeutic Claim
        "X" -> Color(0xFF8E24AA) // Purple - Non-scheduled Poisons
        "T" -> Color(0xFF43A047) // Green - Natural Products
        "N" -> Color(0xFFFFA000) // Orange - Health Supplements
        "H" -> Color(0xFF5D4037) // Brown - Veterinary Products
        else -> Color(0xFF78909C) // Grey - Unknown
    }
}

fun getQuantityLeftColor(quantity: Int): Color {
    return when {
        quantity <= 0 -> Color(0xFFE53935)
        quantity <= 5 -> Color(0xFFFF5722)
        quantity <= 10 -> Color(0xFFFF9800)
        quantity <= 25 -> Color(0xFFFFC107)
        else -> Color(0xFF4CAF50)
    }
}

fun getQuantityLeftEmoji(quantity: Int): String {
    return when {
        quantity <= 0 -> "📦"
        quantity <= 5 -> "⚠️"
        quantity <= 10 -> "⚡"
        quantity <= 25 -> "📊"
        else -> "✅"
    }
}


fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return "N/A"
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return format.format(date)
}

fun isExpired(timestamp: Long?): Boolean {
    if (timestamp == null) return false
    return timestamp < System.currentTimeMillis()
}

fun isExpiringSoon(timestamp: Long?): Boolean {
    if (timestamp == null) return false
    val thirtyDays = 30 * 24 * 60 * 60 * 1000L
    return timestamp < System.currentTimeMillis() + thirtyDays && !isExpired(timestamp)
}

fun parseDateToLong(dateString: String): Long {
    val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return format.parse(dateString)?.time ?: 0L
}
@Composable
fun getExpiryColor(expiredDate: Long?): Color {
    if (expiredDate == null) return MaterialTheme.colorScheme.onSurface

    val currentTime = System.currentTimeMillis()
    val thirtyDays = 30L * 24 * 60 * 60 * 1000

    return when {
        expiredDate < currentTime -> Color.Red
        expiredDate < currentTime + thirtyDays -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }
}

fun getPriorityColor(priority: Float): Color {
    return when {
        priority >= 8f -> Color.Red
        priority >= 5f -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }
}
