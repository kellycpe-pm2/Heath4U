package com.example.healt4u.model

data class Medicine(
    val name_medicine: String,
    val category : String,
    val dosage : String,
    val quantity : Int,
    val quantityLeft : Int,
    val remark : String ? = null,
    val expiredDate : Long,
    val afterEat :Boolean = true,
    val create_Date :Long = System.currentTimeMillis(),
)
