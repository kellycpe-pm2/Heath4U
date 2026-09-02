package com.example.healt4u.Service

import android.util.Log
import com.example.healt4u.model.Ingredient
import com.example.healt4u.model.OpenFDADB
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@Singleton
class OpenFDAService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * openFDA NDC database
     *
     * Used for:
     * - Product NDC
     * - UPC
     * - Brand name
     * - Generic name
     * - Manufacturer
     * - Dosage form
     * - Route
     */
    private val baseUrl = "https://api.fda.gov/drug/ndc.json"

    /**
     * Search medicine using NDC.
     *
     * Example:
     * 00093-1045
     * 000931045
     */
    suspend fun searchByNdc(
        ndc: String
    ): OpenFDADB? {

        return withContext(Dispatchers.IO) {

            try {

                val cleanNdc = ndc
                    .trim()
                    .replace(Regex("[^0-9-]"), "")

                if (cleanNdc.isBlank()) {
                    return@withContext null
                }

                Log.d(
                    "OpenFDA",
                    "Searching NDC = $cleanNdc"
                )

                val url =
                    "$baseUrl?search=product_ndc:$cleanNdc&limit=1"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->

                    val json = response.body?.string()

                    if (!response.isSuccessful || json.isNullOrBlank()) {

                        Log.d(
                            "OpenFDA",
                            "NDC not found: HTTP ${response.code}"
                        )

                        return@withContext null
                    }

                    val result =
                        gson.fromJson(
                            json,
                            OpenFDAResponse::class.java
                        )

                    val medicine =
                        result.results
                            ?.firstOrNull()
                            ?.toOpenFDAMedicine()

                    if (medicine != null) {
                        Log.d(
                            "OpenFDA",
                            "FOUND BY NDC: ${medicine.brandName}"
                        )
                    }

                    medicine
                }

            } catch (e: Exception) {

                Log.e(
                    "OpenFDA",
                    "searchByNdc() failed",
                    e
                )

                null
            }
        }
    }

    /**
     * Search medicine using barcode.
     *
     * First:
     *     openfda.upc
     *
     * Then:
     *     product_ndc
     */
    suspend fun findByBarcode(
        barcode: String
    ): OpenFDADB? {

        return withContext(Dispatchers.IO) {

            try {

                val cleanBarcode = barcode
                    .trim()
                    .replace(Regex("[^0-9]"), "")

                if (cleanBarcode.isBlank()) {
                    return@withContext null
                }

                Log.d(
                    "OpenFDA",
                    "Searching barcode = $cleanBarcode"
                )

                // ==========================================
                // 1. SEARCH UPC
                // ==========================================

                val upcUrl =
                    "$baseUrl?search=openfda.upc:$cleanBarcode&limit=5"

                val upcRequest = Request.Builder()
                    .url(upcUrl)
                    .get()
                    .build()

                client.newCall(upcRequest).execute().use { response ->

                    val json = response.body?.string()

                    if (response.isSuccessful && !json.isNullOrBlank()) {

                        val result =
                            gson.fromJson(
                                json,
                                OpenFDAResponse::class.java
                            )

                        val medicine =
                            result.results
                                ?.firstOrNull()
                                ?.toOpenFDAMedicine()

                        if (medicine != null) {

                            Log.d(
                                "OpenFDA",
                                "FOUND BY UPC = $cleanBarcode"
                            )

                            return@withContext medicine
                        }
                    }
                }

                // ==========================================
                // 2. SEARCH PRODUCT NDC
                // ==========================================

                val ndcUrl =
                    "$baseUrl?search=product_ndc:$cleanBarcode&limit=5"

                val ndcRequest = Request.Builder()
                    .url(ndcUrl)
                    .get()
                    .build()

                client.newCall(ndcRequest).execute().use { response ->

                    val json = response.body?.string()

                    if (response.isSuccessful && !json.isNullOrBlank()) {

                        val result =
                            gson.fromJson(
                                json,
                                OpenFDAResponse::class.java
                            )

                        val medicine =
                            result.results
                                ?.firstOrNull()
                                ?.toOpenFDAMedicine()

                        if (medicine != null) {

                            Log.d(
                                "OpenFDA",
                                "FOUND BY NDC = $cleanBarcode"
                            )

                            return@withContext medicine
                        }
                    }
                }

                Log.d(
                    "OpenFDA",
                    "NO MEDICINE FOUND FOR BARCODE = $cleanBarcode"
                )

                null

            } catch (e: Exception) {

                Log.e(
                    "OpenFDA",
                    "findByBarcode() failed",
                    e
                )

                null
            }
        }
    }

    /**
     * Search medicine by brand name.
     */
    suspend fun searchByName(
        name: String
    ): List<OpenFDADB> {

        return withContext(Dispatchers.IO) {

            try {

                val cleanName = name.trim()

                if (cleanName.isBlank()) {
                    return@withContext emptyList()
                }

                Log.d(
                    "OpenFDA",
                    "Searching medicine name = $cleanName"
                )

                val encodedName =
                    java.net.URLEncoder.encode(
                        cleanName,
                        "UTF-8"
                    )

                val url =
                    "$baseUrl?search=brand_name:$encodedName&limit=5"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->

                    val json = response.body?.string()

                    if (!response.isSuccessful || json.isNullOrBlank()) {
                        return@withContext emptyList()
                    }

                    val result =
                        gson.fromJson(
                            json,
                            OpenFDAResponse::class.java
                        )

                    result.results
                        ?.map {
                            it.toOpenFDAMedicine()
                        }
                        ?: emptyList()
                }

            } catch (e: Exception) {

                Log.e(
                    "OpenFDA",
                    "searchByName() failed",
                    e
                )

                emptyList()
            }
        }
    }
}


/* ============================================================
   OPEN FDA RESPONSE MODELS
   ============================================================ */

data class OpenFDAResponse(

    @SerializedName("meta")
    val meta: OpenFDAMeta? = null,

    @SerializedName("results")
    val results: List<OpenFDAItem>? = null
)


data class OpenFDAMeta(

    @SerializedName("last_updated")
    val lastUpdated: String? = null,

    @SerializedName("results")
    val results: OpenFDAResultMeta? = null
)


data class OpenFDAResultMeta(

    @SerializedName("skip")
    val skip: Int? = null,

    @SerializedName("limit")
    val limit: Int? = null,

    @SerializedName("total")
    val total: Int? = null
)


/* ============================================================
   OPEN FDA MEDICINE
   ============================================================ */

data class OpenFDAItem(

    @SerializedName("product_ndc")
    val productNdc: String? = null,

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
    val activeIngredients: List<OpenFDAIngredient>? = null,

    @SerializedName("indications_and_usage")
    val indicationsAndUsage: List<String>? = null,

    @SerializedName("warnings")
    val warnings: List<String>? = null,

    @SerializedName("adverse_reactions")
    val adverseReactions: List<String>? = null,

    @SerializedName("description")
    val description: List<String>? = null,

    @SerializedName("openfda")
    val openfda: OpenFDAMetaData? = null
) {

    fun toOpenFDAMedicine(): OpenFDADB {

        return OpenFDADB(

            id = productNdc,

            brandName = brandName,

            genericName = genericName,

            manufacturerName = manufacturerName,

            productNdc = productNdc,

            dosageForm = dosageForm,

            route = route,

            activeIngredients =
                activeIngredients?.map {
                    Ingredient(
                        it.name,
                        it.strength
                    )
                },

            indicationsAndUsage =
                indicationsAndUsage?.firstOrNull(),

            warnings =
                warnings?.firstOrNull(),

            adverseReactions =
                adverseReactions?.firstOrNull(),

            description =
                description?.firstOrNull(),

            applicationNumber =
                openfda
                    ?.applicationNumber
                    ?.firstOrNull(),

            approvalDate =
                openfda
                    ?.approvalDate
                    ?.firstOrNull()
        )
    }
}


/* ============================================================
   ACTIVE INGREDIENT
   ============================================================ */

data class OpenFDAIngredient(

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("strength")
    val strength: String? = null
)


/* ============================================================
   OPEN FDA IDENTIFIERS
   ============================================================ */

data class OpenFDAMetaData(

    @SerializedName("application_number")
    val applicationNumber: List<String>? = null,

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
    val rxcui: List<String>? = null,

    @SerializedName("approval_date")
    val approvalDate: List<String>? = null
)
