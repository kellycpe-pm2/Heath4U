package com.example.healt4u.model

import com.google.gson.annotations.SerializedName

data class OpenFdaResponse(
    @SerializedName("results")
    val results: List<FdaLabel> = emptyList()
)

data class FdaLabel(

    @SerializedName("purpose")
    val purpose: List<String>?,

    @SerializedName("indications_and_usage")
    val indicationsAndUsage: List<String>?,

    @SerializedName("warnings")
    val warnings: List<String>?,

    @SerializedName("dosage_and_administration")
    val dosageAndAdministration: List<String>?,

    @SerializedName("contraindications")
    val contraindications: List<String>?,

    @SerializedName("description")
    val description: List<String>?,

    @SerializedName("active_ingredient")
    val activeIngredient: List<String>?,

    @SerializedName("openfda")
    val openFda: OpenFdaInfo?
)

data class OpenFdaInfo(

    @SerializedName("brand_name")
    val brandName: List<String>?,

    @SerializedName("generic_name")
    val genericName: List<String>?,

    @SerializedName("manufacturer_name")
    val manufacturerName: List<String>?,

    @SerializedName("substance_name")
    val substanceName: List<String>?
)


data class OpenFdaDrug(

    @SerializedName("product_ndc")
    val productNdc: String? = null,

    @SerializedName("package_ndc")
    val packageNdc: String? = null,

    @SerializedName("brand_name")
    val brandName: String? = null,

    @SerializedName("generic_name")
    val genericName: String? = null,

    @SerializedName("manufacturer_name")
    val manufacturerName: String? = null,

    @SerializedName("dosage_form")
    val dosageForm: String? = null,

    @SerializedName("route")
    val route: List<String>? = null,

    @SerializedName("active_ingredients")
    val activeIngredients: List<ActiveIngredient>? = null,

    @SerializedName("openfda")
    val openFda: OpenFdaIdentifiers? = null
)

data class ActiveIngredient(

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("strength")
    val strength: String? = null
)

data class OpenFdaIdentifiers(

    @SerializedName("brand_name")
    val brandName: List<String>? = null,

    @SerializedName("generic_name")
    val genericName: List<String>? = null,

    @SerializedName("manufacturer_name")
    val manufacturerName: List<String>? = null,

    @SerializedName("product_ndc")
    val productNdc: List<String>? = null,

    @SerializedName("package_ndc")
    val packageNdc: List<String>? = null,

    @SerializedName("upc")
    val upc: List<String>? = null,

    @SerializedName("rxcui")
    val rxcui: List<String>? = null
)

data class OpenFDADB(
    val id: String? = null,
    val brandName: String? = null,
    val genericName: String? = null,
    val manufacturerName: String? = null,
    val productNdc: String? = null,
    val dosageForm: String? = null,
    val route: List<String>? = null,
    val activeIngredients: List<Ingredient>? = null,
    val indicationsAndUsage: String? = null,
    val warnings: String? = null,
    val adverseReactions: String? = null,
    val description: String? = null,
    val applicationNumber: String? = null,
    val approvalDate: String? = null
)

data class Ingredient(
    val name: String? = null,
    val strength: String? = null
)

data class FdaMedicineInfo(
    val brandName: String? = null,
    val genericName: String? = null,
    val activeIngredient: String? = null,

    val purpose: String? = null,
    val indicationsAndUsage: String? = null,
    val warnings: String? = null,
    val dosageAndAdministration: String? = null,
    val contraindications: String? = null,

    val description: String? = null,

    val manufacturer: String? = null,

    val source: String = "openFDA"
)
