package com.example.healt4u.Storage

import com.example.healt4u.model.Medicine
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

// ✅ FIXED: Remove /rest/v1/ from URL
private const val SUPABASE_URL = "https://jotudzheiwopavprryxx.supabase.co"
private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpvdHVkemhlaXdvcGF2cHJyeXh4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY1NDU1ODgsImV4cCI6MjEwMjEyMTU4OH0.Q4R0_c94lxfUKcMTVoIOdhilsDA6YfffQt7-dNoA1zM"

// ✅ FIXED: Single client instance
val supabase = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_KEY
) {
    install(Postgrest)
    install(Auth)
}

// ✅ FIXED: JSON serializer
private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

// ✅ FIXED: Insert Single Medicine
suspend fun insertSingleMedicine(medicine: Medicine): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            val medicineMap = mapOf(
                "name_medicine" to medicine.name_medicine,
                "category" to medicine.category,
                "dosage" to medicine.dosage,
                "quantity" to medicine.quantity,
                "quantity_left" to (medicine.quantityLeft ?: medicine.quantity),
                "remark" to medicine.remark,
                "expired_date" to medicine.expiredDate,
                "after_eat" to medicine.afterEat,
                "create_date" to medicine.create_Date,
                "priority" to medicine.priority,
                "ic" to (medicine.ic ?: "1")
            )

            val result = supabase
                .from("medicines")
                .insert(json.encodeToString(medicineMap))

            true
        }
    } catch (e: Exception) {
        false
    }
}

suspend fun deleteMedicine(medicineId: Int): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("medicines")
                .delete {
                    filter {
                        eq("id", medicineId)
                    }
                }
            true
        }
    } catch (e: Exception) {
        false
    }
}

suspend fun getAllMedicines(): List<Medicine> {
    return try {
        withContext(Dispatchers.IO) {
            val result = supabase
                .from("medicines")
                .select()
            val medicines = result.decodeList<Medicine>()
            medicines
        }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun getMedicinesByIC(ic: String = "1"): List<Medicine> {
    return try {
        withContext(Dispatchers.IO) {
            val result = supabase
                .from("medicines")
                .select {
                    filter {
                        eq("ic", ic)
                    }
                }
            result.decodeList<Medicine>()
        }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun updateMedicineQuantity(id: Int, newQuantity: Int): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            val updateMap = mapOf("quantityLeft" to newQuantity)
            supabase
                .from("medicines")
                .update(json.encodeToString(updateMap)) {
                    filter {
                        eq("id", id)
                    }
                }
            true
        }
    } catch (e: Exception) {
        false
    }
}
