package com.example.healt4u.screen.Adherence

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healt4u.ViewModel.ReminderViewModel
import com.example.healt4u.model.ReminderLog
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdherenceStatisticScreen(
    onBack: () -> Unit,
    vm: ReminderViewModel = viewModel()
) {
    val context = LocalContext.current
    val fullSchedule by vm.todaySchedule.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf(getTodayDate()) }

    LaunchedEffect(Unit) {
        vm.loadTodaySchedule(context)
    }

    LaunchedEffect(selectedDate) {
        vm.loadTodaySchedule(context)
    }

    val medicineLogs = fullSchedule.filter { it.type != "APPOINTMENT" }
    val stats = calculateStats(medicineLogs)
    val dateFormat = SimpleDateFormat("dd MMM yyyy", LocalLocale.current.platformLocale)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Adherence Statistics",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 ${dateFormat.format(parseDate(selectedDate))}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row {
                        IconButton(
                            onClick = { selectedDate = adjustDate(selectedDate, -1) }
                        ) { Text("◀", fontSize = 20.sp) }
                        IconButton(
                            onClick = { selectedDate = adjustDate(selectedDate, 1) }
                        ) { Text("▶", fontSize = 20.sp) }
                    }
                }

                Spacer(Modifier.height(16.dp))
                AdherenceStatsCard(stats)
                Spacer(Modifier.height(16.dp))

                if (medicineLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💊", fontSize = 48.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No medicine records for this patient",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        "Dose Logs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(medicineLogs, key = { it.id }) { log ->
                            ReminderLogItem(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdherenceStatsCard(stats: AdherenceStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Adherence Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Total", stats.total.toString(), MaterialTheme.colorScheme.secondary)
                StatItem("Taken", stats.taken.toString(), Color(0xFF4CAF50))
                StatItem("Missed", stats.missed.toString(), Color(0xFFF44336))
                StatItem("Pending", stats.pending.toString(), Color(0xFFFF9800))
            }
            Spacer(Modifier.height(12.dp))

            val rate = if (stats.total > 0) (stats.taken.toFloat() / stats.total) * 100 else 0f
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Adherence Rate", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${rate.toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rate >= 80) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = rate / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (rate >= 80) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ReminderLogItem(log: ReminderLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (log.status) {
                "TAKEN" -> Color(0xFFE8F5E9)
                "MISSED" -> Color(0xFFFFEBEE)
                else -> Color(0xFFFFF3E0)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (log.status) {
                    "TAKEN" -> Icons.Filled.CheckCircle
                    "MISSED" -> Icons.Filled.Cancel
                    else -> Icons.Filled.Pending
                },
                null,
                tint = when (log.status) {
                    "TAKEN" -> Color(0xFF4CAF50)
                    "MISSED" -> Color(0xFFF44336)
                    else -> Color(0xFFFF9800)
                },
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(log.medicineName, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🕐 ${log.time}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        when (log.status) {
                            "TAKEN" -> "✅ Taken"
                            "MISSED" -> "❌ Missed"
                            else -> "⏳ Pending"
                        },
                        fontSize = 12.sp,
                        color = when (log.status) {
                            "TAKEN" -> Color(0xFF4CAF50)
                            "MISSED" -> Color(0xFFF44336)
                            else -> Color(0xFFFF9800)
                        }
                    )
                }
            }
        }
    }
}

data class AdherenceStats(val total: Int, val taken: Int, val missed: Int, val pending: Int)

private fun calculateStats(logs: List<ReminderLog>): AdherenceStats {
    val total = logs.size
    val taken = logs.count { it.status == "TAKEN" }
    val missed = logs.count { it.status == "MISSED" }
    val pending = logs.count { it.status != "TAKEN" && it.status != "MISSED" }
    return AdherenceStats(total, taken, missed, pending)
}

private fun getTodayDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

private fun parseDate(date: String): Date = try {
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date) ?: Date()
} catch (e: Exception) {
    Date()
}

private fun adjustDate(current: String, days: Int): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = fmt.parse(current) ?: Date()
    val cal = Calendar.getInstance()
    cal.time = date
    cal.add(Calendar.DAY_OF_MONTH, days)
    return fmt.format(cal.time)
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewAdherenceScreen() {
    colorTheme {
        AdherenceStatisticScreen(onBack = {})
    }
}