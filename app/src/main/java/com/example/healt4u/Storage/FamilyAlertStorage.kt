package com.example.healt4u.Storage

import com.example.healt4u.model.FamilyAlert
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getFamilyAlertsForDate(date: String): List<FamilyAlert> {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("family_alerts")
                .select {
                    filter {
                        eq(column = "date", value = date)
                    }
                }
                .decodeList<FamilyAlert>()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun getFamilyAlertsByPatient(patientId: String): List<FamilyAlert> {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("family_alerts")
                .select {
                    filter {
                        eq(column = "patient_id", value = patientId)
                    }
                }
                .decodeList<FamilyAlert>()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun upsertFamilyAlertCloud(alert: FamilyAlert): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("family_alerts")
                .upsert(alert) {
                    onConflict = "id"
                }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun upsertFamilyAlertsCloud(alerts: List<FamilyAlert>): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("family_alerts")
                .upsert(alerts) {
                    onConflict = "id"
                }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun deleteFamilyAlert(alertId: String): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("family_alerts")
                .delete {
                    filter {
                        eq(column = "id", value = alertId)
                    }
                }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun getFamilyAlertsForCaregiver(caregiverUserId: Int): List<FamilyAlert> {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("family_alerts")
                .select {
                    filter {
                        eq(column = "caregiver_user_id", value = caregiverUserId)
                    }
                }
                .decodeList<FamilyAlert>()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}


