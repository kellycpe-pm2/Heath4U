package com.example.healt4u.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Service.NPRADataService
import com.example.healt4u.model.NPRAMedicine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NPRAMedicineViewModel @Inject constructor(
    private val dataService: NPRADataService
) : ViewModel() {

    private val _medicines = MutableStateFlow<List<NPRAMedicine>>(emptyList())
    val medicines: StateFlow<List<NPRAMedicine>> = _medicines.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchResult = MutableStateFlow<NPRAMedicine?>(null)
    val searchResult: StateFlow<NPRAMedicine?> = _searchResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    init {
        loadMedicines()
    }

    fun loadMedicines() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val data = dataService.fetchAllMedicines()
                if (data.isNotEmpty()) {
                    _medicines.value = data
                    _isDataLoaded.value = true
                } else {
                    val localData = dataService.loadFromAssets()
                    if (localData.isNotEmpty()) {
                        _medicines.value = localData
                        _isDataLoaded.value = true
                    } else {
                        _errorMessage.value = "Unable to load the medicine data"
                    }
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Fail in loading data :  ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun searchByRegNo(regNo: String) {
        _searchResult.value = null
        _errorMessage.value = null

        val result = dataService.searchByRegNo(regNo)
        if (result != null) {
            _searchResult.value = result
        } else {
            _errorMessage.value = "No find the MAL NO: $regNo"
        }
    }

    fun searchByBarcode(barcode: String) {
        _searchResult.value = null
        _errorMessage.value = null

        val result = dataService.searchByBarcode(barcode)
        if (result != null) {
            _searchResult.value = result
        } else {
            val results = dataService.searchByRegNoContains(barcode)
            if (results.isNotEmpty()) {
                _medicines.value = results
                if (results.size == 1) {
                    _searchResult.value = results.first()
                }
            } else {
                _errorMessage.value = "No Found This QR Barcode"
            }
        }
    }

    fun searchByProductName(query: String) {
        _searchResult.value = null
        _errorMessage.value = null

        val results = dataService.searchByProductName(query)
        if (results.isNotEmpty()) {
            _medicines.value = results
        } else {
            _errorMessage.value = "Not find the related medicine: $query"
        }
    }

    fun searchMedicine(query: String) {
        val cleanQuery = query.trim()

        if (cleanQuery.uppercase().startsWith("MAL")) {
            searchByRegNo(cleanQuery)
        } else if (cleanQuery.all { it.isDigit() }) {
            searchByBarcode(cleanQuery)
        } else {
            searchByProductName(cleanQuery)
        }
    }

    fun resetSearch() {
        _searchResult.value = null
        _errorMessage.value = null
        if (_isDataLoaded.value) {
            loadMedicines()
        }
    }

    fun getProductCount(): Int {
        return dataService.getProductCount()
    }
}