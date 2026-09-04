package com.example.healt4u.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Service.BarcodeLookUpService
import com.example.healt4u.Service.OpenFDAService
import com.example.healt4u.data.MedicineCodeParser
import com.example.healt4u.data.MedicineData
import com.example.healt4u.data.MedicineNameParser
import com.example.healt4u.model.UnifiedMedicineResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


@HiltViewModel
class FindMedicineViewModel @Inject constructor(

    private val barcodeLookUpService:
    BarcodeLookUpService,

    private val openFDAService:
    OpenFDAService

) : ViewModel() {


    // ================================================================
    // RESULT
    // ================================================================

    private val _searchResult =
        MutableStateFlow<UnifiedMedicineResult?>(null)

    val searchResult:
            StateFlow<UnifiedMedicineResult?> =
        _searchResult.asStateFlow()


    // ================================================================
    // LOADING
    // ================================================================

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading:
            StateFlow<Boolean> =
        _isLoading.asStateFlow()


    // ================================================================
    // ERROR
    // ================================================================

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage:
            StateFlow<String?> =
        _errorMessage.asStateFlow()


    // ================================================================
    // MAIN SEARCH
    // ================================================================

    fun searchUnified(
        value: String
    ) {

        val cleanValue =
            value.trim()

        if (cleanValue.isBlank()) {
            return
        }

        viewModelScope.launch {

            _isLoading.value = true

            _errorMessage.value = null

            _searchResult.value = null


            try {

                Log.d(
                    TAG,
                    "Searching: $cleanValue"
                )


                // ========================================================
                // NPRA LOOKUP
                // ========================================================

                when (
                    val result =
                        barcodeLookUpService
                            .lookup(cleanValue)
                ) {


                    // ====================================================
                    // MEDICINE FOUND
                    // ====================================================

                    is BarcodeLookUpService
                    .LookupResult
                    .Found -> {


                        val npra =
                            result.medicine


                        Log.d(
                            TAG,
                            "NPRA medicine found: $npra"
                        )


                        // =================================================
                        // PARSE NAME + DOSAGE
                        // =================================================

                        /*
                         * Example:
                         *
                         * PANADOL 500MG TABLET
                         *
                         * becomes:
                         *
                         * name   = PANADOL
                         * dosage = 500
                         */

                        val parsed =
                            MedicineNameParser.parse(

                                productName =
                                    npra.product,

                                npraDosage =
                                    null
                            )


                        Log.d(
                            TAG,
                            "Parsed name = ${parsed.name}"
                        )

                        Log.d(
                            TAG,
                            "Parsed dosage = ${parsed.dosage}"
                        )


                        // =================================================
                        // MAL CATEGORY
                        // =================================================

                        val categoryCode =
                            MedicineCodeParser
                                .extractCategoryFromMal(
                                    result.resolvedMal
                                )


                        val category =
                            if (
                                !categoryCode.isNullOrBlank()
                            ) {

                                MedicineData
                                    .getCategoryName(
                                        categoryCode
                                    )

                            } else {

                                "Other"
                            }


                        Log.d(
                            TAG,
                            "MAL category code = $categoryCode"
                        )

                        Log.d(
                            TAG,
                            "Category = $category"
                        )


                        // =================================================
                        // FDA LOOKUP
                        // =================================================

                        val fda =
                            if (
                                !npra.activeIngredient
                                    .isNullOrBlank()
                            ) {

                                try {

                                    openFDAService
                                        .searchByIngredient(
                                            npra.activeIngredient
                                        )

                                } catch (e: Exception) {

                                    Log.e(
                                        TAG,
                                        "FDA lookup failed",
                                        e
                                    )

                                    null
                                }

                            } else {

                                null
                            }


                        // =================================================
                        // BUILD FINAL RESULT
                        // =================================================

                        _searchResult.value =
                            UnifiedMedicineResult(

                                medicine =
                                    npra,

                                rawValue =
                                    result.rawValue,

                                resolvedMal =
                                    result.resolvedMal,

                                source =
                                    if (
                                        fda != null
                                    ) {

                                        "NPRA + openFDA"

                                    } else {

                                        result
                                            .source
                                            .name
                                    },

                                isVerified =
                                    true,

                                parsedName =
                                    parsed.name,

                                parsedDosage =
                                    parsed.dosage,

                                category =
                                    category,

                                categoryCode =
                                    categoryCode,

                                fdaInfo =
                                    fda
                            )
                    }


                    // ====================================================
                    // MAL NOT FOUND
                    // ====================================================

                    is BarcodeLookUpService
                    .LookupResult
                    .MalNotFound -> {

                        _errorMessage.value =
                            "MAL number ${result.mal} was found, " +
                                    "but it is not in the NPRA database."
                    }


                    // ====================================================
                    // BARCODE NOT MAPPED
                    // ====================================================

                    is BarcodeLookUpService
                    .LookupResult
                    .BarcodeNotMapped -> {

                        _errorMessage.value =
                            "Barcode ${result.barcode} is not currently " +
                                    "mapped to an NPRA MAL number."
                    }


                    // ====================================================
                    // QUEST3+ FAILED
                    // ====================================================

                    is BarcodeLookUpService
                    .LookupResult
                    .Quest3PlusResolutionFailed -> {

                        _errorMessage.value =
                            "Unable to obtain the MAL number " +
                                    "from the NPRA QUEST3+ page."
                    }


                    // ====================================================
                    // NOTHING FOUND
                    // ====================================================

                    is BarcodeLookUpService
                    .LookupResult
                    .NotFound -> {

                        _errorMessage.value =
                            "No valid MAL number or supported barcode was found."
                    }
                }


            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Medicine lookup failed",
                    e
                )

                _errorMessage.value =
                    e.message
                        ?: "Unable to find medicine."


            } finally {

                _isLoading.value =
                    false
            }
        }
    }


    // ================================================================
    // CLEAR RESULT
    // ================================================================

    fun clearResult() {

        _searchResult.value = null

        _errorMessage.value = null

        _isLoading.value = false
    }


    companion object {

        private const val TAG =
            "FindMedicineViewModel"
    }
}