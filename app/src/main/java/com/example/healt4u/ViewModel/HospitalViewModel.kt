package com.example.healt4u.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.model.Doctor
import com.example.healt4u.model.Hospital
import com.example.healt4u.repository.HospitalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HospitalViewModel(
    private val repository: HospitalRepository = HospitalRepository()
) : ViewModel() {

    private val _hospitals = MutableStateFlow<List<Hospital>>(emptyList())
    val hospitals: StateFlow<List<Hospital>> = _hospitals.asStateFlow()

    private val _selectedHospital = MutableStateFlow<Hospital?>(null)
    val selectedHospital: StateFlow<Hospital?> = _selectedHospital.asStateFlow()

    private val _doctors = MutableStateFlow<List<Doctor>>(emptyList())
    val doctors: StateFlow<List<Doctor>> = _doctors.asStateFlow()

    private val _selectedDoctor = MutableStateFlow<Doctor?>(null)
    val selectedDoctor: StateFlow<Doctor?> = _selectedDoctor.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadHospitals() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getHospitals()
                _hospitals.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load hospitals"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchHospitals(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = if (query.isBlank()) {
                    repository.getHospitals()
                } else {
                    repository.searchHospitals(query)
                }
                _hospitals.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Search failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectHospital(hospital: Hospital) {
        _selectedHospital.value = hospital
        loadDoctorsForHospital(hospital.id)
    }

    fun loadDoctorsForHospital(hospitalId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getDoctorsByHospital(hospitalId)
                _doctors.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load doctors"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchDoctors(query: String) {
        viewModelScope.launch {
            val allDoctors = _doctors.value
            _doctors.value = if (query.isBlank()) {
                _selectedHospital.value?.let { loadDoctorsForHospital(it.id) }
                emptyList()
            } else {
                allDoctors.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.specialization.contains(query, ignoreCase = true)
                }
            }
        }
    }

    fun selectDoctor(doctor: Doctor) {
        _selectedDoctor.value = doctor
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getDoctorById(id: Int): Doctor? {
        return _doctors.value.find { it.id == id }
    }

    fun getHospitalById(id: Int): Hospital? {
        return _hospitals.value.find { it.id == id }
    }

    fun getAvailableDoctors(): List<Doctor> {
        return _doctors.value.filter { it.verificationStatus == "verified" }
    }
}