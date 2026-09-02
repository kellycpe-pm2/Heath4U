package com.example.healt4u.screen.Statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.getDoctorRevenueStats
import com.example.healt4u.Storage.getPaymentsByDoctor
import com.example.healt4u.model.Payment

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RevenueStatisticScreen(
    doctorId: String,
    onBack: () -> Unit
) {
    var stats by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var payments by remember { mutableStateOf<List<Payment>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(doctorId) {
        stats = getDoctorRevenueStats(doctorId)
        payments = getPaymentsByDoctor(doctorId)
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revenue Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Total Revenue", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "RM %.2f".format(stats["total"] ?: 0.0),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Column {
                                    Text("Today", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("RM %.2f".format(stats["today"] ?: 0.0), fontWeight = FontWeight.SemiBold)
                                }
                                Column {
                                    Text("This Month", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("RM %.2f".format(stats["month"] ?: 0.0), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Payment History", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                if (payments.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                            Text("No payments yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(payments) { p ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                Arrangement.SpaceBetween,
                                Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Patient #${p.patientId}", fontWeight = FontWeight.Medium)
                                    Text("${p.date} · ${p.time}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(p.paymentMethod, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "RM %.2f".format(p.amount),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        p.status.uppercase(),
                                        fontSize = 11.sp,
                                        color = if (p.status.equals("completed", true)) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}