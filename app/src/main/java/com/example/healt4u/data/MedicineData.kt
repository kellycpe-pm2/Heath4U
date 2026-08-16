package com.example.healt4u.data

object MedicineData {
    val categories : List <Pair<Char, String>> = listOf(
        'A'.to("Controlled medicines"),
        'B'.to("Natural Products with Therapeutic Claim"),
        'X'.to("Non-scheduled Poisons"),
        'N'.to("Health Supplements"),
        'H'.to("Veterinary Products")
    )
    fun getCategoryName(code: String): String {
        return categories.find { it.first.toString() == code }?.second ?: code
    }

}
