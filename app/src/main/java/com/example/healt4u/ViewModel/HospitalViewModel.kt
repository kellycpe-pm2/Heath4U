package com.example.healt4u.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Storage.getAllDoctors
import com.example.healt4u.Storage.getAllHospitals
import com.example.healt4u.Storage.getDoctorsByHospital
import com.example.healt4u.model.Doctor
import com.example.healt4u.model.Hospital
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HospitalViewModel : ViewModel() {

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

    private val _allDoctors = MutableStateFlow<List<Doctor>>(emptyList())
    val allDoctors: StateFlow<List<Doctor>> = _allDoctors.asStateFlow()

    fun loadHospitals() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = getAllHospitals()
                _hospitals.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load hospitals"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllHospitals(): List<Hospital> {
        return _hospitals.value
    }

    fun getHospitalById(id: Int): Hospital? {
        return _hospitals.value.find { it.id == id }
    }

    fun selectHospital(hospital: Hospital) {
        _selectedHospital.value = hospital
        loadDoctorsForHospital(hospital.id)
    }

    fun loadAllDoctors() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = getAllDoctors()
                _allDoctors.value = result
                _doctors.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load doctors"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadDoctorsForHospital(hospitalId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = getDoctorsByHospital(hospitalId)
                _doctors.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load doctors for hospital"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getDoctorById(id: Int): Doctor? {
        return _doctors.value.find { it.id == id }
    }

    fun selectDoctor(doctor: Doctor) {
        _selectedDoctor.value = doctor
    }

    fun searchDoctors(query: String) {
        val allDoctors = _allDoctors.value
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

    fun searchHospitals(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allHospitals = getAllHospitals()
                _hospitals.value = if (query.isBlank()) {
                    allHospitals
                } else {
                    allHospitals.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.address?.contains(query, ignoreCase = true) == true
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Search failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAvailableDoctors(): List<Doctor> {
        return _doctors.value.filter {
            it.verificationStatus == "verified" || it.verificationStatus == "VERIFIED"
        }
    }

    fun getDoctorsBySpecialty(specialty: String): List<Doctor> {
        return _doctors.value.filter {
            it.specialization.equals(specialty, ignoreCase = true)
        }
    }

    fun getTotalDoctors(): Int {
        return _doctors.value.size
    }

    fun getTotalHospitals(): Int {
        return _hospitals.value.size
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSelection() {
        _selectedHospital.value = null
        _selectedDoctor.value = null
        _doctors.value = emptyList()
    }

    fun refresh() {
        loadHospitals()
        loadAllDoctors()
    }
}