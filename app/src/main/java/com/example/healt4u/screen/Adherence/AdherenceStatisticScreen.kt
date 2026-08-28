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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.model.ReminderLog
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.Storage.getReminderLogsForDate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault
import androidx.compose.ui.platform.LocalLocale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdherenceStatisticScreen(
    patientId: Int,
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf(getTodayDate()) }
    var logs by remember { mutableStateOf<List<ReminderLog>>(emptyList()) }
    var stats by remember { mutableStateOf<AdherenceStats?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("dd MMM yyyy", LocalLocale.current.platformLocale)

    fun loadData() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = getReminderLogsForDate(selectedDate)
                logs = result
                stats = calculateStats(result)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load data"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedDate) {
        loadData()
    }

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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading adherence data...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Something went wrong",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadData() }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
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
                            text = "📅 ${dateFormat.format(Date(parseDate(selectedDate)))}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    val date = parseDate(selectedDate)
                                    val cal = Calendar.getInstance().apply { time = Date(date) }
                                    cal.add(Calendar.DAY_OF_YEAR, -1)
                                    selectedDate = formatDate(cal.timeInMillis)
                                }
                            ) {
                                Text("◀", fontSize = 20.sp)
                            }
                            IconButton(
                                onClick = {
                                    selectedDate = getTodayDate()
                                }
                            ) {
                                Text("📅", fontSize = 16.sp)
                            }
                            IconButton(
                                onClick = {
                                    val date = parseDate(selectedDate)
                                    val cal = Calendar.getInstance().apply { time = Date(date) }
                                    cal.add(Calendar.DAY_OF_YEAR, 1)
                                    if (cal.timeInMillis <= System.currentTimeMillis()) {
                                        selectedDate = formatDate(cal.timeInMillis)
                                    }
                                }
                            ) {
                                Text("▶", fontSize = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    stats?.let {
                        AdherenceStatsCard(stats = it)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (logs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "💊",
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No reminder logs for this date",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Check back later for adherence data",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Dose Logs",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = logs,
                                key = { it.id }
                            ) { log ->
                                ReminderLogItem(log = log)
                            }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Adherence Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total",
                    value = stats.total.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    label = "Taken",
                    value = stats.taken.toString(),
                    color = Color(0xFF4CAF50)
                )
                StatItem(
                    label = "Missed",
                    value = stats.missed.toString(),
                    color = Color(0xFFF44336)
                )
                StatItem(
                    label = "Pending",
                    value = stats.pending.toString(),
                    color = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ========== Progress Bar ==========
            val adherenceRate = if (stats.total > 0) {
                (stats.taken.toFloat() / stats.total) * 100
            } else 0f

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Adherence Rate",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${adherenceRate.toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (adherenceRate >= 80) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = adherenceRate / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (adherenceRate >= 80) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
            // Status Icon
            Icon(
                imageVector = when (log.status) {
                    "TAKEN" -> Icons.Filled.CheckCircle
                    "MISSED" -> Icons.Filled.Cancel
                    else -> Icons.Filled.Pending
                },
                contentDescription = log.status,
                tint = when (log.status) {
                    "TAKEN" -> Color(0xFF4CAF50)
                    "MISSED" -> Color(0xFFF44336)
                    else -> Color(0xFFFF9800)
                },
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.medicineName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🕐 ${log.time}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (log.status == "TAKEN") {
                        Text(
                            text = "✅ Taken",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    } else if (log.status == "MISSED") {
                        Text(
                            text = "❌ Missed",
                            fontSize = 12.sp,
                            color = Color(0xFFF44336)
                        )
                    } else {
                        Text(
                            text = "⏳ Pending",
                            fontSize = 12.sp,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }
        }
    }
}

// ========== Data Classes ==========
data class AdherenceStats(
    val total: Int,
    val taken: Int,
    val missed: Int,
    val pending: Int
)

// ========== Helper Functions ==========
private fun calculateStats(logs: List<ReminderLog>): AdherenceStats {
    val total = logs.size
    val taken = logs.count { it.status == "TAKEN" }
    val missed = logs.count { it.status == "MISSED" }
    val pending = logs.count { it.status == "PENDING" }
    return AdherenceStats(total, taken, missed, pending)
}

private fun getTodayDate(): String {
    val format = SimpleDateFormat("yyyy-MM-dd", getDefault())
    return format.format(Date())
}

private fun parseDate(date: String): Long {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", getDefault())
        format.parse(date)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("yyyy-MM-dd", getDefault())
    return format.format(Date(timestamp))
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewAdherenceStatisticScreen() {
    colorTheme {
        AdherenceStatisticScreen(
            patientId = 1,
            onBack = {}
        )
    }
}