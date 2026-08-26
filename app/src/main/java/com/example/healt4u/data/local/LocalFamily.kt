package com.example.healt4u.data.local

import android.content.Context
import com.example.healt4u.model.CaregiverProfile
import com.example.healt4u.model.FamilyAlert
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private const val CAREGIVERS_FILE = "caregivers.json"
private const val ALERTS_FILE = "family_alerts.json"

private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

// ---- Caregiver local storage ----

fun saveCaregivers(context: Context, caregivers: List<CaregiverProfile>) {
    try {
        val jsonString = json.encodeToString(caregivers)
        context.openFileOutput(CAREGIVERS_FILE, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    } catch (_: Exception) { }
}

fun loadCaregivers(context: Context): List<CaregiverProfile> {
    val file = File(context.filesDir, CAREGIVERS_FILE)
    if (!file.exists()) return emptyList()
    return try {
        Json.decodeFromString(file.readText())
    } catch (_: Exception) {
        emptyList()
    }
}

// ---- Family alert local storage ----

fun saveFamilyAlerts(context: Context, alerts: List<FamilyAlert>) {
    try {
        val jsonString = json.encodeToString(alerts)
        context.openFileOutput(ALERTS_FILE, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    } catch (_: Exception) { }
}

fun loadFamilyAlerts(context: Context): List<FamilyAlert> {
    val file = File(context.filesDir, ALERTS_FILE)
    if (!file.exists()) return emptyList()
    return try {
        Json.decodeFromString(file.readText())
    } catch (_: Exception) {
        emptyList()
    }
}
