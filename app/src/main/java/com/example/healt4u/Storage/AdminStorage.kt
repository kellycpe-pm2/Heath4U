package com.example.healt4u.Storage

import com.example.healt4u.model.Doctor
import com.example.healt4u.model.DoctorRating
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
    } catch (e: Exception) { emptyList() }
}

suspend fun addHospital(hospital: Hospital): Boolean {
    return try {
        withContext(Dispatchers.IO) { supabase.from("hospitals").insert(hospital) }
        true
    } catch (e: Exception) { false }
}

suspend fun deleteHospital(id: Int): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase.from("hospitals").delete { filter { eq("id", id) } }
        }
        true
    } catch (e: Exception) { false }
}

// ===== DOCTORS =====

suspend fun getAllDoctors(): List<Doctor> {
    return try {
        withContext(Dispatchers.IO) {
            supabase.from("doctors").select().decodeList<Doctor>()
        }
    } catch (e: Exception) { emptyList() }
}

suspend fun addDoctor(doctor: Doctor): Boolean {
    return try {
        withContext(Dispatchers.IO) { supabase.from("doctors").insert(doctor) }
        true
    } catch (e: Exception) { false }
}

suspend fun updateDoctorVerification(doctorId: Int, status: String): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase.from("doctors").update(mapOf("verification_status" to status)) {
                filter { eq("id", doctorId) }
            }
        }
        true
    } catch (e: Exception) { false }
}

suspend fun deleteDoctor(id: Int): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase.from("doctors").delete { filter { eq("id", id) } }
        }
        true
    } catch (e: Exception) { false }
}

// ===== KPI =====

suspend fun getDoctorRatings(doctorId: Int): List<DoctorRating> {
    return try {
        withContext(Dispatchers.IO) {
            supabase.from("doctor_ratings")
                .select { filter { eq("doctor_id", doctorId) } }
                .decodeList<DoctorRating>()
        }
    } catch (e: Exception) { emptyList() }
}