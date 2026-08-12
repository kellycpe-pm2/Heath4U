package com.example.healt4u.model

data class Medicine(
    val id : Int,
    val name_medicine: String,
    val category : String,
    val dosage : Int,
    val quantity : Int,
    val quantityLeft : Int ?= quantity,
    val remark : String ? = null,
    val expiredDate : Long ?=System.currentTimeMillis(),
    val afterEat :Boolean ?= true,
    val create_Date :Long ?= System.currentTimeMillis(),
    val priority : Int ?= 0
)
