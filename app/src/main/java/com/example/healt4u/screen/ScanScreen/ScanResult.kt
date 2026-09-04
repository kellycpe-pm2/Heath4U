package com.example.healt4u.screen.ScanScreen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.model.NPRAMedicine
import com.example.healt4u.model.UnifiedMedicineResult
import kotlinx.coroutines.delay

// ========================================================================
// MAIN SCAN RESULT
// ========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResult(
    result: UnifiedMedicineResult?,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onAddMedicine: ((UnifiedMedicineResult) -> Unit)? = null,
    onAddToReminder: ((NPRAMedicine) -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "💊 Medicine Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            if (result != null && !isLoading) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        ),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        onAddMedicine?.let { callback ->
                            Button(
                                onClick = { callback(result) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Add to My Medicines",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        onAddToReminder?.let { callback ->
                            OutlinedButton(
                                onClick = { callback(result.medicine) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Set Reminder",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> LoadingMedicineScreen()
                errorMessage != null -> ErrorMedicineScreen(message = errorMessage, onBack = onBack)
                result != null -> MedicineResultContent(result = result)
                else -> EmptyMedicineScreen()
            }
        }
    }
}

// ========================================================================
// LOADING SCREEN
// ========================================================================

@Composable
private fun LoadingMedicineScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        Color.White
                    )
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .rotate(rotation),
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Medication,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Searching for Medicine",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Checking NPRA and FDA databases...",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        var dotCount by remember { mutableStateOf(0) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(400)
                dotCount = (dotCount + 1) % 4
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = ".".repeat(dotCount),
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ========================================================================
// ERROR SCREEN
// ========================================================================

@Composable
private fun ErrorMedicineScreen(
    message: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                        Color.White
                    )
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Medicine Not Found",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Scanner", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ========================================================================
// EMPTY SCREEN
// ========================================================================

@Composable
private fun EmptyMedicineScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Medication,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Medicine Result",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Scan a medicine barcode or enter a MAL number to search",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ========================================================================
// MEDICINE RESULT CONTENT
// ========================================================================

@Composable
private fun MedicineResultContent(
    result: UnifiedMedicineResult
) {
    val medicine = result.medicine

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 140.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ================================================================
        // HEADER CARD
        // ================================================================
        item {
            AnimatedItem {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Medication,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Medicine",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = medicine.product.takeIf { it.isNotBlank() } ?: "Unknown",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Verified badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (result.isVerified) {
                                    Color(0xFF4CAF50).copy(alpha = 0.3f)
                                } else {
                                    Color(0xFFFF5722).copy(alpha = 0.3f)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (result.isVerified) {
                                            Icons.Default.CheckCircle
                                        } else {
                                            Icons.Default.Warning
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (result.isVerified) Color(0xFF4CAF50) else Color(0xFFFF5722)
                                    )
                                    Text(
                                        text = if (result.isVerified) "Verified" else "Not Verified",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (result.isVerified) Color(0xFF4CAF50) else Color(0xFFFF5722)
                                    )
                                }
                            }

                            // Source badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Source: ${result.source}",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ================================================================
        // QUICK INFO
        // ================================================================
        item {
            AnimatedItem(delay = 100) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickInfoItem(
                                icon = Icons.Outlined.QrCode,
                                label = "MAL Number",
                                value = result.resolvedMal
                            )
                            QuickInfoItem(
                                icon = Icons.Outlined.Numbers,
                                label = "Registration",
                                value = medicine.regNo?.takeIf { it.isNotBlank() } ?: "-"
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val category = extractMalCategory(result.resolvedMal)
                            QuickInfoItem(
                                icon = Icons.Outlined.Category,
                                label = "Category",
                                value = when (category) {
                                    "A" -> "Scheduled Poison"
                                    "B" -> "Natural Products"
                                    "X" -> "Non-scheduled Poisons"
                                    "N" -> "Health Supplements"
                                    "T" -> "Traditional Products"
                                    "H" -> "Veterinary Products"
                                    else -> "Other"
                                }
                            )
                            QuickInfoItem(
                                icon = Icons.Outlined.Info,
                                label = "Status",
                                value = medicine.status?.takeIf { it.isNotBlank() } ?: "-"
                            )
                        }
                    }
                }
            }
        }

        // ================================================================
        // DETAILS SECTION TITLE
        // ================================================================
        item {
            AnimatedItem(delay = 150) {
                Text(
                    text = "📋 Medicine Information",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A2E),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // ================================================================
        // DETAIL CARDS
        // ================================================================
        val details = listOf(
            "Generic Name" to medicine.genericName,
            "Active Ingredient" to medicine.activeIngredient,
            "Dosage / Strength" to medicine.strength,
            "Dosage Form" to medicine.dosageForm,
            "Description" to medicine.description,
            "Product Holder" to medicine.holder,
            "Manufacturer" to medicine.manufacturer,
            "Importer" to medicine.importer,
            "Registration Date" to medicine.dateReg,
            "Registration Expiry" to medicine.dateEnd,
            "MDC Code" to medicine.mdcCode,
            "Reference Number" to medicine.refNo
        )

        details.forEach { (title, value) ->
            if (!value.isNullOrBlank()) {
                item {
                    AnimatedItem(delay = 200) {
                        DetailCard(title = title, value = value)
                    }
                }
            }
        }

        // ================================================================
        // FDA INFORMATION
        // ================================================================
        result.fdaInfo?.let { fda ->
            val fdaDetails = listOf(
                "Brand Name" to fda.brandName,
                "Generic Name" to fda.genericName,
                "Active Ingredient" to fda.activeIngredient,
                "Manufacturer" to fda.manufacturer,
                "Purpose" to fda.purpose,
                "Indications & Usage" to fda.indicationsAndUsage,
                "Dosage & Administration" to fda.dosageAndAdministration,
                "Warnings" to fda.warnings,
                "Contraindications" to fda.contraindications,
                "Description" to fda.description
            )

            val hasData = fdaDetails.any { !it.second.isNullOrBlank() }

            if (hasData) {
                item {
                    AnimatedItem(delay = 250) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE3F2FD)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF1565C0)
                                    )
                                    Text(
                                        text = "🏥 FDA Information",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1565C0)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                fdaDetails.forEach { (title, value) ->
                                    if (!value.isNullOrBlank()) {
                                        FdaInfoRow(title = title, value = value)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Source: ${fda.source}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ================================================================
        // SAFETY WARNING
        // ================================================================
        item {
            AnimatedItem(delay = 300) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "⚠️ Always check the medicine packaging and follow the advice of a qualified healthcare professional.",
                            fontSize = 13.sp,
                            color = Color(0xFF4E342E),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

// ========================================================================
// ANIMATED ITEM WRAPPER
// ========================================================================

@Composable
private fun AnimatedItem(
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
    ) {
        content()
    }
}

// ========================================================================
// QUICK INFO ITEM
// ========================================================================

@Composable
private fun QuickInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Color(0xFFF5F5F5),
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                label,
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A2E),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ========================================================================
// DETAIL CARD
// ========================================================================

@Composable
private fun DetailCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                lineHeight = 20.sp
            )
        }
    }
}

// ========================================================================
// FDA INFO ROW
// ========================================================================

@Composable
private fun FdaInfoRow(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF1A1A2E)
        )
        Divider(
            color = Color(0xFFEEEEEE),
            thickness = 0.5.dp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// ========================================================================
// EXTRACT MAL CATEGORY
// ========================================================================

private fun extractMalCategory(value: String): String? {
    val mal = value
        .trim()
        .uppercase()
        .replace(Regex("\\s+"), "")

    return Regex("""^MAL\d{8}([ABXNTH])""")
        .find(mal)
        ?.groupValues
        ?.getOrNull(1)
}