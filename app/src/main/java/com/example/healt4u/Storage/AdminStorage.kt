package com.example.healt4u.Storage

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.healt4u.model.Doctor
import com.example.healt4u.model.FamilyAlert
import com.example.healt4u.model.Hospital
import com.example.healt4u.model.PatientUser
import com.example.healt4u.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AdminDashboardStatistics(
    val patients: Int = 0,
    val caregiverLinks: Int = 0,
    val doctors: Int = 0,
    val hospitals: Int = 0,
    val missedDosesToday: Int = 0,
    val unresolvedAlerts: Int = 0
)

suspend fun getAdminDashboardStatistics(): AdminDashboardStatistics {
    return try {
        withContext(Dispatchers.IO) {
            val patients = SupabaseClient.supabase.from("Patient")
                .select().decodeList<PatientUser>()
            val caregiverLinks = SupabaseClient.supabase.from("caregivers")
                .select().decodeList<com.example.healt4u.model.CaregiverLink>()
            val doctors = SupabaseClient.supabase.from("doctors")
                .select().decodeList<Doctor>()
            val hospitals = SupabaseClient.supabase.from("hospitals")
                .select().decodeList<Hospital>()
            val alerts = SupabaseClient.supabase.from("family_alerts")
                .select().decodeList<FamilyAlert>()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            AdminDashboardStatistics(
                patients = patients.size,
                caregiverLinks = caregiverLinks.size,
                doctors = doctors.size,
                hospitals = hospitals.size,
                missedDosesToday = alerts.count { it.date == today },
                unresolvedAlerts = alerts.count { it.status != "RESOLVED" }
            )
        }
    } catch (e: Exception) {
        Log.e("AdminStorage", "getAdminDashboardStatistics FAILED", e)
        AdminDashboardStatistics()
    }
}

// ===== HOSPITALS =====

suspend fun getAllHospitals(): List<Hospital> {
    return try {
        withContext(Dispatchers.IO) {
            Log.d("AdminStorage", "Fetching hospitals...")
            val result = SupabaseClient.supabase
                .from("hospitals")
                .select()
                .decodeList<Hospital>()
            Log.d("AdminStorage", "SUCCESS: Got ${result.size} hospitals")
            result
        }
    } catch (e: Exception) {
        Log.e("AdminStorage", "getAllHospitals FAILED", e)
        emptyList()
    }
}

suspend fun getHospitalById(id: Int): Hospital? {
    return try {
        withContext(Dispatchers.IO) {
            SupabaseClient.supabase
                .from("hospitals")
                .select {
                    filter{ eq("id", id) }
                }
                .decodeSingleOrNull<Hospital>()
        }
    } catch (e: Exception) {
        Log.e("AdminStorage", "getHospitalById failed: id=$id", e)
        null
    }
}

suspend fun addHospital(hospital: Hospital): Result<Unit> {
    return try {
        withContext(Dispatchers.IO) {
            Log.d("AdminStorage", "Inserting hospital: $hospital")
            SupabaseClient.supabase.from("hospitals").insert(hospital)
        }
        Log.d("AdminStorage", "Hospital inserted successfully")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("AdminStorage", "addHospital failed", e)
        Result.failure(e)
    }
}

suspend fun deleteHospital(id: Int): Result<Unit> {
    return try {
        withContext(Dispatchers.IO) {
            SupabaseClient.supabase.from("hospitals").delete {
                filter{eq("id", id)}
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("AdminStorage", "deleteHospital failed", e)
        Result.failure(e)
    }
}

// ===== DOCTORS =====

suspend fun getAllDoctors(): List<Doctor> {
    return try {
        withContext(Dispatchers.IO) {
            Log.d("AdminStorage", "Fetching doctors...")
            val result = SupabaseClient.supabase
                .from("doctors")
                .select()
                .decodeList<Doctor>()
            Log.d("AdminStorage", "SUCCESS: Got ${result.size} doctors")
            result
        }
    } catch (e: Exception) {
        Log.e("AdminStorage", "getAllDoctors FAILED", e)
        emptyList()
    }
}

suspend fun getDoctorById(id: Int): Doctor? {
    return try {
        withContext(Dispatchers.IO) {
            SupabaseClient.supabase
                .from("doctors")
                .select {
                    filter{eq("id", id)}
                }
                .decodeSingleOrNull<Doctor>()
        }
    } catch (e: Exception) {
        Log.e("AdminStorage", "getDoctorById failed: id=$id", e)
        null
    }
}

suspend fun addDoctor(doctor: Doctor): Result<Unit> {
    return try {
        withContext(Dispatchers.IO) {
            Log.d("AdminStorage", "Inserting doctor: $doctor")
            SupabaseClient.supabase.from("doctors").insert(doctor)
        }
        Log.d("AdminStorage", "Doctor inserted successfully")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("AdminStorage", "addDoctor failed", e)
        Result.failure(e)
    }
}

suspend fun updateDoctorVerification(doctorId: Int, status: String): Result<Unit> {
    return try {
        withContext(Dispatchers.IO) {
            SupabaseClient.supabase.from("doctors").update(mapOf("verification_status" to status)) {
                filter{ eq("id", doctorId) }
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("AdminStorage", "updateDoctorVerification failed", e)
        Result.failure(e)
    }
}

suspend fun deleteDoctor(id: Int): Result<Unit> {
    return try {
        withContext(Dispatchers.IO) {
            SupabaseClient.supabase.from("doctors").delete {
                filter {  eq("id", id) }
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("AdminStorage", "deleteDoctor failed", e)
        Result.failure(e)
    }
}

suspend fun linkDoctorToHospital(doctorId: Int, hospitalId: Int): Result<Unit> {
    return try {
        withContext(Dispatchers.IO) {
            SupabaseClient.supabase.from("doctors").update(mapOf("hospital_id" to hospitalId)) {
                filter{ eq("id", doctorId) }
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("AdminStorage", "linkDoctorToHospital failed", e)
        Result.failure(e)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getDoctorsByHospital(hospitalId: Int): List<Doctor> {
    return try {
        Log.d("SupabaseStorage", "Fetching doctors for hospitalId: $hospitalId")

        val result = SupabaseClient.supabase
            .from("doctors")
            .select {
                filter { eq("hospital_id", hospitalId) }
            }
            .decodeList<Doctor>()

        Log.d("SupabaseStorage", "Found ${result.size} doctors for hospitalId: $hospitalId")
        result
    } catch (e: Exception) {
        Log.e("SupabaseStorage", "getDoctorsByHospital failed: ${e.message}", e)
        emptyList()
    }
}
