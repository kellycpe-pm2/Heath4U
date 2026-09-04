package com.example.healt4u.Service

import android.content.Context
import android.util.Log
import com.example.healt4u.model.NPRAMedicine
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.StringReader
import java.util.concurrent.TimeUnit

@Singleton
class NPRADataService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val csvUrl =
        "https://storage.data.gov.my/healthcare/pharmaceutical_products.csv"

    private var cachedMedicines: List<NPRAMedicine>? = null

    suspend fun fetchAllMedicines(): List<NPRAMedicine> {

        return withContext(Dispatchers.IO) {

            cachedMedicines?.let {
                return@withContext it
            }

            try {

                Log.d(
                    TAG,
                    "Downloading NPRA pharmaceutical CSV..."
                )

                val request = Request.Builder()
                    .url(csvUrl)
                    .header(
                        "User-Agent",
                        "Health4U-App"
                    )
                    .build()

                client.newCall(request).execute().use { response ->

                    if (!response.isSuccessful) {

                        Log.w(
                            TAG,
                            "NPRA download failed: ${response.code}"
                        )

                        return@withContext loadFromAssets()
                    }

                    val csvContent =
                        response.body?.string()

                    if (csvContent.isNullOrBlank()) {

                        return@withContext loadFromAssets()
                    }

                    val medicines =
                        parseCSV(csvContent)

                    cachedMedicines = medicines

                    Log.d(
                        TAG,
                        "Loaded ${medicines.size} NPRA medicines"
                    )

                    medicines
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "NPRA download error",
                    e
                )

                loadFromAssets()
            }
        }
    }


    suspend fun loadFromAssets(
        fileName: String = "pharmaceutical_products (4).csv"
    ): List<NPRAMedicine> {

        return withContext(Dispatchers.IO) {

            try {

                cachedMedicines?.let {
                    return@withContext it
                }

                context.assets.open(fileName).use { inputStream ->

                    val csvContent =
                        inputStream
                            .bufferedReader(Charsets.UTF_8)
                            .use { it.readText() }

                    val medicines =
                        parseCSV(csvContent)

                    cachedMedicines = medicines

                    Log.d(
                        TAG,
                        "Loaded ${medicines.size} NPRA medicines from assets"
                    )

                    medicines
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Unable to load NPRA CSV from assets",
                    e
                )

                emptyList()
            }
        }
    }

    suspend fun searchByRegNo(
        regNo: String
    ): NPRAMedicine? {

        val medicines =
            getLoadedMedicines()

        val target =
            normalizeRegNo(regNo)

        if (target.isBlank()) {
            return null
        }

        return medicines.firstOrNull {

            normalizeRegNo(it.regNo) == target
        }
    }


    suspend fun searchByProductName(
        query: String
    ): List<NPRAMedicine> {

        val medicines =
            getLoadedMedicines()

        val cleanQuery =
            query.trim().lowercase()

        if (cleanQuery.isBlank()) {
            return emptyList()
        }

        return medicines
            .asSequence()
            .filter {

                it.product
                    .lowercase()
                    .contains(cleanQuery) ||

                        it.genericName
                            ?.lowercase()
                            ?.contains(cleanQuery) == true ||

                        it.activeIngredient
                            ?.lowercase()
                            ?.contains(cleanQuery) == true
            }
            .take(50)
            .toList()
    }

    /**
     * Get all loaded NPRA medicines.
     */
    suspend fun getAllMedicines(): List<NPRAMedicine> {
        return getLoadedMedicines()
    }


    suspend fun getProductCount(): Int {
        return getLoadedMedicines().size
    }


    private suspend fun getLoadedMedicines():
            List<NPRAMedicine> {

        cachedMedicines?.let {
            return it
        }

        return fetchAllMedicines()
    }

    private fun normalizeRegNo(
        value: String
    ): String {

        return value
            .trim()
            .uppercase()
            .replace("\\s+".toRegex(), "")
    }

    private fun parseCSV(
        csvContent: String
    ): List<NPRAMedicine> {

        val medicines =
            mutableListOf<NPRAMedicine>()

        val lines =
            csvContent
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .split("\n")

        if (lines.isEmpty()) {
            return emptyList()
        }

        val header =
            parseCSVLine(lines[0])

        val columnMap =
            header
                .mapIndexed { index, name ->
                    name
                        .trim()
                        .removePrefix("\uFEFF") to index
                }
                .toMap()

        fun List<String>.getColumn(
            name: String
        ): String {

            val index =
                columnMap[name]
                    ?: return ""

            return if (
                index >= 0 &&
                index < size
            ) {
                this[index].trim()
            } else {
                ""
            }
        }

        for (i in 1 until lines.size) {

            val line =
                lines[i]

            if (line.isBlank()) {
                continue
            }

            try {

                val columns =
                    parseCSVLine(line)

                val medicine =
                    NPRAMedicine(

                        regNo =
                            columns.getColumn("reg_no"),

                        refNo =
                            columns.getColumn("ref_no"),

                        product =
                            columns.getColumn("product"),

                        status =
                            columns.getColumn("status"),

                        description =
                            columns.getColumn("description"),

                        holder =
                            columns.getColumn("holder"),

                        holderOsa =
                            columns.getColumn("holder_osa"),

                        manufacturer =
                            columns.getColumn("manufacturer"),

                        manufacturerOsa =
                            columns.getColumn("manufacturer_osa"),

                        importer =
                            columns.getColumn("importer"),

                        importerOsa =
                            columns.getColumn("importer_osa"),

                        dateReg =
                            columns.getColumn("date_reg"),

                        dateEnd =
                            columns.getColumn("date_end"),

                        activeIngredient =
                            columns.getColumn("active_ingredient"),

                        mdcCode =
                            columns.getColumn("mdc_code"),

                        genericName =
                            columns.getColumn("generic_name")
                    )

                if (
                    medicine.regNo.isNotBlank() ||
                    medicine.product.isNotBlank()
                ) {

                    medicines.add(medicine)
                }

            } catch (e: Exception) {

                Log.w(
                    TAG,
                    "Skipping malformed CSV row $i"
                )
            }
        }

        return medicines
    }


    private fun parseCSVLine(
        line: String
    ): List<String> {

        val result =
            mutableListOf<String>()

        val current =
            StringBuilder()

        var insideQuotes =
            false

        var index =
            0

        while (index < line.length) {

            val c =
                line[index]

            when {

                c == '"' -> {

                    // Escaped quote: ""
                    if (
                        insideQuotes &&
                        index + 1 < line.length &&
                        line[index + 1] == '"'
                    ) {

                        current.append('"')
                        index++

                    } else {

                        insideQuotes =
                            !insideQuotes
                    }
                }

                c == ',' && !insideQuotes -> {

                    result.add(
                        current.toString().trim()
                    )

                    current.clear()
                }

                else -> {

                    current.append(c)
                }
            }

            index++
        }

        result.add(
            current.toString().trim()
        )

        return result
    }

    companion object {

        private const val TAG =
            "NPRADataService"
    }
}
