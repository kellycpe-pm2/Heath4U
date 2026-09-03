package com.example.healt4u.screen.Dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healt4u.ViewModel.FamilyModeViewModel
import com.example.healt4u.ViewModel.ReminderViewModel
import com.example.healt4u.model.MedicineAlert
import com.example.healt4u.model.ReminderLog
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
    onChatClick:()-> Unit,
    onAppointmentClick:()-> Unit,
    onAdherenceClick:()-> Unit,
    onScanClick :()-> Unit,
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val scheduleCardMinHeight = (screenHeightDp * 0.45f).dp
    val schedule by vm.todaySchedule.collectAsStateWithLifecycle()
    val medicineAlerts by vm.medicineAlerts.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        vm.loadTodaySchedule(context, patientId)
    }

    LaunchedEffect(schedule){
        if(schedule.isNotEmpty()){
            vmFamily?.checkOverdueAndCreateAlerts(context, patientId)
        }
    }

    val (taken, total) = vm.adherenceCount()
    // Appointments (medicineId == -1) aren't "missed doses" — exclude them here
    val missed = schedule.filter { it.status == "MISSED" && it.medicineId != -1 }
    val previewItems = schedule.sortedBy { it.time }

    fun notImplemented(name: String) {
        onUnbuiltModuleClick(name)
        scope.launch { snackbarHostState.showSnackbar("$name — built by another team member") }
    }

    colorTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // ===== Top bar =====
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HEALTH4U",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ===== Missed dose alert banner =====
                    if (missed.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = "Missed dose",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "${missed.size} missed dose${if (missed.size > 1) "s" else ""} today — family group notified",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
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
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Inventory2,
                                            contentDescription = "Stock alert",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = "${medicineAlerts.size} medicine alert${if (medicineAlerts.size > 1) "s" else ""}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    medicineAlerts.take(3).forEach { alert ->
                                        Text(
                                            text = "• ${alert.message}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
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
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Today, Schedule",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.secondary
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Medication Adherence : $taken / $total",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))

                                if (isLoading && schedule.isEmpty()) {
                                    Text(
                                        text = "Loading today's schedule...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                } else if (previewItems.isEmpty()) {
                                    Text(
                                        text = "No medicines added yet — add one to start your schedule.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                } else {
                                    previewItems.forEachIndexed { index, log ->
                                        ScheduleRowCompact(log)
                                        if (index != previewItems.lastIndex) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "View full schedule →",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(top = 10.dp)
                                    )
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
                                modifier = Modifier.weight(1f),
                                onClick = onMedicineClick
                            )
                            DashboardTile(
                                label = "Schedule",
                                icon = Icons.Filled.CalendarMonth,
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
                                label = "Doctor-Patient Chat",
                                icon = Icons.Filled.Groups,
                                modifier = Modifier.weight(1f),
                                onClick = { onChatClick() }
                            )
                            DashboardTile(
                                label = "Appointment",
                                icon = Icons.Filled.Event,
                                modifier = Modifier.weight(1f),
                                onClick = { onAppointmentClick() }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(70.dp)) }
                }
            }

            // ===== Bottom navigation =====
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onPrimary)
                    .padding(top = 10.dp, bottom = 22.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "Home",
                    icon = Icons.Filled.CheckCircle,
                    selected = true,
                    onClick = {}
                )
                BottomNavItem(
                    label = "Family Mode",
                    icon = Icons.Filled.Groups,
                    selected = false,
                    onClick = onFamilyModeClick
                )
                Spacer(modifier = Modifier.width(56.dp))
                BottomNavItem(
                    label = "Statistic",
                    icon = Icons.Filled.QueryStats,
                    selected = false,
                    onClick = { onAdherenceClick() }
                )
                BottomNavItem(
                    label = "Settings",
                    icon = Icons.Filled.Settings,
                    selected = false,
                    onClick = onSettingsClick
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 38.dp)
                    .size(56.dp)
                    .clickable { onScanClick() }
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

            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
            )
        }
    }
}

@Composable
private fun ScheduleRowCompact(log: ReminderLog) {
    val (icon, tint) = when (log.status) {
        "TAKEN" -> Icons.Filled.CheckCircle to Color(0xFF4CAF50)
        "MISSED" -> Icons.Filled.Warning to Color.Red
        else -> Icons.Filled.Circle to MaterialTheme.colorScheme.secondary
    }
    val typeLabel = when (log.type) {
        "APPOINTMENT" -> "Appointment"
        else -> "Medicine"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = log.status, tint = tint, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$typeLabel · ${log.time}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = log.medicineName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DashboardTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
