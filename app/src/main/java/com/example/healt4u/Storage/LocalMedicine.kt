// com/example/healt4u/data/local/JsonMedicineStorage.kt
package com.example.healt4u.data.local

import android.content.Context
import com.example.healt4u.Session.CurrentSession
import com.example.healt4u.model.Medicine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// One file PER PATIENT (was a single shared "medicines.json" for the whole
// device before — meaning every account saw whichever patient's data was
// cached last). CurrentSession.patientId is set at login/switch-account.
private fun fileName(): String = "medicines_${CurrentSession.patientId}.json"

// Create Json instance with proper configuration
private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun saveMedicines(context: Context, medicines: List<Medicine>) {
    try {
        val jsonString = json.encodeToString(medicines)
        context.openFileOutput(fileName(), Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun load_Medicines(context: Context): List<Medicine> {
    val file = File(context.filesDir, fileName())
    if (!file.exists()) return emptyList() // first run — file not created yet
    return try {
        Json.decodeFromString(file.readText())
    } catch (e: Exception) {
        emptyList() 
    }
}

fun insertMedicine(context: Context, medicine: Medicine): Boolean {
    return try {
        val currentList = load_Medicines(context).toMutableList()
        currentList.add(medicine)
        saveMedicines(context, currentList)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun insertMedicines(context: Context, medicines: List<Medicine>): Boolean {
    return try {
        val currentList = load_Medicines(context).toMutableList()
        currentList.addAll(medicines)
        saveMedicines(context, currentList)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun updateMedicine(context: Context, updatedMedicine: Medicine): Boolean {
    return try {
        val currentList = load_Medicines(context).toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedMedicine.id }
        if (index != -1) {
            currentList[index] = updatedMedicine
            saveMedicines(context, currentList)
            true
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun deleteMedicine(context: Context, medicineId: Int): Boolean {
    return try {
        val currentList = load_Medicines(context).toMutableList()
        val removed = currentList.removeAll { it.id == medicineId }
        if (removed) {
            saveMedicines(context, currentList)
            true
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun deleteAllMedicines(context: Context): Boolean {
    return try {
        val file = File(context.filesDir, fileName())
        file.delete()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun getMedicineById(context: Context, medicineId: Int): Medicine? {
    return try {
        val medicines = load_Medicines(context)
        medicines.find { it.id == medicineId }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun searchMedicines(context: Context, query: String): List<Medicine> {
    return try {
        val medicines = load_Medicines(context)
        medicines.filter {
            it.name_medicine.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.remark?.contains(query, ignoreCase = true) == true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun getMedicinesByIc(context: Context, ic: String): List<Medicine> {
    return try {
        val medicines = load_Medicines(context)
        medicines.filter { it.ic == ic }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun getMedicinesByCategory(context: Context, category: String): List<Medicine> {
    return try {
        val medicines = load_Medicines(context)
        medicines.filter { it.category == category }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun getExpiredMedicines(context: Context): List<Medicine> {
    return try {
        val medicines = load_Medicines(context)
        val currentTime = System.currentTimeMillis()
        medicines.filter {
            it.expiredDate != null && it.expiredDate!! < currentTime
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun getLowStockMedicines(context: Context, threshold: Int = 10): List<Medicine> {
    return try {
        val medicines = load_Medicines(context)
        medicines.filter {
            (it.quantityLeft ?: it.quantity) < threshold
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun getNextMedicineId(context: Context): Int {
    return try {
        val medicines = load_Medicines(context)
        (medicines.maxOfOrNull { it.id } ?: 0) + 1
    } catch (e: Exception) {
        e.printStackTrace()
        1
    }
}

fun getMedicineCount(context: Context): Int {
    return try {
        val medicines = load_Medicines(context)
        medicines.size
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}