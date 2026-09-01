package com.example.healt4u.screen.ScanScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.model.NPRAMedicine
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    onBack: () -> Unit,
    onSave: () -> Unit ={}
) {
    var selectedTime by remember { mutableStateOf("08:00") }
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }
    var note by remember { mutableStateOf("") }
    var repeatDays by remember { mutableStateOf(setOf<String>()) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加提醒", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            onSave(
                            )
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("保存", color = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("💊 药品", fontSize = 14.sp)
                        Text(
                            "",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showTimePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Column {
                                Text("时间", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(selectedTime, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("重复提醒", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        val days = listOf("一", "二", "三", "四", "五", "六", "日")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            days.forEach { day ->
                                val isSelected = day in repeatDays
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        repeatDays = if (isSelected) repeatDays - day else repeatDays + day
                                    },
                                    label = { Text(day) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF1A237E),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注 (可选)") },
                    placeholder = { Text("例如: 饭后服用") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = selectedTime,
            onConfirm = { selectedTime = it; showTimePicker = false },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun TimePickerDialog(
    initialTime: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var hour by remember { mutableStateOf(initialTime.substringBefore(":").toIntOrNull() ?: 8) }
    var minute by remember { mutableStateOf(initialTime.substringAfter(":").toIntOrNull() ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { if (hour < 23) hour++ }) {
                            Icon(Icons.Default.KeyboardArrowUp, "增加")
                        }
                        Text(String.format("%02d", hour), fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { if (hour > 0) hour-- }) {
                            Icon(Icons.Default.KeyboardArrowDown, "减少")
                        }
                    }
                    Text(":", fontSize = 48.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { if (minute < 59) minute++ }) {
                            Icon(Icons.Default.KeyboardArrowUp, "增加")
                        }
                        Text(String.format("%02d", minute), fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { if (minute > 0) minute-- }) {
                            Icon(Icons.Default.KeyboardArrowDown, "减少")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(String.format("%02d:%02d", hour, minute)) }) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

fun getCurrentDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}