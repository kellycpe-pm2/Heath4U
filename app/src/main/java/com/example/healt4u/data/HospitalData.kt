package com.example.healt4u.data

import com.example.healt4u.model.Doctor
import com.example.healt4u.model.Hospital

object HospitalData {
    val hospitals = listOf(
        Hospital(
            id = 1,
            name = "Penang General Hospital",
            address = "Jalan Residensi, 10450 George Town, Pulau Pinang",
            phone = "04-2225333"
        ),
        Hospital(
            id = 2,
            name = "Hospital Bukit Mertajam",
            address = "Jalan Kulim, 14000 Bukit Mertajam, Pulau Pinang",
            phone = "04-5497333"
        ),
        Hospital(
            id = 3,
            name = "Hospital Sultan Abdul Halim",
            address = "225, Bandar Amanjaya, 08000 Sungai Petani, Kedah",
            phone = "04-4457333"
        ),
        Hospital(
            id = 4,
            name = "Hospital Taiping",
            address = "Jalan Taming Sari, 34000 Taiping, Perak",
            phone = "05-8204000"
        ),
        Hospital(
            id = 5,
            name = "Hospital Raja Permaisuri Bainun",
            address = "Jalan Raja Ashman Shah, 30450 Ipoh, Perak",
            phone = "05-2087000"
        )
    )

    val doctors = listOf(
        Doctor(
            id = 1,
            name = "Dr. Ahmad Ismail",
            ic = "700101-10-1234",
            phone = "013-2615542",
            email = "ahmad.ismail@pgh.gov.my",
            specialization = "Cardiologist",
            hospitalId = 1,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 2,
            name = "Dr. Sarah Tan",
            ic = "750202-14-5678",
            phone = "013-7418455",
            email = "sarah.tan@pgh.gov.my",
            specialization = "Neurologist",
            hospitalId = 1,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 3,
            name = "Dr. Ravi Kumar",
            ic = "800303-10-9012",
            phone = "013-2629571",
            email = "ravi.kumar@pjh.gov.my",
            specialization = "Orthopedic",
            hospitalId = 1,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 4,
            name = "Dr. Lee Mei Ling",
            ic = "810404-14-3456",
            phone = "012-6155103",
            email = "meiling.lee@pjh.gov.my",
            specialization = "Pediatrician",
            hospitalId = 1,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 5,
            name = "Dr. Norashikin",
            ic = "820505-10-7890",
            phone = "017-5116504",
            email = "norashikin@hbm.gov.my",
            specialization = "Dermatologist",
            hospitalId = 2,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 6,
            name = "Dr. Nik Mohd",
            ic = "830606-14-1234",
            phone = "016-521805",
            email = "nikmohd@hbm.edu.my",
            specialization = "Cardiologist",
            hospitalId = 2,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 7,
            name = "Dr. Aina",
            ic = "840707-10-5678",
            phone = "013-7949201",
            email = "aina@hbm.edu.my",
            specialization = "Neurologist",
            hospitalId = 2,
            qualificationDocUrl = null,
            verificationStatus = "pending"
        ),
        Doctor(
            id = 8,
            name = "Dr. Rizal",
            ic = "850808-14-9012",
            phone = "017-9494202",
            email = "rizal@hbm.edu.my",
            specialization = "Orthopedic",
            hospitalId = 2,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 9,
            name = "Dr. Maya",
            ic = "860909-10-3456",
            phone = "019-4152903",
            email = "maya@hbm.edu.my",
            specialization = "Pediatrician",
            hospitalId = 2,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 10,
            name = "Dr. Khairul",
            ic = "870101-14-7890",
            phone = "015-2809451",
            email = "khairul@hsah.edu.my",
            specialization = "Surgeon",
            hospitalId = 3,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 11,
            name = "Dr. Mages",
            ic = "880202-10-1234",
            phone = "018-6103300",
            email = "mages@hsah.moh.gov.my",
            specialization = "Nephrologist",
            hospitalId = 3,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 12,
            name = "Dr. Pua",
            ic = "890303-14-5678",
            phone = "012-0345601",
            email = "pua@hsah.moh.gov.my",
            specialization = "Gynecologist",
            hospitalId = 3,
            qualificationDocUrl = null,
            verificationStatus = "pending"
        ),
        Doctor(
            id = 13,
            name = "Dr. Farid",
            ic = "900404-10-9012",
            phone = "011-9811402",
            email = "farid@ht.moh.gov.my",
            specialization = "Cardiologist",
            hospitalId = 4,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 14,
            name = "Dr. Hafiz",
            ic = "910505-14-3456",
            phone = "014-2034500",
            email = "hafiz@ht.moh.gov.my",
            specialization = "Cardiologist",
            hospitalId = 4,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 15,
            name = "Dr. Azlina",
            ic = "920606-10-7890",
            phone = "016-3720101",
            email = "azlina@ht.moh.gov.my",
            specialization = "Oncologist",
            hospitalId = 4,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 16,
            name = "Dr. Rahim",
            ic = "930707-14-1234",
            phone = "018-9606815",
            email = "rahim@hrpb.moh.gov.my",
            specialization = "Infectious Disease",
            hospitalId = 5,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 17,
            name = "Dr. Chin",
            ic = "940808-10-5678",
            phone = "011-0548251",
            email = "chin@hrpb.moh.gov.my",
            specialization = "General Medicine",
            hospitalId = 5,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        ),
        Doctor(
            id = 18,
            name = "Dr. Iskandar",
            ic = "950909-14-9012",
            phone = "015-5280930",
            email = "iskandar@hrpb.moh.gov.my",
            specialization = "Surgeon",
            hospitalId = 5,
            qualificationDocUrl = null,
            verificationStatus = "pending"
        ),
        Doctor(
            id = 19,
            name = "Dr. Lim",
            ic = "960101-10-3456",
            phone = "014-2533871",
            email = "lim@hrpb.moh.gov.my",
            specialization = "Cardiologist",
            hospitalId = 5,
            qualificationDocUrl = null,
            verificationStatus = "verified"
        )
    )

    fun getHospitalById(id: Int): Hospital? = hospitals.find { it.id == id }

    fun searchHospitals(query: String): List<Hospital> {
        return if (query.isBlank()) {
            hospitals
        } else {
            hospitals.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true)
            }
        }
    }

    fun getDoctorsByHospital(hospitalId: Int): List<Doctor> {
        return doctors.filter { it.hospitalId == hospitalId }
    }

    fun getDoctorById(id: Int): Doctor? = doctors.find { it.id == id }

    fun searchDoctors(query: String): List<Doctor> {
        return if (query.isBlank()) {
            doctors
        } else {
            doctors.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.specialization.contains(query, ignoreCase = true)
            }
        }
    }

    fun getAvailableDoctors(): List<Doctor> {
        return doctors.filter { it.verificationStatus == "verified" }
    }

    fun getAvailableDoctorsByHospital(hospitalId: Int): List<Doctor> {
        return doctors.filter {
            it.hospitalId == hospitalId &&
                    it.verificationStatus == "verified"
        }
    }
}