package com.example.healt4u.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Service.BarcodeLookUpService
import com.example.healt4u.model.NPRAMedicine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FindMedicineViewModel @Inject constructor(
    private val barcodeLookUpService: BarcodeLookUpService
) : ViewModel() {

    private val _searchResult =
        MutableStateFlow<UnifiedMedicineResult?>(null)

    val searchResult: StateFlow<UnifiedMedicineResult?> =
        _searchResult.asStateFlow()

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()

    fun searchUnified(value: String) {

        val cleanValue = value.trim()

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

                when (
                    val result =
                        barcodeLookUpService.lookup(cleanValue)
                ) {

                    is BarcodeLookUpService.LookupResult.Found -> {

                        Log.d(
                            TAG,
                            "Medicine found: ${result.medicine}"
                        )

                        _searchResult.value =
                            UnifiedMedicineResult(
                                medicine = result.medicine,
                                query = cleanValue,
                                resolvedMal = result.resolvedMal,
                                source = result.source.name,
                                rawValue = result.rawValue,
                                sourceUrl = result.sourceUrl,
                                isVerified = true
                            )
                    }

                    is BarcodeLookUpService.LookupResult.MalNotFound -> {

                        _errorMessage.value =
                            "MAL number ${result.mal} was found, " +
                                    "but it is not in the NPRA database."
                    }

                    is BarcodeLookUpService.LookupResult.BarcodeNotMapped -> {

                        _errorMessage.value =
                            "Barcode ${result.barcode} is not currently " +
                                    "mapped to an NPRA MAL number."
                    }

                    is BarcodeLookUpService.LookupResult.Quest3PlusResolutionFailed -> {

                        _errorMessage.value =
                            "Unable to obtain the MAL number " +
                                    "from the NPRA QUEST3+ page."
                    }

                    is BarcodeLookUpService.LookupResult.NotFound -> {

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
                    e.message ?: "Unable to find medicine."

            } finally {

                _isLoading.value = false
            }
        }
    }

    fun clearResult() {

        _searchResult.value = null
        _errorMessage.value = null
        _isLoading.value = false
    }

    companion object {
        private const val TAG = "FindMedicineViewModel"
    }
}

data class UnifiedMedicineResult(
    val medicine: NPRAMedicine,
    val query: String,
    val resolvedMal: String,
    val source: String,
    val rawValue: String,
    val sourceUrl: String?,
    val isVerified: Boolean
)