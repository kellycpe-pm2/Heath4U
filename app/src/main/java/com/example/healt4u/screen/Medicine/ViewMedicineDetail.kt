package com.example.healt4u.screen.Medicine

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.data.MedicineData
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.screen.componentUI.button
import java.text.SimpleDateFormat
import java.util.*




fun getExpiryStatus(expiredDate: Long?): String {
    if (expiredDate == null) return "No expiry date"
    val daysLeft = (expiredDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
    return when {
        daysLeft < 0 -> "Expired! 🚫"
        daysLeft < 7 -> "Expires in ${daysLeft}d ⚠️"
        daysLeft < 30 -> "Expires in ${daysLeft}d"
        else -> "Valid ✅"
    }
}


// ===================== MAIN SCREEN =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    medicine: Medicine?,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val total by remember { mutableStateOf(medicine?.quantity ?: 0) }
    val stock by remember { mutableStateOf(medicine?.quantityLeft ?: 0) }

    // Calculate percentage with proper null safety
    val stockPercentage = remember(stock, total) {
        if (total > 0) (stock.toFloat() / total) * 100 else 0f
    }
    colorTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Medicine Details",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .padding(end=48.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF5F7FA),
                                Color(0xFFFFFFFF)
                            )
                        )
                    )
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ===================== HEADER CARD =====================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.secondary,
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            // Medicine icon + name
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "💊",
                                            fontSize = 28.sp
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = medicine?.name_medicine ?: "—",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===================== STOCK PROGRESS CARD =====================
                AnimatedContent(
                    targetState = medicine,
                    transitionSpec = {
                        fadeIn() + slideInVertically() togetherWith
                                fadeOut() + slideOutVertically()
                    }
                ) { med ->
                    if (med != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    clip = false
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = getQuantityLeftEmoji(stock),
                                            fontSize = 20.sp
                                        )
                                        Text(
                                            text = "Stock Status",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF333333)
                                        )
                                    }

                                    Text(
                                        text = "$stock / $total",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = getQuantityLeftColor(stock)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE8E8E8))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(stockPercentage / 100f)
                                            .fillMaxHeight()
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        getQuantityLeftColor(stock),
                                                        getQuantityLeftColor(stock).copy(alpha = 0.7f)
                                                    )
                                                )
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = when {
                                            stock <= 0 -> "Out of Stock"
                                            stock <= 5 -> "Critical - Reorder Now!"
                                            stock <= 10 -> "Low Stock"
                                            stock <= 25 -> "Medium Stock"
                                            else -> "In Stock"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = getQuantityLeftColor(stock)
                                    )

                                    Text(
                                        text = "${String.format("%.0f", stockPercentage)}%",
                                        fontSize = 13.sp,
                                        color = Color(0xFF757575)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===================== DETAILS GRID =====================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Section title
                        Text(
                            text = "📋 Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Info items in 2 columns
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            InfoItem(
                                icon = Icons.Default.Category,
                                label = "Category",
                                value = medicine?.category ?: "—",
                                iconColor = Color(0xFF1976D2)
                            )

                            Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                            InfoItem(
                                icon = Icons.Default.Medication,
                                label = "Dosage",
                                value = "${medicine?.dosage ?: 0} mg",
                                iconColor = Color(0xFF7B1FA2)
                            )

                            Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                            InfoItem(
                                icon = Icons.Default.Inventory,
                                label = "Total Quantity",
                                value = "${medicine?.quantity ?: 0}",
                                iconColor = Color(0xFF00695C)
                            )

                            Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                            InfoItem(
                                icon = Icons.Default.CalendarToday,
                                label = "Expiry Date",
                                value = formatDate(medicine?.expiredDate),
                                valueColor = getExpiryColor(medicine?.expiredDate),
                                iconColor = getExpiryColor(medicine?.expiredDate),
                                subtitle = getExpiryStatus(medicine?.expiredDate)
                            )

                            Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                            InfoItem(
                                icon = Icons.Default.Restaurant,
                                label = "When to Take",
                                value = if (medicine?.afterEat == true) "After Eating 🍽️" else "Before Eating ⏰",
                                valueColor = if (medicine?.afterEat == true) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                iconColor = if (medicine?.afterEat == true) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )

                            Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                            InfoItem(
                                icon = Icons.Default.Star,
                                label = "Priority",
                                value = "${(medicine?.priority ?: 0f).toInt()} / 5 ⭐",
                                valueColor = getPriorityColor(medicine?.priority ?: 0f),
                                iconColor = getPriorityColor(medicine?.priority ?: 0f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===================== REMINDER & CREATED =====================
                if (medicine?.remark?.isNotEmpty() == true ||
                    medicine?.reminderTime?.isNotEmpty() == true ||
                    medicine?.timesPerDay != null) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(16.dp),
                                clip = false
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "⏰ Reminder Settings",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A2E),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            medicine?.reminderTime?.takeIf { it.isNotEmpty() }?.let { time ->
                                InfoItem(
                                    icon = Icons.Default.Alarm,
                                    label = "First Reminder",
                                    value = time,
                                    iconColor = Color(0xFFE65100)
                                )
                            }

                            medicine?.timesPerDay?.let { times ->
                                if (medicine.reminderTime?.isNotEmpty() == true) {
                                    Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                                }
                                InfoItem(
                                    icon = Icons.Default.Repeat,
                                    label = "Times Per Day",
                                    value = "$times time(s)",
                                    iconColor = Color(0xFF1565C0)
                                )
                            }

                            medicine?.remark?.takeIf { it.isNotEmpty() }?.let { remark ->
                                if (medicine.reminderTime?.isNotEmpty() == true || medicine.timesPerDay != null) {
                                    Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                                }
                                InfoItem(
                                    icon = Icons.Default.Note,
                                    label = "Remark",
                                    value = remark,
                                    iconColor = Color(0xFF4E342E),
                                    multiline = true
                                )
                            }

                            Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                            InfoItem(
                                icon = Icons.Default.DateRange,
                                label = "Created Date",
                                value = formatDate(medicine?.createDate),
                                iconColor = Color(0xFF455A64)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ===================== BUTTONS =====================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onEditClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onBackClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF696969),
                            contentColor = Color(0xFFFFFFFF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Back",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ===================== INFO ITEM COMPONENT =====================

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1A1A2E),
    iconColor: Color = Color(0xFF1976D2),
    subtitle: String? = null,
    multiline: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = value,
                fontSize = if (multiline) 14.sp else 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
                maxLines = if (multiline) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis
            )

            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 11.sp,
                    color = valueColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}