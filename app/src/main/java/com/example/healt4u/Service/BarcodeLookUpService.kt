package com.example.healt4u.Service

import com.example.healt4u.data.MedicineCodeParser
import com.example.healt4u.model.NPRAMedicine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarcodeLookUpService @Inject constructor(
    private val npraDataService: NPRADataService
) {

    sealed class LookupResult {

        data class Found(
            val medicine: NPRAMedicine,
            val resolvedMal: String,
            val rawValue: String,
            val source: Source,
            val sourceUrl: String? = null
        ) : LookupResult()

        data class MalNotFound(
            val mal: String,
            val rawValue: String
        ) : LookupResult()

        data class BarcodeNotMapped(
            val barcode: String,
            val rawValue: String
        ) : LookupResult()

        data class Quest3PlusResolutionFailed(
            val url: String
        ) : LookupResult()

        data class NotFound(
            val rawValue: String
        ) : LookupResult()
    }

    enum class Source {
        MAL,
        QUEST3PLUS,
        BARCODE
    }

    suspend fun lookup(rawValue: String): LookupResult {

        val value = rawValue.trim()

        if (value.isBlank()) {
            return LookupResult.NotFound(rawValue)
        }

        // ============================================================
        // 1. EXTRACT MAL NUMBER
        // ============================================================

        val mal = MedicineCodeParser.extractMal(value)

        if (mal != null) {
            return findByMal(
                mal = mal,
                rawValue = rawValue,
                source = Source.MAL
            )
        }

        // ============================================================
        // 2. QUEST3+ URL
        // ============================================================

        if (MedicineCodeParser.isQuest3PlusUrl(value)) {
            return resolveQuest3Plus(value)
        }

        // ============================================================
        // 3. NORMAL BARCODE
        // ============================================================

        if (MedicineCodeParser.isBarcode(value)) {
            return lookupBarcode(value)
        }

        return LookupResult.NotFound(rawValue)
    }

    // ================================================================
    // FIND BY MAL
    // ================================================================

    private suspend fun findByMal(
        mal: String,
        rawValue: String,
        source: Source,
        sourceUrl: String? = null
    ): LookupResult {

        val medicine = npraDataService.searchByRegNo(mal)

        return if (medicine != null) {

            LookupResult.Found(
                medicine = medicine,
                resolvedMal = mal,
                rawValue = rawValue,
                source = source,
                sourceUrl = sourceUrl
            )

        } else {

            LookupResult.MalNotFound(
                mal = mal,
                rawValue = rawValue
            )
        }
    }

    // ================================================================
    // BARCODE
    // ================================================================

    private suspend fun lookupBarcode(
        barcode: String
    ): LookupResult {

        val mal = VERIFIED_BARCODE_TO_MAL[barcode]

        if (mal == null) {

            return LookupResult.BarcodeNotMapped(
                barcode = barcode,
                rawValue = barcode
            )
        }

        return findByMal(
            mal = mal,
            rawValue = barcode,
            source = Source.BARCODE
        )
    }

    // ================================================================
    // QUEST3+
    // ================================================================

    private suspend fun resolveQuest3Plus(
        url: String
    ): LookupResult {

        val malFromUrl =
            MedicineCodeParser.extractMalFromQuest3PlusUrl(url)

        if (malFromUrl != null) {

            return findByMal(
                mal = malFromUrl,
                rawValue = url,
                source = Source.QUEST3PLUS,
                sourceUrl = url
            )
        }

        val html =
            fetchQuest3PlusPage(url)
                ?: return LookupResult.Quest3PlusResolutionFailed(url)

        val malFromPage =
            MedicineCodeParser.extractMal(html)

        if (malFromPage == null) {
            return LookupResult.Quest3PlusResolutionFailed(url)
        }

        return findByMal(
            mal = malFromPage,
            rawValue = url,
            source = Source.QUEST3PLUS,
            sourceUrl = url
        )
    }

    // ================================================================
    // FETCH QUEST3+
    // ================================================================

    private suspend fun fetchQuest3PlusPage(
        urlString: String
    ): String? {

        return withContext(Dispatchers.IO) {

            var connection: HttpURLConnection? = null

            try {

                val url = URL(urlString)

                if (
                    !url.protocol.equals(
                        "https",
                        ignoreCase = true
                    )
                ) {
                    return@withContext null
                }

                if (
                    !MedicineCodeParser.isQuest3PlusUrl(
                        urlString
                    )
                ) {
                    return@withContext null
                }

                connection =
                    url.openConnection() as HttpURLConnection

                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.instanceFollowRedirects = false

                connection.setRequestProperty(
                    "User-Agent",
                    "Health4U-Android"
                )

                connection.setRequestProperty(
                    "Accept",
                    "text/html"
                )

                if (connection.responseCode !in 200..299) {
                    return@withContext null
                }

                val bytes =
                    connection.inputStream.use {
                        it.readBytes()
                    }

                if (bytes.size > 2 * 1024 * 1024) {
                    return@withContext null
                }

                String(
                    bytes,
                    Charsets.UTF_8
                )

            } catch (_: Exception) {

                null

            } finally {

                connection?.disconnect()
            }
        }
    }

    companion object {

        private val VERIFIED_BARCODE_TO_MAL =
            mapOf(

                "9556019237858" to "MAL12035013X",

                "9557201000489" to "MAL05041936AZ",

                "5060337260527" to "MAL19913211ACZ",

                "9557328000058" to "MAL21086010AZ"
            )
    }
}