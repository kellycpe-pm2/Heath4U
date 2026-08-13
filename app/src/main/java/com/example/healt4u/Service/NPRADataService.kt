package com.example.healt4u.Service

import com.example.healt4u.model.NPRAMedicine
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.StringReader
import kotlin.text.get

class NPRADataService {
    private val client = OkHttpClient()
    private val csvUrl = "https://storage.data.gov.my/healthcare/pharmaceutical_products.csv"

    suspend fun fetchAllMedicines(): List<NPRAMedicine> {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(csvUrl).build()
            val response = client.newCall(request).execute()
            val csvContent = response.body?.string() ?: return@withContext emptyList()

            parseCSV(csvContent)
        }
    }



    private fun parseCSV(csvContent: String): List<NPRAMedicine> {
        val medicines = mutableListOf<NPRAMedicine>()
        val lines = csvContent.split("\n")
        if (lines.isEmpty()) return emptyList()

        val header = lines[0].split(",").map { it.trim() }

        val columnMap = header.mapIndexed { index, name -> name to index }.toMap()

        fun List<NPRAMedicine>.getByColumn(columnName: String): String {
            val index = columnMap[columnName] ?: return ""
            return if (index < this.size) this[index].toString() else ""
        }

        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            val columns = parseCSV(line)


            val medicine = NPRAMedicine(
                regNo = columns.getByColumn("reg_no"),
                refNo = columns.getByColumn("ref_no"),
                product = columns.getByColumn("product"),
                status = columns.getByColumn("status"),
                description = columns.getByColumn("description"),
                holder = columns.getByColumn("holder"),
                holderOsa = columns.getByColumn("holder_osa"),
                manufacturer = columns.getByColumn("manufacturer"),
                manufacturerOsa = columns.getByColumn("manufacturer_osa"),
                importer = columns.getByColumn("importer"),
                importerOsa = columns.getByColumn("importer_osa"),
                dateReg = columns.getByColumn("date_reg"),
                dateEnd = columns.getByColumn("date_end"),
                activeIngredient = columns.getByColumn("active_ingredient"),
                mdcCode = columns.getByColumn("mdc_code"),
                genericName = columns.getByColumn("generic_name")
            )
            medicines.add(medicine)
        }
        return medicines
    }
}




