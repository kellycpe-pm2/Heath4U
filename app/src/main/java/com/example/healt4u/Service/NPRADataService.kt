package com.example.healt4u.Service

import android.content.Context
import android.util.Log
import com.example.healt4u.model.NPRAMedicine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.StringReader
import java.util.concurrent.TimeUnit

class NPRADataService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val csvUrl = "https://storage.data.gov.my/healthcare/pharmaceutical_products.csv"
    private var cachedMedicines: List<NPRAMedicine>? = null

    // ============ 获取数据 ============

    suspend fun fetchAllMedicines(forceRefresh: Boolean = false): List<NPRAMedicine> {
        return withContext(Dispatchers.IO) {
            try {
                if (!forceRefresh && cachedMedicines != null) {
                    return@withContext cachedMedicines!!
                }

                val request = Request.Builder()
                    .url(csvUrl)
                    .header("User-Agent", "Health4U-App")
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("NPRADataService", "HTTP Error: ${response.code}")
                    return@withContext loadFromAssets()
                }

                val csvContent = response.body?.string()
                if (csvContent.isNullOrEmpty()) {
                    return@withContext loadFromAssets()
                }

                val medicines = parseCSV(csvContent)
                if (medicines.isNotEmpty()) {
                    cachedMedicines = medicines
                    Log.d("NPRADataService", "✅ 加载了 ${medicines.size} 条药品数据")
                }
                medicines
            } catch (e: Exception) {
                Log.e("NPRADataService", "❌ 网络错误: ${e.message}")
                loadFromAssets()
            }
        }
    }

    fun loadFromAssets(fileName: String = "npra_data.csv"): List<NPRAMedicine> {
        return try {
            val inputStream = context.assets.open(fileName)
            val csvContent = inputStream.bufferedReader().use { it.readText() }
            val medicines = parseCSV(csvContent)
            if (medicines.isNotEmpty()) {
                cachedMedicines = medicines
            }
            medicines
        } catch (e: Exception) {
            Log.e("NPRADataService", "❌ 本地加载失败: ${e.message}")
            emptyList()
        }
    }

    // ============ CSV 解析 ============

    private fun parseCSV(csvContent: String): List<NPRAMedicine> {
        val medicines = mutableListOf<NPRAMedicine>()
        val lines = csvContent.split("\n")
        if (lines.isEmpty()) return emptyList()

        val header = parseCSVLine(lines[0])
        val columnMap = header.mapIndexed { index, name -> name.trim() to index }.toMap()

        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            try {
                val columns = parseCSVLine(line)

                fun List<String>.getColumn(name: String): String {
                    val index = columnMap[name] ?: return ""
                    return if (index < this.size) this[index].trim() else ""
                }

                val medicine = NPRAMedicine(
                    regNo = columns.getColumn("reg_no"),
                    refNo = columns.getColumn("ref_no"),
                    product = columns.getColumn("product"),
                    status = columns.getColumn("status"),
                    description = columns.getColumn("description"),
                    holder = columns.getColumn("holder"),
                    holderOsa = columns.getColumn("holder_osa"),
                    manufacturer = columns.getColumn("manufacturer"),
                    manufacturerOsa = columns.getColumn("manufacturer_osa"),
                    importer = columns.getColumn("importer"),
                    importerOsa = columns.getColumn("importer_osa"),
                    dateReg = columns.getColumn("date_reg"),
                    dateEnd = columns.getColumn("date_end"),
                    activeIngredient = columns.getColumn("active_ingredient"),
                    mdcCode = columns.getColumn("mdc_code"),
                    genericName = columns.getColumn("generic_name")
                )

                if (medicine.regNo.isNotEmpty() || medicine.product.isNotEmpty()) {
                    medicines.add(medicine)
                }
            } catch (e: Exception) {
                continue
            }
        }
        return medicines
    }

    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val reader = StringReader(line)
        var current = StringBuilder()
        var inQuotes = false

        while (true) {
            val char = reader.read()
            if (char == -1) break
            val c = char.toChar()
            when {
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(c)
            }
        }
        result.add(current.toString().trim())
        return result
    }


    fun searchByRegNo(regNo: String): NPRAMedicine? {
        val all = cachedMedicines ?: return null
        return all.find { it.regNo.uppercase() == regNo.trim().uppercase() }
    }

    fun searchByProductName(query: String): List<NPRAMedicine> {
        val all = cachedMedicines ?: return emptyList()
        val cleanQuery = query.trim().lowercase()
        return all.filter {
            it.product.lowercase().contains(cleanQuery) ||
                    it.genericName?.lowercase()?.contains(cleanQuery) == true ||
                    it.activeIngredient?.lowercase()?.contains(cleanQuery) == true
        }
    }

    fun searchByBarcode(barcode: String): NPRAMedicine? {
        val all = cachedMedicines ?: return null
        return all.find { it.barcode == barcode } ?: searchByRegNo(barcode)
    }

    fun searchByRegNoContains(query: String): List<NPRAMedicine> {
        val all = cachedMedicines ?: return emptyList()
        return all.filter { it.regNo.uppercase().contains(query.trim().uppercase()) }
    }

    fun getAllMedicines(): List<NPRAMedicine> {
        return cachedMedicines ?: emptyList()
    }

    fun getProductCount(): Int {
        return cachedMedicines?.size ?: 0
    }
}