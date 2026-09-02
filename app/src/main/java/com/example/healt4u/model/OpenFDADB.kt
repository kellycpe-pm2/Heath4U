package com.example.healt4u.model

import com.google.gson.annotations.SerializedName

data class OpenFdaResponse(
    @SerializedName("results")
    val results: List<OpenFdaDrug> = emptyList()
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
