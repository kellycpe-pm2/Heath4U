package com.example.healt4u.repository

import com.example.healt4u.data.HospitalData
import com.example.healt4u.model.Doctor
import com.example.healt4u.model.Hospital
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HospitalRepository {

    suspend fun getHospitals(): List<Hospital> {
        return HospitalData.hospitals
    }

    fun getHospitalsFlow(): Flow<List<Hospital>> = flow {
        emit(HospitalData.hospitals)
    }

    suspend fun getHospitalById(id: Int): Hospital? {
        return HospitalData.getHospitalById(id)
    }

    suspend fun searchHospitals(query: String): List<Hospital> {
        return HospitalData.searchHospitals(query)
    }

    suspend fun getDoctorsByHospital(hospitalId: Int): List<Doctor> {
        return HospitalData.getDoctorsByHospital(hospitalId)
    }

    suspend fun getDoctorById(id: Int): Doctor? {
        return HospitalData.getDoctorById(id)
    }

    suspend fun searchDoctors(query: String): List<Doctor> {
        return HospitalData.searchDoctors(query)
    }

    suspend fun getAvailableDoctors(): List<Doctor> {
        return HospitalData.getAvailableDoctors()
    }
}