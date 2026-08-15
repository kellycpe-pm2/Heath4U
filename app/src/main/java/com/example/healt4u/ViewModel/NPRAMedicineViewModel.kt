package com.example.healt4u.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Service.NPRADataService
import com.example.healt4u.model.NPRAMedicine
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NPRAMedicineViewModel @Inject constructor(private val dataService: NPRADataService) : ViewModel() {
    private var cachedMedicines: List<NPRAMedicine>? = null
    private val _medicines = MutableStateFlow<List<NPRAMedicine>>(emptyList())
    val medicines: StateFlow<List<NPRAMedicine>> = _medicines.asStateFlow()

    init {
        loadMedicines()
    }

    fun loadMedicines() {
        viewModelScope.launch {
            cachedMedicines?.let {
                _medicines.value = it
                return@launch
            }

            try {
                val fetchedData = dataService.fetchAllMedicines()
                cachedMedicines = fetchedData
                _medicines.value = fetchedData
            } catch (e: Exception) {
            }
        }
    }

    fun searchMedicine(query: String) {
        val all = cachedMedicines ?: return
        _medicines.value = all.filter {
            it.product.contains(query, ignoreCase = true) ||
                    it.genericName.contains(query, ignoreCase = true) ||
                    it.regNo.contains(query, ignoreCase = true)
        }
    }
}