package com.example.healt4u.model

data class ProductInfo(
    val barcode: String,
    val productName: String,
    val brand: String? = null,
    val category: String? = null,
    val ingredients: String? = null,
    val imageUrl: String? = null,
    val source: String = "Open Food Facts"
)
