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

    init {
        loadMedicines()
    }

    // ============ 加载数据 ============

    fun loadMedicines() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val data = dataService.fetchAllMedicines()
                if (data.isNotEmpty()) {
                    _medicines.value = data
                } else {
                    val local = dataService.loadFromAssets()
                    if (local.isNotEmpty()) {
                        _medicines.value = local
                    } else {
                        _errorMessage.value = "无法加载药品数据"
                    }
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "加载失败: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // ============ 搜索方法 ============

    fun searchByRegNo(regNo: String) {
        viewModelScope.launch {
            _searchResult.value = null
            _errorMessage.value = null
            val result = dataService.searchByRegNo(regNo)
            if (result != null) {
                _searchResult.value = result
            } else {
                _errorMessage.value = "未找到 MAL 号码: $regNo"
            }
        }
    }

    fun searchByBarcode(barcode: String) {
        viewModelScope.launch {
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
                    _errorMessage.value = "未找到该条形码的药品"
                }
            }
        }
    }

    fun searchByProductName(query: String) {
        viewModelScope.launch {
            _searchResult.value = null
            _errorMessage.value = null
            val results = dataService.searchByProductName(query)
            if (results.isNotEmpty()) {
                _medicines.value = results
            } else {
                _errorMessage.value = "未找到相关药品: $query"
            }
        }
    }

    // 智能搜索 - 自动判断类型
    fun searchMedicine(query: String) {
        val cleanQuery = query.trim()
        when {
            cleanQuery.uppercase().startsWith("MAL") -> searchByRegNo(cleanQuery)
            cleanQuery.all { it.isDigit() } -> searchByBarcode(cleanQuery)
            else -> searchByProductName(cleanQuery)
        }
    }

    fun resetSearch() {
        _searchResult.value = null
        _errorMessage.value = null
        loadMedicines()
    }

    fun getProductCount(): Int = dataService.getProductCount()
}