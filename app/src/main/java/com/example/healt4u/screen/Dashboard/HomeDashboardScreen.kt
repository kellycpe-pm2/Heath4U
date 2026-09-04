package com.example.healt4u.screen.Dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healt4u.ViewModel.FamilyModeViewModel
import com.example.healt4u.ViewModel.ReminderViewModel
import com.example.healt4u.model.ReminderLog
import com.example.healt4u.screen.componentUI.AppLogo
import com.example.healt4u.screen.componentUI.AppSnackbarHost
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import kotlinx.coroutines.launch

@Composable
fun HomeDashboardScreen(
    vm: ReminderViewModel = viewModel(),
    vmFamily: FamilyModeViewModel? = null,
    patientId: Int,
    onMedicineClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onFamilyModeClick: () -> Unit = {},
    onUnbuiltModuleClick: (String) -> Unit = {},
    onChatClick: () -> Unit,
    onAppointmentClick: () -> Unit,
    onAdherenceClick: () -> Unit,
    onScanClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val scheduleCardMinHeight = (screenHeightDp * 0.35f).dp
    val schedule by vm.todaySchedule.collectAsStateWithLifecycle()
    val medicineAlerts by vm.medicineAlerts.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        vm.loadTodaySchedule(context, patientId)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        vm.loadTodaySchedule(context, patientId)
    }

    LaunchedEffect(schedule) {
        if (schedule.isNotEmpty()) {
            vmFamily?.checkOverdueAndCreateAlerts(context, patientId)
        }
    }

    val (taken, total) = vm.adherenceCount()
    val missed = schedule.filter { it.status == "MISSED" && it.medicineId != -1 }
    val previewItems = schedule.sortedBy { it.time }
    val adherencePercentage = if (total > 0) (taken.toFloat() / total * 100).toInt() else 0

    fun notImplemented(name: String) {
        onUnbuiltModuleClick(name)
        scope.launch { snackbarHostState.showSnackbar("$name — built by another team member") }
    }

    colorTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE9FCFF),
                                Color(0xFFD4F0FF)
                            )
                        )
                    )
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AppLogo(modifier = Modifier.size(42.dp))
                            Text(
                                text = "Health4U",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = Color.White
                                ),
                                color = Color.White
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onSettingsClick,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.Person,
                                            contentDescription = "Profile",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                            }
                        }
                    }
                }

                // ===== Main Content with LazyColumn (Scrollable) =====
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFE9FCFF),
                                            Color(0xFFD4F0FF)
                                        )
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Good ${getTimeOfDay()}!",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Here's your health overview",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Favorite,
                                        contentDescription = "Health",
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }


                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Today's Adherence",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "$taken of $total doses taken",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = if (total > 0) taken.toFloat() / total else 0f,
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = if (adherencePercentage >= 80) Color(0xFF4CAF50)
                                        else if (adherencePercentage >= 50) Color(0xFFFFA726)
                                        else Color(0xFFEF5350),
                                        trackColor = Color(0xFFF5F5F5)
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = if (adherencePercentage >= 80) Color(0xFFE8F5E9)
                                    else if (adherencePercentage >= 50) Color(0xFFFFF3E0)
                                    else Color(0xFFFFEBEE),
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$adherencePercentage%",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (adherencePercentage >= 80) Color(0xFF4CAF50)
                                            else if (adherencePercentage >= 50) Color(0xFFFFA726)
                                            else Color(0xFFEF5350)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ===== Missed dose alert banner =====
                    if (missed.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                ),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFFCDD2),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Filled.Warning,
                                                contentDescription = "Missed dose",
                                                tint = Color(0xFFC62828),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${missed.size} Missed Dose${if (missed.size > 1) "s" else ""}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC62828),
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Family group has been notified",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ===== Stock / expiry alert banner =====
                    if (medicineAlerts.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF3E0)
                                ),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFFFE0B2),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Filled.Inventory2,
                                                    contentDescription = "Stock alert",
                                                    tint = Color(0xFFE65100),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${medicineAlerts.size} Medicine Alert${if (medicineAlerts.size > 1) "s" else ""}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100),
                                            fontSize = 13.sp
                                        )
                                    }
                                    medicineAlerts.take(3).forEach { alert ->
                                        Row(
                                            modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(Color(0xFFE65100), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = alert.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF5D4037),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ===== Today, Schedule card =====
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = scheduleCardMinHeight)
                                .clickable { onScheduleClick() },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Today's Schedule",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "$taken / $total",
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(10.dp))

                                if (isLoading && schedule.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                } else if (previewItems.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Outlined.MedicalServices,
                                                contentDescription = null,
                                                tint = Color(0xFFBDBDBD),
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "No medicines scheduled",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "Add medicine to start your schedule",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                } else {
                                    previewItems.take(4).forEachIndexed { index, log ->
                                        ScheduleRowCompact(log)
                                        if (index != minOf(3, previewItems.lastIndex)) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                color = Color(0xFFF5F5F5)
                                            )
                                        }
                                    }
                                    if (previewItems.size > 4) {
                                        Text(
                                            text = "+${previewItems.size - 4} more • View full schedule →",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "View full schedule →",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ===== Quick access tiles =====
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardTile(
                                label = "Medicine",
                                icon = Icons.Filled.MedicalServices,
                                iconColor = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f),
                                onClick = onMedicineClick
                            )
                            DashboardTile(
                                label = "Schedule",
                                icon = Icons.Filled.CalendarMonth,
                                iconColor = Color(0xFF2196F3),
                                modifier = Modifier.weight(1f),
                                onClick = onScheduleClick
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardTile(
                                label = "Chat",
                                icon = Icons.Filled.Chat,
                                iconColor = Color(0xFF9C27B0),
                                modifier = Modifier.weight(1f),
                                onClick = onChatClick
                            )
                            DashboardTile(
                                label = "Appointment",
                                icon = Icons.Filled.Event,
                                iconColor = Color(0xFFFF5722),
                                modifier = Modifier.weight(1f),
                                onClick = onAppointmentClick
                            )
                        }
                    }

                    // ===== Additional Quick Action =====
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardTile(
                                label = "Adherence",
                                icon = Icons.Filled.QueryStats,
                                iconColor = Color(0xFF00BCD4),
                                modifier = Modifier.weight(1f),
                                onClick = onAdherenceClick
                            )
                            DashboardTile(
                                label = "Family",
                                icon = Icons.Filled.Groups,
                                iconColor = Color(0xFFFF6B6B),
                                modifier = Modifier.weight(1f),
                                onClick = onFamilyModeClick
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }

            // ===== Enhanced Bottom Navigation with Scan Icon =====
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        label = "Home",
                        icon = Icons.Filled.Home,
                        selected = true,
                        onClick = {}
                    )
                    BottomNavItem(
                        label = "Family",
                        icon = Icons.Filled.Groups,
                        selected = false,
                        onClick = onFamilyModeClick
                    )
                    // Scan button in the middle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onScanClick() }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.QrCodeScanner,
                                    contentDescription = "Scan",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Text(
                            text = "Scan",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    BottomNavItem(
                        label = "Stats",
                        icon = Icons.Filled.QueryStats,
                        selected = false,
                        onClick = onAdherenceClick
                    )
                    BottomNavItem(
                        label = "Settings",
                        icon = Icons.Filled.Settings,
                        selected = false,
                        onClick = onSettingsClick
                    )
                }
            }

            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )
        }
    }
}

@Composable
private fun ScheduleRowCompact(log: ReminderLog) {
    val statusIcon = when (log.status) {
        "TAKEN" -> Icons.Filled.CheckCircle
        "MISSED" -> Icons.Filled.Warning
        else -> Icons.Filled.Circle
    }

    val statusTint = when (log.status) {
        "TAKEN" -> Color(0xFF4CAF50)
        "MISSED" -> Color(0xFFEF5350)
        else -> MaterialTheme.colorScheme.secondary
    }

    val bgColor = when (log.status) {
        "TAKEN" -> Color(0xFFE8F5E9)
        "MISSED" -> Color(0xFFFFEBEE)
        else -> Color.Transparent
    }

    val typeLabel = when (log.type) {
        "APPOINTMENT" -> "Appointment"
        else -> "Medicine"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (bgColor != Color.Transparent) bgColor else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = statusTint.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    statusIcon,
                    contentDescription = log.status,
                    tint = statusTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.time,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = typeLabel,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = log.medicineName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 15.sp
            )
        }
        if (log.status == "TAKEN") {
            Text(
                text = "✓ Done",
                fontSize = 12.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DashboardTile(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else Color.Transparent,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun getTimeOfDay(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Morning"
        in 12..16 -> "Afternoon"
        in 17..20 -> "Evening"
        else -> "Night"
    }
}