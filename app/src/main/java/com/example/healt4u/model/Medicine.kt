package com.example.healt4u.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.serialization.Serializable

//A – Controlled medicines
//B - Natural Products with Therapeutic Claim
//X – Non-scheduled Poisons
//T – Natural Products (Traditional and
//Homeopathic Medicines)
//N – Health Supplements
//H - Veterinary Products

@Serializable
data class Medicine(

    val id : Int,
    val name_medicine: String,
    val category : String ,
    val dosage : Int,
    val quantity : Int,
    val quantityLeft : Int ?= quantity,
    val remark : String ? = null,
    val expiredDate : Long ?=System.currentTimeMillis(),
    val afterEat :Boolean ?= true,
    val create_Date :Long ?= System.currentTimeMillis(),
    val priority : Float ?= 0f,
    val ic : String ?= "1"
)

