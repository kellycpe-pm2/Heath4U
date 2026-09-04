package com.example.healt4u.model

data class UnifiedMedicine(
    val barcode: String,
    val productName: String,
    val brandName: String? = null,
    val genericName: String? = null,
    val malNumber: String? = null,
    val registrationStatus: String? = null,
    val manufacturer: String? = null,
    val activeIngredients: String? = null,
    val dosageForm: String? = null,
    val strength: String? = null,
    val indications: String? = null,
    val warnings: String? = null,
    val adverseReactions: String? = null,
    val description: String? = null,
    val source: String,
    val hasNpraData: Boolean = false,
    val hasOpenFdaData: Boolean = false,
    val hasProductInfo: Boolean = false
)

sealed class SearchResult {
    data class Npra(val medicine: NPRAMedicine) : SearchResult()
    data class OpenFDA(val data: OpenFDADB) : SearchResult()
    data class ProductInfo(val info: ProductInfo) : SearchResult()
    data class Combined(
        val npra: NPRAMedicine? = null,
        val openfda: OpenFDADB? = null,
        val productInfo: ProductInfo? = null,
        val unified: UnifiedMedicine
    ) : SearchResult()
    data class Error(val message: String) : SearchResult()
    object NotFound : SearchResult()
}

data class UnifiedMedicineResult(
    val medicine: NPRAMedicine,

    val rawValue: String,

    val resolvedMal: String,

    val source: String,

    val isVerified: Boolean,

    val parsedName: String = "",

    val parsedDosage: Int = 0,

    val category: String = "Other",

    val categoryCode: String? = null,

    val fdaInfo: FdaMedicineInfo? = null
)

