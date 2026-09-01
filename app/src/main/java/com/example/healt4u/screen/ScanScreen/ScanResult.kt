package com.example.healt4u.screen.ScanScreen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.model.NPRAMedicine
import com.example.healt4u.model.toDisplayModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResult(
    barcode: String,
    medicine: NPRAMedicine?,
    medicines: List<NPRAMedicine> = emptyList(),
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onItemClick: ((NPRAMedicine) -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Medicine Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                // Loading State
                isLoading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Searching for medicine information...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Error State
                errorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "❌",
                            fontSize = 48.sp
                        )
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Go Back")
                        }
                    }
                }

                // Single Medicine Result
                medicine != null -> {
                    MedicineDetailContent(medicine = medicine)
                }

                // Multiple Results
                medicines.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Found ${medicines.size} related medicines",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                        }
                        items(medicines) { item ->
                            MedicineResultItem(
                                medicine = item,
                                onClick = {
                                    onItemClick?.invoke(item)
                                }
                            )
                        }
                    }
                }

                // Empty State
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📋",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "No medicine information found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Search: $barcode",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineDetailContent(medicine: NPRAMedicine) {
    val displayData = medicine.toDisplayModel()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Product Name
        item {
            Text(
                text = displayData.productName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Generic Name
        item {
            displayData.genericName?.let {
                Text(
                    text = it,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // MAL Number Chip
        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "MAL: ${displayData.regNo}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Status
        item {
            displayData.registrationStatus?.let { status ->
                val statusColor = when (status.lowercase()) {
                    "active" -> Color(0xFF4CAF50)
                    "expired" -> Color(0xFFF44336)
                    "cancelled", "revoked" -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = statusColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

        // Basic Information
        item {
            InfoCard(
                title = "Basic Information",
                icon = Icons.Default.Info,
                items = listOf(
                    "Product Name" to displayData.productName,
                    "Generic Name" to (displayData.genericName ?: "Not provided"),
                    "Registration No" to displayData.regNo,
                    "Reference No" to (displayData.refNo ?: "Not provided"),
                    "MDC Code" to (displayData.mdcCode ?: "Not provided")
                )
            )
        }

        // Manufacturer Information
        item {
            InfoCard(
                title = "Manufacturer Information",
                icon = Icons.Default.Business,
                items = listOf(
                    "Product Holder" to (displayData.holder ?: "Not provided"),
                    "Manufacturer" to (displayData.manufacturer ?: "Not provided")
                )
            )
        }

        // Ingredients
        item {
            displayData.activeIngredients?.let { ingredients ->
                InfoCard(
                    title = "Active Ingredients",
                    icon = Icons.Default.Science,
                    items = listOf(
                        "Active Ingredient" to ingredients
                    )
                )
            }
        }

        // Registration Information
        item {
            InfoCard(
                title = "Registration Information",
                icon = Icons.Default.CalendarToday,
                items = listOf(
                    "Registration Date" to (displayData.registrationDate ?: "Not provided"),
                    "Expiry Date" to (displayData.expiryDate ?: "Not provided")
                )
            )
        }

        // Description
        item {
            displayData.description?.let { desc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Description",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Divider()
                        Text(
                            text = desc,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Data Source
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Data Source: ${displayData.dataSource}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Disclaimer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 16.sp
                    )
                    Text(
                        text = "This information is for reference only. Please consult your doctor or pharmacist before using any medication.",
                        fontSize = 11.sp,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Divider()
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(0.4f)
                    )
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = if (value == "Not provided") null else FontWeight.Medium,
                        modifier = Modifier.weight(0.6f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun MedicineResultItem(
    medicine: NPRAMedicine,
    onClick: () -> Unit
) {
    val displayData = medicine.toDisplayModel()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = displayData.productName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                displayData.genericName?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MAL: ${displayData.regNo}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    displayData.registrationStatus?.let { status ->
                        val statusColor = when (status.lowercase()) {
                            "active" -> Color(0xFF4CAF50)
                            "expired" -> Color(0xFFF44336)
                            else -> Color(0xFFFF9800)
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Extension function to convert NPRAMedicine to display model
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
        dataSource = "NPRA"
    )
}

// Display model for UI
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
    val dataSource: String = "NPRA"
)