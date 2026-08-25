package com.example.healt4u.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Storage.*
import com.example.healt4u.model.Doctor
import com.example.healt4u.model.Hospital
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminManagementViewModel : ViewModel() {

    private val _hospitals = MutableStateFlow<List<Hospital>>(emptyList())
    val hospitals: StateFlow<List<Hospital>> = _hospitals

    private val _doctors = MutableStateFlow<List<Doctor>>(emptyList())
    val doctors: StateFlow<List<Doctor>> = _doctors

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _hospitals.value = getAllHospitals()
            _doctors.value = getAllDoctors()
            _isLoading.value = false
        }
    }

    fun addHospital(name: String, address: String, phone: String) {
        if (name.isBlank() || address.isBlank() || phone.isBlank()) {
            _error.value = "All hospital fields are required"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val ok = addHospital(Hospital(name = name, address = address, phone = phone))
            if (ok) loadAll() else _error.value = "Failed to add hospital"
            _isLoading.value = false
        }
    }

    fun addDoctor(
        name: String, ic: String, phone: String, email: String,
        specialization: String, hospitalId: Int?
    ) {
        if (name.isBlank() || ic.isBlank() || phone.isBlank() || email.isBlank()) {
            _error.value = "Required doctor fields are missing"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val ok = addDoctor(
                Doctor(
                    name = name, ic = ic, phone = phone, email = email,
                    specialization = specialization, hospitalId = hospitalId,
                    verificationStatus = "pending"
                )
            )
            if (ok) loadAll() else _error.value = "Failed to add doctor"
            _isLoading.value = false
        }
    }

    fun approveDoctor(doctorId: Int) {
        viewModelScope.launch {
            updateDoctorVerification(doctorId, "approved")
            loadAll()
        }
    }

    fun rejectDoctor(doctorId: Int) {
        viewModelScope.launch {
            updateDoctorVerification(doctorId, "rejected")
            loadAll()
        }
    }

    fun removeDoctor(doctorId: Int) {
        viewModelScope.launch {
            deleteDoctor(doctorId)
            loadAll()
        }
    }

    fun clearError() { _error.value = null }
}