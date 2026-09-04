package com.example.healt4u.model

data class MedicineScanData(
    val rawValue: String,
    val malNumber: String?,
    val barcode: String?,
    val ocrText: String?
)