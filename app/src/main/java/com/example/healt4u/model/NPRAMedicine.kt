package com.example.healt4u.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "npra_medicines")
data class NPRAMedicine(
    @PrimaryKey
    val regNo: String,  // MAL 号码
    val refNo: String? = null,
    val product: String = "",
    val status: String? = null,
    val description: String? = null,
    val holder: String? = null,
    val holderOsa: String? = null,
    val manufacturer: String? = null,
    val manufacturerOsa: String? = null,
    val importer: String? = null,
    val importerOsa: String? = null,
    val dateReg: String? = null,
    val dateEnd: String? = null,
    val activeIngredient: String? = null,
    val mdcCode: String? = null,
    val genericName: String? = null,
    val barcode: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun isActive(): Boolean {
        return status?.equals("Active", ignoreCase = true) == true
    }

    fun isExpired(): Boolean {
        return status?.equals("Expired", ignoreCase = true) == true ||
                status?.equals("Cancelled", ignoreCase = true) == true
    }

    fun getDisplayName(): String {
        return product.ifEmpty { genericName ?: regNo }
    }

    fun getSearchableText(): String {
        return listOf(
            regNo,
            product,
            genericName,
            activeIngredient,
            manufacturer,
            holder,
            barcode
        ).filterNotNull()
            .joinToString(" ")
            .lowercase()
    }
}


fun NPRAMedicine.toDisplayModel(): MedicineDisplay {
    return MedicineDisplay(
        regNo = this.regNo,
        productName = this.product,
        genericName = this.genericName,
        activeIngredients = this.activeIngredient,
        manufacturer = this.manufacturer,
        holder = this.holder,
        registrationStatus = this.status,
        registrationDate = this.dateReg,
        expiryDate = this.dateEnd,
        description = this.description,
        refNo = this.refNo,
        mdcCode = this.mdcCode,
        dataSource = "NPRA",
        isActive = this.isActive(),
        isExpired = this.isExpired()
    )
}

fun List<NPRAMedicine>.toDisplayModels(): List<MedicineDisplay> {
    return this.map { it.toDisplayModel() }
}


data class MedicineDisplay(
    val regNo: String,
    val productName: String,
    val genericName: String? = null,
    val activeIngredients: String? = null,
    val manufacturer: String? = null,
    val holder: String? = null,
    val registrationStatus: String? = null,
    val registrationDate: String? = null,
    val expiryDate: String? = null,
    val description: String? = null,
    val refNo: String? = null,
    val mdcCode: String? = null,
    val dataSource: String = "NPRA",
    val isActive: Boolean = true,
    val isExpired: Boolean = false
) {
    fun getStatusColor(): androidx.compose.ui.graphics.Color {
        return when {
            isActive -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
            isExpired -> androidx.compose.ui.graphics.Color(0xFFF44336)
            else -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        }
    }

    fun getStatusText(): String {
        return when {
            isActive -> "Active"
            isExpired -> "Expired"
            else -> registrationStatus ?: "Unknown"
        }
    }

    fun matchesSearch(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return productName.lowercase().contains(lowerQuery) ||
                genericName?.lowercase()?.contains(lowerQuery) == true ||
                regNo.lowercase().contains(lowerQuery) ||
                activeIngredients?.lowercase()?.contains(lowerQuery) == true ||
                manufacturer?.lowercase()?.contains(lowerQuery) == true
    }
}


data class NpCSVRow(
    @SerializedName("reg_no")
    val regNo: String = "",
    @SerializedName("ref_no")
    val refNo: String? = null,
    @SerializedName("product")
    val product: String = "",
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("holder")
    val holder: String? = null,
    @SerializedName("holder_osa")
    val holderOsa: String? = null,
    @SerializedName("manufacturer")
    val manufacturer: String? = null,
    @SerializedName("manufacturer_osa")
    val manufacturerOsa: String? = null,
    @SerializedName("importer")
    val importer: String? = null,
    @SerializedName("importer_osa")
    val importerOsa: String? = null,
    @SerializedName("date_reg")
    val dateReg: String? = null,
    @SerializedName("date_end")
    val dateEnd: String? = null,
    @SerializedName("active_ingredient")
    val activeIngredient: String? = null,
    @SerializedName("mdc_code")
    val mdcCode: String? = null,
    @SerializedName("generic_name")
    val genericName: String? = null
) {
    fun toNPRAMedicine(): NPRAMedicine {
        return NPRAMedicine(
            regNo = regNo,
            refNo = refNo,
            product = product,
            status = status,
            description = description,
            holder = holder,
            holderOsa = holderOsa,
            manufacturer = manufacturer,
            manufacturerOsa = manufacturerOsa,
            importer = importer,
            importerOsa = importerOsa,
            dateReg = dateReg,
            dateEnd = dateEnd,
            activeIngredient = activeIngredient,
            mdcCode = mdcCode,
            genericName = genericName
        )
    }
}

// ============ 搜索结果的包装类 ============

data class MedicineSearchResult(
    val query: String,
    val results: List<NPRAMedicine>,
    val totalCount: Int,
    val hasResults: Boolean = results.isNotEmpty()
) {
    fun getDisplayModels(): List<MedicineDisplay> {
        return results.toDisplayModels()
    }

    fun getSummary(): String {
        return if (hasResults) {
            "Found $totalCount medicine(s) matching '$query'"
        } else {
            "No medicines found matching '$query'"
        }
    }
}

// ============ 排序和过滤扩展 ============

fun List<NPRAMedicine>.filterActive(): List<NPRAMedicine> {
    return this.filter { it.isActive() }
}

fun List<NPRAMedicine>.filterByStatus(status: String): List<NPRAMedicine> {
    return this.filter { it.status?.equals(status, ignoreCase = true) == true }
}

fun List<NPRAMedicine>.sortByName(): List<NPRAMedicine> {
    return this.sortedBy { it.product }
}

fun List<NPRAMedicine>.sortByRegNo(): List<NPRAMedicine> {
    return this.sortedBy { it.regNo }
}

fun List<NPRAMedicine>.search(query: String): List<NPRAMedicine> {
    val lowerQuery = query.lowercase().trim()
    if (lowerQuery.isEmpty()) return this

    return this.filter { medicine ->
        medicine.regNo.lowercase().contains(lowerQuery) ||
                medicine.product.lowercase().contains(lowerQuery) ||
                medicine.genericName?.lowercase()?.contains(lowerQuery) == true ||
                medicine.activeIngredient?.lowercase()?.contains(lowerQuery) == true ||
                medicine.manufacturer?.lowercase()?.contains(lowerQuery) == true ||
                medicine.barcode?.lowercase()?.contains(lowerQuery) == true
    }
}