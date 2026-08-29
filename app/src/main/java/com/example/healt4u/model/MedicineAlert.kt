package com.example.healt4u.model

// A single expiry or low-stock warning surfaced on the Dashboard/Schedule.
// Not persisted — recomputed each time the medicine list is loaded.
data class MedicineAlert(
    val medicineId: Int,
    val medicineName: String,
    val kind: Kind,
    val message: String
) {
    enum class Kind { EXPIRING_SOON, LOW_STOCK }
}
