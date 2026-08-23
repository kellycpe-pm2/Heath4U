// com/example/healt4u/ViewModel/ViewModelMedicine.kt
package com.example.healt4u.ViewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Storage.delete_Medicine
import com.example.healt4u.Storage.getAllMedicines
import com.example.healt4u.Storage.insertSingleMedicine
import com.example.healt4u.Storage.update_Medicine
import com.example.healt4u.data.local.*
import com.example.healt4u.model.Medicine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModelMedicine(
    private val application: Application
) : AndroidViewModel(application) {


    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage


    private val _input_med_name = MutableStateFlow("")
    val input_med_name: StateFlow<String> = _input_med_name

    private val _input_category = MutableStateFlow("")
    val input_category: StateFlow<String> = _input_category

    private val _input_dosage = MutableStateFlow(0)
    val input_dosage: StateFlow<Int> = _input_dosage

    private val _input_quantity = MutableStateFlow(0)
    val input_quantity: StateFlow<Int> = _input_quantity

    private val _input_remark = MutableStateFlow("")
    val input_remark: StateFlow<String> = _input_remark

    private val _input_ExpiredDate = MutableStateFlow(System.currentTimeMillis())
    val input_ExpiredDate: StateFlow<Long> = _input_ExpiredDate

    private val _input_afterEat = MutableStateFlow(true)
    val input_afterEat: StateFlow<Boolean> = _input_afterEat

    private val _input_priority = MutableStateFlow(0f)
    val input_priority: StateFlow<Float> = _input_priority



    // Load from local JSON first, then sync with server
    fun loadMedicines(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                // 1. Load from local JSON
                val localMedicines = load_Medicines(context)
                if (localMedicines.isNotEmpty()) {
                    _medicines.value = localMedicines
                }

                // 2. Sync with server (optional)
                syncWithServer(context)
            } catch (e: Exception) {
                _error.value = "Failed to load medicines: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Load only from local JSON (offline mode)
    fun loadFromLocal(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                val localMedicines = load_Medicines(context)
                _medicines.value = localMedicines
            } catch (e: Exception) {
                _error.value = "Failed to load local data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== Local CRUD Operations ==========

    // Add to local JSON only
    fun addMedicineLocal(medicine: Medicine, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                val success = insertMedicine(context, medicine)
                if (success) {
                    _medicines.update { current -> current + medicine }
                    _successMessage.value = "Medicine added locally!"
                } else {
                    _error.value = "Failed to add medicine locally"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete from local JSON only
    fun deleteMedicineLocal(medicine: Medicine, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                val success = deleteMedicine(context, medicine.id)
                if (success) {
                    _medicines.update { current ->
                        current.filter { it.id != medicine.id }
                    }
                    _successMessage.value = "Medicine deleted locally!"
                } else {
                    _error.value = "Failed to delete medicine"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update in local JSON only
    fun updateMedicineLocal(medicine: Medicine, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                val success = updateMedicine(context, medicine)
                if (success) {
                    _medicines.update { current ->
                        current.map { if (it.id == medicine.id) medicine else it }
                    }
                    _successMessage.value = "Medicine updated locally!"
                } else {
                    _error.value = "Failed to update medicine"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add to cloud only (Supabase)
    fun addMedicineCloud(medicine: Medicine) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                val success = insertSingleMedicine(medicine)
                if (success) {
                    _medicines.update { current -> current + medicine }
                    _successMessage.value = "Medicine synced to cloud!"
                } else {
                    _error.value = "Failed to sync to cloud"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete from cloud only (Supabase)
    fun deleteMedicineCloud(medicine: Medicine) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                val success = delete_Medicine(medicine.id)
                if (success) {
                    _medicines.update { current ->
                        current.filter { it.id != medicine.id }
                    }
                    _successMessage.value = "Deleted from cloud!"
                } else {
                    _error.value = "Failed to delete from cloud"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update in cloud only (Supabase)
    fun updateMedicineCloud(medicine: Medicine) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                val success = update_Medicine(medicine)
                if (success) {
                    _medicines.update { current ->
                        current.map { if (it.id == medicine.id) medicine else it }
                    }
                    _successMessage.value = "Updated in cloud!"
                } else {
                    _error.value = "Failed to update in cloud"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // Add to both local and cloud
    fun addMedicineBoth(medicine: Medicine,context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                // 1. Save to local first
                val localSuccess = insertMedicine(context, medicine)
                // 2. Save to cloud
                val cloudSuccess = insertSingleMedicine(medicine)

                if (localSuccess && cloudSuccess) {
                    _medicines.update { current -> current + medicine }
                    _successMessage.value = "Medicine saved locally and synced to cloud!"
                } else if (localSuccess) {
                    _medicines.update { current -> current + medicine }
                    _successMessage.value = "Saved locally (cloud sync failed)"
                    _error.value = "Cloud sync failed, but local save succeeded"
                } else {
                    _error.value = "Failed to save medicine"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete from both local and cloud
    fun deleteMedicineBoth(medicine: Medicine, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                val localSuccess = deleteMedicine(context, medicine.id)
                val cloudSuccess = delete_Medicine(medicine.id)

                if (localSuccess || cloudSuccess) {
                    _medicines.update { current ->
                        current.filter { it.id != medicine.id }
                    }
                    _successMessage.value = "Medicine deleted!"
                } else {
                    _error.value = "Failed to delete medicine"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update in both local and cloud
    fun updateMedicineBoth(medicine: Medicine, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                val localSuccess = updateMedicine(context, medicine)
                val cloudSuccess = update_Medicine(medicine)

                if (localSuccess || cloudSuccess) {
                    _medicines.update { current ->
                        current.map { if (it.id == medicine.id) medicine else it }
                    }
                    _successMessage.value = "Medicine updated!"
                } else {
                    _error.value = "Failed to update medicine"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // Sync local data with server
    fun syncWithServer(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val serverMedicines = getAllMedicines()

                if (serverMedicines.isNotEmpty()) {
                    // Replace local with server data
                    saveMedicines(context, serverMedicines)
                    _medicines.value = serverMedicines
                    _successMessage.value = "Synced with server!"
                }
            } catch (e: Exception) {

            }
        }
    }

    // Upload local data to server
    fun uploadToServer(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val localMedicines = load_Medicines(context)
                var successCount = 0

                for (medicine in localMedicines) {
                    if (insertSingleMedicine(medicine)) {
                        successCount++
                    }
                }
                updateExistingInCloud(context)

                _successMessage.value = "Uploaded $successCount/${localMedicines.size} medicines to cloud!"
            } catch (e: Exception) {
                _error.value = "Upload failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateExistingInCloud(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                // 1. Get local medicines
                val localMedicines = load_Medicines(context )
                if (localMedicines.isEmpty()) {
                    _error.value = "No local medicines"
                    _isLoading.value = false
                    return@launch
                }

                // 2. Get cloud medicines
                val cloudMedicines = getAllMedicines()
                if (cloudMedicines.isEmpty()) {
                    _error.value = "No medicines in cloud to update"
                    _isLoading.value = false
                    return@launch
                }

                var updatedCount = 0
                var skippedCount = 0
                var notFoundCount = 0

                // 3. Update only medicines that exist in BOTH places and have changes
                for (localMedicine in localMedicines) {
                    val cloudMedicine = cloudMedicines.find { it.id == localMedicine.id }

                    if (cloudMedicine != null) {
                        // Medicine exists in cloud - check if changed
                        if (localMedicine != cloudMedicine) {
                            if (update_Medicine(localMedicine)) {
                                updatedCount++
                            }
                        } else {
                            skippedCount++  // No changes needed
                        }
                    } else {
                        notFoundCount++  // In local but not in cloud (need upload)
                    }
                }

                _successMessage.value = "Updated $updatedCount medicines (Skipped $skippedCount unchanged, $notFoundCount not in cloud)"

            } catch (e: Exception) {
                _error.value = "Update failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun on_Med_Name_Change(value: String) { _input_med_name.value = value }
    fun on_Category_Change(value: String) { _input_category.value = value }
    fun on_Dos_Change(value: Int) { _input_dosage.value = value }
    fun on_Quantity_Change(value: Int) { _input_quantity.value = value }
    fun on_Remark_Change(value: String) { _input_remark.value = value }
    fun on_ExpiredDate_Change(value: Long) { _input_ExpiredDate.value = value }
    fun on_AfterEat_Change(value: Boolean) { _input_afterEat.value = value }
    fun on_Priority_Change(value: Float) { _input_priority.value = value }

    // Add medicine from form (uses both local + cloud)
    fun addMedicineForm(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val name = _input_med_name.value.trim()
            val category = _input_category.value.trim()
            val dosage = _input_dosage.value
            val quantity = _input_quantity.value
            val priority = _input_priority.value

            // Validations
            if (name.isEmpty() || dosage <= 0 || quantity < 0 || priority < 0f || priority > 5f) {
                _error.value = "Please fill in all fields correctly"
                return@launch
            }

            val nextId = getNextMedicineId(context)
            val medicine = Medicine(
                id = nextId,
                name_medicine = name,
                category = category.ifEmpty { "General" },
                dosage = dosage,
                quantity = quantity,
                quantityLeft = quantity,
                remark = _input_remark.value,
                expiredDate = _input_ExpiredDate.value,
                afterEat = _input_afterEat.value,
                createDate = System.currentTimeMillis(),
                priority = priority,
                ic = "1"
            )

            // Use hybrid approach
            addMedicineBoth(medicine,context)
            clearForm()
        }
    }

    // Add from form - local only
    fun addMedicineFormLocal(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val name = _input_med_name.value.trim()
            val dosage = _input_dosage.value
            val quantity = _input_quantity.value

            if (name.isEmpty() || dosage <= 0 || quantity < 0) {
                _error.value = "Please fill in required fields"
                return@launch
            }

            val nextId = getNextMedicineId(context)
            val medicine = Medicine(
                id = nextId,
                name_medicine = name,
                category = _input_category.value.ifEmpty { "General" },
                dosage = dosage,
                quantity = quantity,
                quantityLeft = quantity,
                remark = _input_remark.value,
                expiredDate = _input_ExpiredDate.value,
                afterEat = _input_afterEat.value,
                createDate = System.currentTimeMillis(),
                priority = _input_priority.value,
                ic = "1"
            )

            addMedicineLocal(medicine,context)
            clearForm()
        }
    }

    private fun clearForm() {
        _input_med_name.value = ""
        _input_category.value = ""
        _input_dosage.value = 0
        _input_quantity.value = 0
        _input_remark.value = ""
        _input_ExpiredDate.value = System.currentTimeMillis()
        _input_afterEat.value = true
        _input_priority.value = 0f
    }

    // ========== Query Functions ==========

    fun searchMedicines(query: String,context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val results = searchMedicines(context, query)
                _medicines.value = results
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun get_ExpiredMedicines(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val results = getExpiredMedicines(context)
                _medicines.value = results
            } catch (e: Exception) {
                _error.value = "Failed to get expired: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getLowStockMedicines(threshold: Int = 10, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val results = getLowStockMedicines(context, threshold)
                _medicines.value = results
            } catch (e: Exception) {
                _error.value = "Failed to get low stock: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMedicinesByCategory(category: String,context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val results = getMedicinesByCategory(context, category)
                _medicines.value = results
            } catch (e: Exception) {
                _error.value = "Filter failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMedicinesByIc(ic: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val results = getMedicinesByIc(context, ic)
                _medicines.value = results
            } catch (e: Exception) {
                _error.value = "Failed to get by IC: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun get_MedicineById(medicineId: Int, context: Context): Medicine? {
        return _medicines.value.find { it.id == medicineId }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    fun resetToLocal(context: Context) {
        loadFromLocal(context)
    }

    fun resetToCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val serverMedicines = getAllMedicines()
                _medicines.value = serverMedicines
                _successMessage.value = "Loaded from cloud!"
            } catch (e: Exception) {
                _error.value = "Failed to load from cloud: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}