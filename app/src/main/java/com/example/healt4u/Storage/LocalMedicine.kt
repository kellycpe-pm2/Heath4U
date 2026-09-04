package com.example.healt4u.data.local

import android.content.Context
import com.example.healt4u.Session.CurrentSession
import com.example.healt4u.model.Medicine
import kotlinx.serialization.json.Json
import java.io.File

private fun fileName(): String = "medicines_${CurrentSession.patientId}.json"

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

fun load_Medicines(context: Context, patientId: Int): List<Medicine> {
    val fileName = "medicines_${patientId}.json"
    val file = File(context.filesDir, fileName)
    if (!file.exists()) return emptyList()
    return try {
        Json.decodeFromString(file.readText())
    } catch (e: Exception) {
        emptyList()
    }
}

fun insertMedicine(context: Context, patientId: Int, medicine: Medicine): Boolean {
    return try {
        val currentList = load_Medicines(context, patientId).toMutableList()
        currentList.add(medicine)
        saveMedicines(context, currentList)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun insertMedicines(context: Context, patientId: Int, medicines: List<Medicine>): Boolean {
    return try {
        val currentList = load_Medicines(context, patientId).toMutableList()
        currentList.addAll(medicines)
        saveMedicines(context, currentList)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun updateMedicine(context: Context, patientId: Int, updatedMedicine: Medicine): Boolean {
    return try {
        val currentList = load_Medicines(context, patientId).toMutableList()
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

fun deleteMedicine(context: Context, patientId: Int, medicineId: Int): Boolean {
    return try {
        val currentList = load_Medicines(context, patientId).toMutableList()
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

fun getMedicineById(context: Context, patientId: Int, medicineId: Int): Medicine? {
    return try {
        val medicines = load_Medicines(context, patientId)
        medicines.find { it.id == medicineId }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun searchMedicines(context: Context, patientId: Int, query: String): List<Medicine> {
    return try {
        val medicines = load_Medicines(context, patientId)
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

fun getMedicines_ByPatientId(context: Context, patientId: Int): List<Medicine> {
    return try {
        val medicines = load_Medicines(context, patientId)
        medicines.filter { it.patientId == patientId }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun getMedicinesByCategory(context: Context, patientId: Int, category: String): List<Medicine> {
    return try {
        val medicines = load_Medicines(context, patientId)
        medicines.filter { it.category == category }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun getExpiredMedicines(context: Context, patientId: Int): List<Medicine> {
    return try {
        val medicines = load_Medicines(context, patientId)
        val currentTime = System.currentTimeMillis()
        medicines.filter {
            it.expiredDate != null && it.expiredDate!! < currentTime
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun getLowStockMedicines(context: Context, patientId: Int, threshold: Int = 10): List<Medicine> {
    return try {
        val medicines = load_Medicines(context, patientId)
        medicines.filter {
            (it.quantityLeft ?: it.quantity) < threshold
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun getNextMedicineId(context: Context, patientId: Int): Int {
    return try {
        val medicines = load_Medicines(context, patientId)
        (medicines.maxOfOrNull { it.id } ?: 0) + 1
    } catch (e: Exception) {
        e.printStackTrace()
        1
    }
}

fun getMedicineCount(context: Context, patientId: Int): Int {
    return try {
        val medicines = load_Medicines(context, patientId)
        medicines.size
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}