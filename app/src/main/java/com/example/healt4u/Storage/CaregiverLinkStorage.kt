package com.example.healt4u.Storage

import com.example.healt4u.model.CaregiverLink
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

suspend fun addCaregiverLink(link: CaregiverLink): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            val username = "cg_${link.patientUserId}_${link.caregiverUserId}"
            val insertData = buildMap {
                put("username", JsonPrimitive(username))
                put("name", JsonPrimitive(link.caregiverName))
                put("phone", JsonPrimitive(link.caregiverPhone))
                put("relationship", JsonPrimitive(link.relationship))
                put("patient_phone", JsonPrimitive(link.patientPhone))
                put("patient_username", JsonPrimitive(""))
                put("patient_user_id", JsonPrimitive(link.patientUserId))
                put("caregiver_user_id", JsonPrimitive(link.caregiverUserId))
                put("caregiver_name", JsonPrimitive(link.caregiverName))
                put("caregiver_phone", JsonPrimitive(link.caregiverPhone))
                put("patient_name", JsonPrimitive(link.patientName))
                put("status", JsonPrimitive(link.status))
            }
            android.util.Log.d("CaregiverLink", "Inserting: $insertData")
            supabase
                .from("caregivers")
                .insert(JsonObject(insertData))
        }
        true
    } catch (e: Exception) {
        android.util.Log.e("CaregiverLink", "addCaregiverLink failed", e)
        false
    }
}

suspend fun getCaregiversForPatient(patientUserId: Int): List<CaregiverLink> {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("caregivers")
                .select {
                    filter {
                        eq(column = "patient_user_id", value = patientUserId)
                    }
                }
                .decodeList<CaregiverLink>()
        }
    } catch (e: Exception) {
        android.util.Log.e("CaregiverLink", "getCaregiversForPatient failed", e)
        emptyList()
    }
}

suspend fun getPatientsForCaregiver(caregiverUserId: Int): List<CaregiverLink> {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("caregivers")
                .select {
                    filter {
                        eq(column = "caregiver_user_id", value = caregiverUserId)
                    }
                }
                .decodeList<CaregiverLink>()
        }
    } catch (e: Exception) {
        android.util.Log.e("CaregiverLink", "getPatientsForCaregiver failed", e)
        emptyList()
    }
}

suspend fun removeCaregiverLink(linkId: Int): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("caregivers")
                .delete {
                    filter {
                        eq(column = "id", value = linkId)
                    }
                }
        }
        true
    } catch (e: Exception) {
        android.util.Log.e("CaregiverLink", "removeCaregiverLink failed", e)
        false
    }
}

suspend fun updateCaregiverLinkStatus(linkId: Int, status: String): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("caregivers")
                .update(
                    mapOf("status" to status)
                ) {
                    filter {
                        eq(column = "id", value = linkId)
                    }
                }
        }
        true
    } catch (e: Exception) {
        android.util.Log.e("CaregiverLink", "updateCaregiverLinkStatus failed", e)
        false
    }
}

suspend fun getCaregiverLinkByPatientAndCaregiver(
    patientUserId: Int,
    caregiverUserId: Int
): CaregiverLink? {
    return try {
        withContext(Dispatchers.IO) {
            val links = supabase
                .from("caregivers")
                .select {
                    filter {
                        eq(column = "patient_user_id", value = patientUserId)
                        eq(column = "caregiver_user_id", value = caregiverUserId)
                    }
                }
                .decodeList<CaregiverLink>()
            links.firstOrNull()
        }
    } catch (e: Exception) {
        android.util.Log.e("CaregiverLink", "getCaregiverLinkByPatientAndCaregiver failed", e)
        null
    }
}
