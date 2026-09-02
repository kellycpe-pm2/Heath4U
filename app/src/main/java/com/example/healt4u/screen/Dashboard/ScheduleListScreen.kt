package com.example.healt4u.screen.Dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healt4u.ViewModel.ReminderViewModel
import com.example.healt4u.model.MedicineAlert
import com.example.healt4u.model.ReminderLog
import com.example.healt4u.screen.componentUI.Theme.colorTheme

@Composable
fun ScheduleListScreen(
    vm: ReminderViewModel = viewModel(),
    patientId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val schedule by vm.todaySchedule.collectAsStateWithLifecycle()
    val medicineAlerts by vm.medicineAlerts.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.loadTodaySchedule(context, patientId)
    }

    val (taken, total) = vm.adherenceCount()
    val missedCount = schedule.count { it.status == "MISSED" }

    colorTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ===== Top bar =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = "Today's Schedule",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // ===== Adherence summary =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        text = "Adherence : $taken / $total",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                if (missedCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = "Missed",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$missedCount missed — family notified",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ===== Stock / expiry alert banner =====
            if (medicineAlerts.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
                        medicineAlerts.forEach { alert ->
                            Text(
                                text = "• ${alert.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (isLoading && schedule.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            } else if (schedule.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No medicines added yet",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Add a medicine to build today's schedule",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(schedule.sortedBy { it.time }) { log ->
                        ScheduleRow(
                            log = log,
                            onMarkTaken = { vm.markTaken(context, log) },
                            onMarkMissed = { vm.markMissed(context, log) }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ScheduleRow(
    log: ReminderLog,
    onMarkTaken: () -> Unit,
    onMarkMissed: () -> Unit
) {
    val statusColor = when (log.status) {
        "TAKEN" -> Color(0xFF4CAF50)
        "MISSED" -> Color.Red
        else -> MaterialTheme.colorScheme.secondary
    }
    val typeLabel = when (log.type) {
        "APPOINTMENT" -> "Appointment"
        else -> "Medicine"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$typeLabel · ${log.time}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = log.medicineName,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (log.status) {
                        "TAKEN" -> "Taken"
                        "MISSED" -> "Missed — family notified"
                        else -> "Pending"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }

            if (log.status == "PENDING" || log.status == "MISSED") {
                IconButton(onClick = onMarkTaken) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Mark taken",
                        tint = Color(0xFF4CAF50)
                    )
                }
            }
            if (log.status == "PENDING") {
                IconButton(onClick = onMarkMissed) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Mark missed",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}
