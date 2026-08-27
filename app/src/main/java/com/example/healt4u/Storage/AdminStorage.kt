package com.example.healt4u.Storage

import android.util.Log
import com.example.healt4u.model.Doctor
import com.example.healt4u.model.Hospital
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ===== HOSPITALS =====

suspend fun getAllHospitals(): List<Hospital> {
    return try {
        withContext(Dispatchers.IO) {
            supabase.from("hospitals").select().decodeList<Hospital>()
        }
    } catch (e: Exception) {
        Log.e("AdminStorage", "getAllHospitals failed", e)
        emptyList()
    }
}

suspend fun addHospital(hospital: Hospital): Result<Unit> {
    return try {
        withContext(Dispatchers.IO) {
            Log.d("AdminStorage", "Inserting hospital: $hospital")
            supabase.from("hospitals").insert(hospital)
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
            supabase.from("hospitals").delete { filter { eq("id", id) } }
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
            supabase.from("doctors").select().decodeList<Doctor>()
        }
    } catch (e: Exception) {
        Log.e("AdminStorage", "getAllDoctors failed", e)
        emptyList()
    }
}

suspend fun addDoctor(doctor: Doctor): Result<Unit> {
    return try {
        withContext(Dispatchers.IO) {
            Log.d("AdminStorage", "Inserting doctor: $doctor")
            supabase.from("doctors").insert(doctor)
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
            supabase.from("doctors").update(mapOf("verification_status" to status)) {
                filter { eq("id", doctorId) }
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
            supabase.from("doctors").delete { filter { eq("id", id) } }
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
            supabase.from("doctors").update(mapOf("hospital_id" to hospitalId)) {
                filter { eq("id", doctorId) }
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("AdminStorage", "linkDoctorToHospital failed", e)
        Result.failure(e)
    }
}
