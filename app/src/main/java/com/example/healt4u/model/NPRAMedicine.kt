package com.example.healt4u.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "npra_medicines")
data class NPRAMedicine(
    @PrimaryKey
    val regNo: String,
    val refNo: String? = null,
    val product: String = "",
    val status: String? = null,
    val description: String? = null,
    val holder: String? = null,
    val holderOsa: String? = null,
    val manufacturer: String? = null,
    val manufacturerOsa: String? = null,
    val importer: String? = null,
    val importerOsa: String? = null,
    val dateReg: String? = null,
    val dateEnd: String? = null,
    val activeIngredient: String? = null,
    val mdcCode: String? = null,
    val genericName: String? = null,
    val barcode: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val dosageForm: String? = null,
    val strength: String? = null
)

