package com.example.healt4u.ViewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Storage.delete_Medicine
import com.example.healt4u.Storage.getMedicinesByPatientId
import com.example.healt4u.Session.CurrentSession
import com.example.healt4u.Storage.insertSingleMedicine
import com.example.healt4u.Storage.update_Medicine
import com.example.healt4u.data.MedicineData
import com.example.healt4u.data.local.*
import com.example.healt4u.model.Medicine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewModelMedicine(
    private val application: Application
) : AndroidViewModel(application) {

    // ========== STATE FLOWS ==========
    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines

    private val _stockUpdateResult = MutableStateFlow<Boolean?>(null)
    val stockUpdateResult: StateFlow<Boolean?> = _stockUpdateResult

    private val _validationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val validationErrors: StateFlow<Map<String, String>> = _validationErrors

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<Boolean?>(null)
    val success: StateFlow<Boolean?> = _success

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    // ========== FORM FIELDS ==========
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

    private val _input_reminderTime = MutableStateFlow("08:00")
    val input_reminderTime: StateFlow<String> = _input_reminderTime

    private val _input_timesPerDay = MutableStateFlow(1)
    val input_timesPerDay: StateFlow<Int> = _input_timesPerDay

    // ========== LOAD FUNCTIONS ==========
    fun loadMedicines(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                val patientId = CurrentSession.patientId ?: 0
                val localMedicines = load_Medicines(context, patientId)
                if (localMedicines.isNotEmpty()) {
                    _medicines.value = localMedicines
                }
                syncWithServer(context)
            } catch (e: Exception) {
                _error.value = "Failed to load medicines: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFromLocal(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                val patientId = CurrentSession.patientId
                if (patientId == null || patientId == 0) {
                    _error.value = "No patient logged in"
                    _medicines.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                val localMedicines = getMedicines_ByPatientId(context, patientId)

                val validMedicines = localMedicines.filter { it.patientId == patientId }

                _medicines.value = validMedicines
                if (validMedicines.size != localMedicines.size) {
                    cleanUpOrphanedMedicines(context)
                }

            } catch (e: Exception) {
                _error.value = "Failed to load local data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cleanUpOrphanedMedicines(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val patientId = CurrentSession.patientId ?: 0
                val allMedicines = load_Medicines(context, patientId)

                val orphaned = allMedicines.filter {
                    it.patientId != null && it.patientId != patientId
                }

                if (orphaned.isNotEmpty()) {
                    val cleanedList = allMedicines.filter {
                        it.patientId == null || it.patientId == patientId
                    }
                    saveMedicines(context, cleanedList)

                    _successMessage.value = "Cleaned up ${orphaned.size} medicines from other accounts"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ========== DUPLICATE CHECK FUNCTIONS ==========
    fun isMedicineNameDuplicate(name: String, excludeId: Int? = null): Boolean {
        val currentMedicines = _medicines.value
        val patientId = CurrentSession.patientId

        return currentMedicines.any { medicine ->
            medicine.patientId == patientId &&
                    medicine.name_medicine.equals(name.trim(), ignoreCase = true) &&
                    (excludeId == null || medicine.id != excludeId)
        }
    }

    fun getDuplicateNameError(name: String, excludeId: Int? = null): String? {
        return if (isMedicineNameDuplicate(name, excludeId)) {
            "Medicine '$name' already exists. Please use a different name."
        } else {
            null
        }
    }

    // ========== LOCAL CRUD ==========
    fun addMedicineLocal(medicine: Medicine, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                if (isMedicineNameDuplicate(medicine.name_medicine)) {
                    _error.value = "Medicine '${medicine.name_medicine}' already exists!"
                    _success.value = false
                    _isLoading.value = false
                    return@launch
                }

                val patientId = CurrentSession.patientId ?: 0
                val success = insertMedicine(context, patientId, medicine)
                if (success) {
                    _medicines.update { current -> current + medicine }
                    _success.value = true
                    _successMessage.value = "Medicine added locally!"
                } else {
                    _error.value = "Failed to add medicine locally"
                    _success.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _success.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMedicineLocal(medicine: Medicine, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                val patientId = CurrentSession.patientId ?: 0
                val success = deleteMedicine(context, patientId, medicine.id)
                if (success) {
                    _medicines.update { current ->
                        current.filter { it.id != medicine.id }
                    }
                    _success.value = true
                    _successMessage.value = "Medicine deleted locally!"
                } else {
                    _error.value = "Failed to delete medicine"
                    _success.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _success.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMedicineLocal(medicine: Medicine, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                if (isMedicineNameDuplicate(medicine.name_medicine, medicine.id)) {
                    _error.value = "Medicine '${medicine.name_medicine}' already exists!"
                    _success.value = false
                    _isLoading.value = false
                    return@launch
                }

                val patientId = CurrentSession.patientId ?: 0
                val success = updateMedicine(context, patientId, medicine)
                if (success) {
                    _medicines.update { current ->
                        current.map { if (it.id == medicine.id) medicine else it }
                    }
                    _success.value = true
                    _successMessage.value = "Medicine updated locally!"
                } else {
                    _error.value = "Failed to update medicine"
                    _success.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _success.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== CLOUD CRUD ==========
    fun addMedicineCloud(medicine: Medicine) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                val success = insertSingleMedicine(medicine)
                if (success) {
                    _medicines.update { current -> current + medicine }
                    _success.value = true
                    _successMessage.value = "Medicine synced to cloud!"
                } else {
                    _error.value = "Failed to sync to cloud"
                    _success.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _success.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMedicineCloud(medicine: Medicine) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                val success = delete_Medicine(medicine.id)
                if (success) {
                    _medicines.update { current ->
                        current.filter { it.id != medicine.id }
                    }
                    _success.value = true
                    _successMessage.value = "Deleted from cloud!"
                } else {
                    _error.value = "Failed to delete from cloud"
                    _success.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _success.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMedicineCloud(medicine: Medicine) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                val success = update_Medicine(medicine)
                if (success) {
                    _medicines.update { current ->
                        current.map { if (it.id == medicine.id) medicine else it }
                    }
                    _success.value = true
                    _successMessage.value = "Updated in cloud!"
                } else {
                    _error.value = "Failed to update in cloud"
                    _success.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _success.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== BOTH LOCAL + CLOUD ==========
    fun addMedicineBoth(medicine: Medicine, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                val patientId = CurrentSession.patientId ?: 0

                val medicineWithPatientId = if (medicine.patientId == null || medicine.patientId == 0) {
                    medicine.copy(patientId = patientId)
                } else {
                    medicine
                }

                if (isMedicineNameDuplicate(medicineWithPatientId.name_medicine)) {
                    _error.value = "Medicine '${medicineWithPatientId.name_medicine}' already exists!"
                    _success.value = false
                    _isLoading.value = false
                    return@launch
                }

                val localSuccess = insertMedicine(context, patientId, medicineWithPatientId)
                val cloudSuccess = insertSingleMedicine(medicineWithPatientId)

                if (localSuccess && cloudSuccess) {
                    _medicines.update { current ->
                        val filtered = current.filter { it.id != medicineWithPatientId.id }
                        filtered + medicineWithPatientId
                    }
                    _success.value = true
                    _successMessage.value = "Medicine saved locally and synced to cloud!"
                } else if (localSuccess) {
                    _medicines.update { current ->
                        val filtered = current.filter { it.id != medicineWithPatientId.id }
                        filtered + medicineWithPatientId
                    }
                    _success.value = true
                    _successMessage.value = "Saved locally (cloud sync failed)"
                    _error.value = "Cloud sync failed, but local save succeeded"
                } else {
                    _error.value = "Failed to save medicine"
                    _success.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _success.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMedicineBoth(medicine: Medicine, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                val patientId = CurrentSession.patientId ?: 0
                val localSuccess = deleteMedicine(context, patientId, medicine.id)
                val cloudSuccess = delete_Medicine(medicine.id)

                if (localSuccess || cloudSuccess) {
                    _medicines.update { current ->
                        current.filter { it.id != medicine.id }
                    }
                    _success.value = true
                    _successMessage.value = "Medicine deleted!"
                } else {
                    _error.value = "Failed to delete medicine"
                    _success.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _success.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMedicineBoth(medicine: Medicine, context: Context, onSuccess: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                if (isMedicineNameDuplicate(medicine.name_medicine, medicine.id)) {
                    _error.value = "Medicine '${medicine.name_medicine}' already exists!"
                    _success.value = false
                    _isLoading.value = false
                    return@launch
                }

                val patientId = CurrentSession.patientId ?: 0
                val localSuccess = updateMedicine(context, patientId, medicine)
                val cloudSuccess = update_Medicine(medicine)

                if (localSuccess || cloudSuccess) {
                    _medicines.update { current ->
                        current.map { if (it.id == medicine.id) medicine else it }
                    }
                    _success.value = true
                    _successMessage.value = "Medicine updated!"
                    onSuccess()
                } else {
                    _error.value = "Failed to update medicine"
                    _success.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _success.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== UPDATED VALIDATION ==========
    fun validateMedicine(
        name: String,
        category: String,
        dosage: Int,
        quantity: Int,
        expiredDate: Long?,
        excludeId: Int? = null,
        context: Context? = null
    ): Boolean {
        val errors = mutableMapOf<String, String>()

        when {
            name.isBlank() -> errors["name"] = "Medicine name is required"
            name.length < 2 -> errors["name"] = "Name must be at least 2 characters"
            name.length > 100 -> errors["name"] = "Name must be less than 100 characters"
            else -> {
                val duplicateError = getDuplicateNameError(name, excludeId)
                if (duplicateError != null) {
                    errors["name"] = duplicateError
                }
            }
        }

        when {
            category.isBlank() -> errors["category"] = "Category is required"
            MedicineData.categories.none { it.second == category } -> errors["category"] = "Please select a valid category"
        }

        when {
            dosage <= 0 -> errors["dosage"] = "Dosage must be greater than 0"
            dosage > 10000 -> errors["dosage"] = "Dosage must be less than 10000"
        }

        when {
            quantity <= 0 -> errors["quantity"] = "Quantity must be greater than 0"
            quantity > 1000 -> errors["quantity"] = "Quantity must be less than 1000"
        }

        when {
            expiredDate == null -> errors["expiredDate"] = "Expired date is required"
            expiredDate < System.currentTimeMillis() -> errors["expiredDate"] = "Medicine has already expired"
            expiredDate < System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000 -> errors["expiredDate"] = "Medicine will expire within 7 days"
        }

        _validationErrors.value = errors
        return errors.isEmpty()
    }

    fun clearValidationErrors() {
        _validationErrors.value = emptyMap()
    }

    fun clearFieldError(field: String) {
        val current = _validationErrors.value.toMutableMap()
        current.remove(field)
        _validationErrors.value = current
    }

    // ========== UPDATE WITH VALIDATION ==========
    fun updateMedicineWithValidation(medicine: Medicine, context: Context) {
        val isValid = validateMedicine(
            name = medicine.name_medicine,
            category = medicine.category,
            dosage = medicine.dosage,
            quantity = medicine.quantity,
            expiredDate = medicine.expiredDate,
            excludeId = medicine.id,
            context = context
        )

        if (!isValid) {
            _error.value = "Please fix all validation errors"
            _success.value = false
            return
        }

        updateMedicineBoth(medicine, context)
    }

    // ========== ADD WITH VALIDATION ==========
    fun addMedicineWithValidation(context: Context) {
        val name = _input_med_name.value.trim()
        val category = _input_category.value.trim()
        val dosage = _input_dosage.value
        val quantity = _input_quantity.value
        val expiredDate = _input_ExpiredDate.value
        val priority = _input_priority.value
        val remark = _input_remark.value
        val afterEat = _input_afterEat.value

        val isValid = validateMedicine(
            name = name,
            category = category,
            dosage = dosage,
            quantity = quantity,
            expiredDate = expiredDate,
            excludeId = null,
            context = context
        )

        if (!isValid) {
            _error.value = "Please fix all validation errors"
            _success.value = false
            return
        }

        addMedicineFormLocal(context)
    }

    // ========== ADD MEDICINE FORM LOCAL ==========
    fun addMedicineFormLocal(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                val name = _input_med_name.value.trim()
                val category = _input_category.value.trim()
                val dosage = _input_dosage.value
                val quantity = _input_quantity.value
                val expiredDate = _input_ExpiredDate.value
                val priority = _input_priority.value
                val remark = _input_remark.value
                val afterEat = _input_afterEat.value

                if (name.isEmpty()) {
                    _error.value = "Medicine name is required"
                    _success.value = false
                    _isLoading.value = false
                    return@launch
                }

                if (isMedicineNameDuplicate(name)) {
                    _error.value = "Medicine '$name' already exists!"
                    _success.value = false
                    _isLoading.value = false
                    return@launch
                }

                if (dosage <= 0) {
                    _error.value = "Dosage must be greater than 0"
                    _success.value = false
                    _isLoading.value = false
                    return@launch
                }

                if (quantity <= 0) {
                    _error.value = "Quantity must be greater than 0"
                    _success.value = false
                    _isLoading.value = false
                    return@launch
                }

                val patientId = CurrentSession.patientId ?: 0
                val nextId = getNextMedicineId(context, patientId)
                val medicine = Medicine(
                    id = nextId,
                    name_medicine = name,
                    category = category.ifEmpty { "General" },
                    dosage = dosage,
                    quantity = quantity,
                    quantityLeft = quantity,
                    remark = remark,
                    expiredDate = expiredDate,
                    afterEat = afterEat,
                    createDate = System.currentTimeMillis(),
                    priority = priority,
                    reminderTime = _input_reminderTime.value,
                    timesPerDay = _input_timesPerDay.value,
                    patientId = CurrentSession.patientId
                )

                val success = insertMedicine(context, patientId, medicine)
                if (success) {
                    _medicines.update { current -> current + medicine }
                    _success.value = true
                    _successMessage.value = "Medicine added successfully!"
                    clearForm()
                } else {
                    _error.value = "Failed to save medicine"
                    _success.value = false
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add medicine"
                _success.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== ADD MEDICINE FORM ==========
    fun addMedicineForm(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val name = _input_med_name.value.trim()
            val category = _input_category.value.trim()
            val dosage = _input_dosage.value
            val quantity = _input_quantity.value
            val priority = _input_priority.value

            if (name.isEmpty() || dosage <= 0 || quantity <= 0 || priority < 0f || priority > 5f) {
                _error.value = "Please fill in all fields correctly"
                _success.value = false
                _isLoading.value = false
                return@launch
            }

            if (isMedicineNameDuplicate(name)) {
                _error.value = "Medicine '$name' already exists!"
                _success.value = false
                _isLoading.value = false
                return@launch
            }

            val patientId = CurrentSession.patientId ?: 0
            val nextId = getNextMedicineId(context, patientId)
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
                reminderTime = _input_reminderTime.value,
                timesPerDay = _input_timesPerDay.value,
                patientId = CurrentSession.patientId
            )

            addMedicineBoth(medicine, context)
        }
    }

    // ========== CLEAR FORM ==========
    private fun clearForm() {
        _input_med_name.value = ""
        _input_category.value = ""
        _input_dosage.value = 0
        _input_quantity.value = 0
        _input_remark.value = ""
        _input_ExpiredDate.value = System.currentTimeMillis()
        _input_afterEat.value = true
        _input_priority.value = 0f
        _input_reminderTime.value = "08:00"
        _input_timesPerDay.value = 1
        _validationErrors.value = emptyMap()
    }

    // ========== FORM FUNCTIONS ==========
    fun on_Med_Name_Change(value: String) {
        _input_med_name.value = value
        clearFieldError("name")
    }

    fun on_Category_Change(value: String) {
        _input_category.value = value
        clearFieldError("category")
    }

    fun on_Dos_Change(value: Int) {
        _input_dosage.value = value
        clearFieldError("dosage")
    }

    fun on_Quantity_Change(value: Int) {
        _input_quantity.value = value
        clearFieldError("quantity")
    }

    fun on_Remark_Change(value: String) { _input_remark.value = value }
    fun on_ExpiredDate_Change(value: Long) {
        _input_ExpiredDate.value = value
        clearFieldError("expiredDate")
    }

    fun on_AfterEat_Change(value: Boolean) { _input_afterEat.value = value }
    fun on_Priority_Change(value: Float) { _input_priority.value = value }
    fun on_ReminderTime_Change(value: String) { _input_reminderTime.value = value }
    fun on_TimesPerDay_Change(value: Int) { _input_timesPerDay.value = value }

    // ========== SYNC FUNCTIONS ==========
    fun syncWithServer(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val patientId = CurrentSession.patientId ?: 0
                val serverMedicines = getMedicinesByPatientId(CurrentSession.patientId)
                if (serverMedicines.isNotEmpty()) {
                    saveMedicines(context, serverMedicines)
                    _medicines.value = serverMedicines
                    _successMessage.value = "Synced with server!"
                }
            } catch (e: Exception) {
            }
        }
    }

    fun uploadToServer(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val patientId = CurrentSession.patientId ?: 0

                val localMedicines = load_Medicines(context, patientId)
                    .filter { it.patientId == patientId || it.patientId == null }
                    .map {
                        if (it.patientId == null) {
                            it.copy(patientId = patientId)
                        } else {
                            it
                        }
                    }

                if (localMedicines.isEmpty()) {
                    _error.value = "No medicines to upload for this patient"
                    _isLoading.value = false
                    return@launch
                }

                var successCount = 0
                var failedCount = 0

                for (medicine in localMedicines) {
                    if (insertSingleMedicine(medicine)) {
                        successCount++
                    } else {
                        failedCount++
                    }
                }

                updateExistingInCloud(context, patientId)

                _successMessage.value = "Uploaded $successCount medicines to cloud! (Failed: $failedCount)"
            } catch (e: Exception) {
                _error.value = "Upload failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateExistingInCloud(context: Context, patientId: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                val targetPatientId = patientId ?: CurrentSession.patientId ?: return@launch

                val localMedicines = load_Medicines(context, targetPatientId)
                    .filter { it.patientId == targetPatientId }

                if (localMedicines.isEmpty()) {
                    _error.value = "No local medicines for patient $targetPatientId to update"
                    _isLoading.value = false
                    return@launch
                }

                val cloudMedicines = getMedicinesByPatientId(targetPatientId)
                if (cloudMedicines.isEmpty()) {
                    _error.value = "No medicines in cloud for patient $targetPatientId to update"
                    _isLoading.value = false
                    return@launch
                }

                var updatedCount = 0
                var skippedCount = 0
                var notFoundCount = 0

                for (localMedicine in localMedicines) {
                    val cloudMedicine = cloudMedicines.find { it.id == localMedicine.id }

                    if (cloudMedicine != null) {
                        if (localMedicine != cloudMedicine) {
                            val medicineToUpdate = localMedicine.copy(patientId = targetPatientId)
                            if (update_Medicine(medicineToUpdate)) {
                                updatedCount++
                            }
                        } else {
                            skippedCount++
                        }
                    } else {
                        notFoundCount++
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

    // ========== STOCK UPDATE ==========
    fun updateStock(
        medicine: Medicine,
        newQuantityLeft: Int,
        context: Context,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            _stockUpdateResult.value = null

            try {
                if (newQuantityLeft < 0) {
                    _error.value = "Quantity cannot be negative"
                    _stockUpdateResult.value = false
                    _isLoading.value = false
                    return@launch
                }

                if (newQuantityLeft > medicine.quantity) {
                    _error.value = "Cannot exceed total quantity (${medicine.quantity})"
                    _stockUpdateResult.value = false
                    _isLoading.value = false
                    return@launch
                }

                val updatedMedicine = medicine.copy(
                    quantityLeft = newQuantityLeft
                )

                val patientId = CurrentSession.patientId ?: 0
                val localSuccess = updateMedicine(context, patientId, updatedMedicine)

                val cloudSuccess = update_Medicine(updatedMedicine)

                if (localSuccess || cloudSuccess) {
                    _medicines.update { current ->
                        current.map {
                            if (it.id == updatedMedicine.id) updatedMedicine else it
                        }
                    }

                    _success.value = true
                    _stockUpdateResult.value = true
                    _successMessage.value = "Stock updated to $newQuantityLeft!"

                    onSuccess?.invoke()
                } else {
                    _error.value = "Failed to update stock"
                    _stockUpdateResult.value = false
                }

            } catch (e: Exception) {
                _error.value = "Error updating stock: ${e.message}"
                _stockUpdateResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== QUERY FUNCTIONS ==========
    fun searchMedicines(query: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val patientId = CurrentSession.patientId ?: 0
                val results = searchMedicines(context, patientId, query)
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
                val patientId = CurrentSession.patientId ?: 0
                val results = getExpiredMedicines(context, patientId)
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
                val patientId = CurrentSession.patientId ?: 0
                val results = getLowStockMedicines(context, patientId, threshold)
                _medicines.value = results
            } catch (e: Exception) {
                _error.value = "Failed to get low stock: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMedicinesByCategory(category: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val patientId = CurrentSession.patientId ?: 0
                val results = getMedicinesByCategory(context, patientId, category)
                _medicines.value = results
            } catch (e: Exception) {
                _error.value = "Filter failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMedicinesByPatientID(patientId: Int, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val results = getMedicines_ByPatientId(context, patientId)
                _medicines.value = results
            } catch (e: Exception) {
                _error.value = "Failed to get by IC: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun get_MedicineById(medicineId: Int, context: Context): Medicine? {
        val patientId = CurrentSession.patientId ?: 0
        val medicines = load_Medicines(context, patientId)
        return medicines.find { it.id == medicineId }
    }

    // ========== CLEAR FUNCTIONS ==========
    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    fun clearSuccessState() {
        _success.value = null
    }

    fun clearStockUpdateResult() {
        _stockUpdateResult.value = null
    }

    fun resetToLocal(context: Context) {
        loadFromLocal(context)
    }

    fun resetToCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val serverMedicines = getMedicinesByPatientId(CurrentSession.patientId)
                _medicines.value = serverMedicines
                _successMessage.value = "Loaded from cloud!"
            } catch (e: Exception) {
                _error.value = "Failed to load from cloud: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ================================================================
// ADD MEDICINE FROM SCAN
// ================================================================

    fun setScannedMedicine(
        name: String,
        category: String,
        dosage: Int,
        remark: String?,
        expiredDate: Long? = null
    ) {
        _input_med_name.value = name
        _input_category.value = category
        _input_dosage.value = dosage
        _input_quantity.value = 1
        if (remark != null) {
            _input_remark.value = remark
        }

        // Use scanned expiry if you have one,
        // otherwise keep your normal default.
        _input_ExpiredDate.value =
            expiredDate ?: (
                    System.currentTimeMillis() +
                            365L * 24L * 60L * 60L * 1000L
                    )

        // Defaults for the fields that aren't from scanning
        _input_afterEat.value = true
        _input_priority.value = 0f
        _input_reminderTime.value = "08:00"
        _input_timesPerDay.value = 1

        // Clear old validation/error state
        clearError()
        clearValidationErrors()
    }


}