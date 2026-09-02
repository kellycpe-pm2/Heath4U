package com.example.healt4u.ViewModel

import android.os.Build
import androidx.annotation.RequiresApi
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

    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success

    private var selectedHospitalId: Int? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun selectHospital(hospital: Hospital) {
        selectedHospitalId = hospital.id
        loadDoctorsForHospital(hospital.id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadDoctorsForHospital(hospitalId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val list = getDoctorsByHospital(hospitalId)
                _doctors.value = list
            } catch (e: Exception) {
                _error.value = "Failed to load doctors: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
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
            _error.value = null
            val result = com.example.healt4u.Storage.addHospital(
                Hospital(name = name, address = address, phone = phone)
            )
            result.fold(
                onSuccess = { loadAll() },
                onFailure = { e ->
                    _error.value = "Failed to add hospital: ${e.message}"
                    _isLoading.value = false
                }
            )
        }
    }

    fun addDoctor(
        name: String, ic: String, phone: String, email: String,
        specialization: String, hospitalId: Int?, consultationFee: Double = 0.0
    ) {
        if (name.isBlank() || ic.isBlank() || phone.isBlank() || email.isBlank()) {
            _error.value = "Required doctor fields are missing"
            return
        }
        if (specialization.isBlank()) {
            _error.value = "Please select a specialization"
            return
        }
        if (hospitalId == null) {
            _error.value = "Please select a hospital"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = addDoctor(
                Doctor(
                    name = name,
                    ic = ic,
                    phone = phone,
                    email = email,
                    specialization = specialization,
                    hospitalId = hospitalId,
                    verificationStatus = "approved",
                    consultationFee = consultationFee
                )
            )
            result.fold(
                onSuccess = { loadAll() },
                onFailure = { e ->
                    _error.value = "Failed to add doctor: ${e.message}"
                    _isLoading.value = false
                }
            )
        }
    }

    fun approveDoctor(doctorId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            updateDoctorVerification(doctorId, "verified")
            loadAll()
        }
    }

    fun rejectDoctor(doctorId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            updateDoctorVerification(doctorId, "rejected")
            loadAll()
        }
    }

    fun removeDoctor(doctorId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            deleteDoctor(doctorId)
            loadAll()
        }
    }

    fun removeHospital(hospitalId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            deleteHospital(hospitalId)
            loadAll()
        }
    }

    fun linkDoctorToHospital(doctorId: Int, hospitalId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = null
            val result = com.example.healt4u.Storage.linkDoctorToHospital(doctorId, hospitalId)
            result.fold(
                onSuccess = {
                    _success.value = "Doctor linked to hospital successfully"
                    loadAll()
                },
                onFailure = { e ->
                    _error.value = "Failed to link doctor: ${e.message}"
                }
            )
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
    fun clearSuccess() { _success.value = null }
}