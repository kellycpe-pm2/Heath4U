package com.example.healt4u.Service

import android.util.Log
import com.example.healt4u.model.FdaMedicineInfo
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

@Singleton
class OpenFDAService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
        suspend fun searchByIngredient(
            ingredient: String
        ): FdaMedicineInfo? {

            return withContext(Dispatchers.IO) {

                try {

                    val cleanIngredient =
                        ingredient
                            .trim()
                            .replace(
                                Regex("\\s+"),
                                " "
                            )

                    if (cleanIngredient.isBlank()) {
                        return@withContext null
                    }

                    Log.d(
                        "FDA",
                        "Searching ingredient: $cleanIngredient"
                    )

                    /*
                     * openFDA field search.
                     *
                     * Example:
                     *
                     * active_ingredient:paracetamol
                     */

                    val encoded =
                        URLEncoder.encode(
                            cleanIngredient,
                            StandardCharsets.UTF_8.toString()
                        )

                    val query =
                        "active_ingredient:$encoded"

                    val response =
                        FdaClient.api.searchDrug(
                            search = query,
                            limit = 5
                        )

                    val label =
                        response.results
                            ?.firstOrNull()
                            ?: return@withContext null

                    val openFda =
                        label.openFda

                    FdaMedicineInfo(

                        brandName =
                            openFda
                                ?.brandName
                                ?.firstOrNull(),

                        genericName =
                            openFda
                                ?.genericName
                                ?.firstOrNull(),

                        activeIngredient =
                            label.activeIngredient
                                ?.firstOrNull()
                                ?: openFda
                                    ?.substanceName
                                    ?.firstOrNull(),

                        purpose =
                            label.purpose
                                ?.firstOrNull(),

                        indicationsAndUsage =
                            label.indicationsAndUsage
                                ?.firstOrNull(),

                        warnings =
                            label.warnings
                                ?.firstOrNull(),

                        dosageAndAdministration =
                            label.dosageAndAdministration
                                ?.firstOrNull(),

                        contraindications =
                            label.contraindications
                                ?.firstOrNull(),

                        description =
                            label.description
                                ?.firstOrNull(),

                        manufacturer =
                            openFda
                                ?.manufacturerName
                                ?.firstOrNull(),

                        source = "openFDA"
                    )

                } catch (e: Exception) {

                    Log.e(
                        "FDA",
                        "FDA lookup failed",
                        e
                    )

                    /*
                     * FDA is supplementary.
                     *
                     * If FDA fails, NPRA should
                     * continue working normally.
                     */

                    null
                }
            }
        }
    }
