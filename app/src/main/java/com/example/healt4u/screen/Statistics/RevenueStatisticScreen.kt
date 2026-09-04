package com.example.healt4u.screen.Statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.getPaymentsByDoctor
import com.example.healt4u.model.Payment
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RevenueStatisticScreen(
    doctorId: String,
    onBack: () -> Unit
) {
    var payments by remember { mutableStateOf<List<Payment>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(doctorId) {
        payments = getPaymentsByDoctor(doctorId)
        loading = false
    }

    // ✅ ONLY COUNT COMPLETED — EXCLUDE REFUNDED
    val completedPayments = remember(payments) {
        payments.filter { it.status.equals("completed", ignoreCase = true) }
    }

    val totalRevenue = remember(completedPayments) {
        completedPayments.sumOf { it.amount }
    }

    val todayRevenue = remember(completedPayments) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        completedPayments.filter { it.date == today }.sumOf { it.amount }
    }

    val monthRevenue = remember(completedPayments) {
        val fmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val thisMonth = fmt.format(Date())
        completedPayments.filter { it.date.orEmpty().startsWith(thisMonth) }.sumOf { it.amount }
    }

    // ✅ COUNT REFUNDS
    val refundedPayments = remember(payments) {
        payments.filter { it.status.equals("refunded", ignoreCase = true) }
    }
    val totalRefunded = remember(refundedPayments) {
        refundedPayments.sumOf { it.amount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Revenue Statistics",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .padding(end = 48.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            if (loading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(24.dp)) {
                                Text(
                                    "Total Revenue",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "RM %.2f".format(totalRevenue),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.height(8.dp))
                                if (totalRefunded > 0) {
                                    Text(
                                        "Refunded: -RM %.2f".format(totalRefunded),
                                        fontSize = 13.sp,
                                        color = Color(0xFFE53935),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                Divider(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    thickness = 1.dp
                                )
                                Spacer(Modifier.height(16.dp))
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Today", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            "RM %.2f".format(todayRevenue),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Column {
                                        Text("This Month", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            "RM %.2f".format(monthRevenue),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "Payment History",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    if (payments.isEmpty()) {
                        item {
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                )
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    Alignment.Center
                                ) {
                                    Text(
                                        "No payments yet",
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(payments) { p ->
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    Arrangement.SpaceBetween,
                                    Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Patient #${p.patientId}",
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            "${p.date} · ${p.time}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            p.paymentMethod,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "RM %.2f".format(p.amount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = when {
                                                p.status.equals("refunded", ignoreCase = true) -> Color(0xFFE53935)
                                                else -> MaterialTheme.colorScheme.secondary
                                            }
                                        )
                                        Text(
                                            p.status.uppercase(),
                                            fontSize = 11.sp,
                                            color = when {
                                                p.status.equals("completed", ignoreCase = true) -> Color(0xFF2E7D32)
                                                p.status.equals("refunded", ignoreCase = true) -> Color(0xFFE53935)
                                                else -> Color(0xFFFF9800)
                                            },
                                            fontWeight = FontWeight.SemiBold
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
}

// --- PREVIEW ---
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Revenue Statistics", showSystemUi = true)
@Composable
fun PreviewRevenueStatisticScreen() {
    colorTheme {
        val samplePayments = listOf(
            Payment("p1", 1, 2, "Dr. Sarah Tan", 100.00, "2026-09-03", "10:30", "COMPLETED", "TnG"),
            Payment("p2", 2, 2, "Dr. Sarah Tan", 80.00, "2026-09-03", "14:00", "COMPLETED", "FPX"),
            Payment("p3", 3, 2, "Dr. Sarah Tan", 50.00, "2026-09-02", "09:15", "REFUNDED", "TnG")
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Revenue Statistics", color = Color.White, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp)
            ) {
                val completed = samplePayments.filter { it.status.equals("COMPLETED", ignoreCase = true) }
                val total = completed.sumOf { it.amount }
                val refunds = samplePayments.filter { it.status.equals("REFUNDED", ignoreCase = true) }.sumOf { it.amount }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(24.dp)) {
                                Text("Total Revenue", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("RM %.2f".format(total), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                if (refunds > 0) {
                                    Text("Refunded: -RM %.2f".format(refunds), fontSize = 13.sp, color = Color(0xFFE53935))
                                }
                            }
                        }
                    }
                    item {
                        Text("Payment History", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                    items(samplePayments) { p ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Column {
                                    Text("Patient #${p.patientId}", fontWeight = FontWeight.SemiBold)
                                    Text("${p.date} · ${p.time}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(p.paymentMethod, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                Column {
                                    Text(
                                        "RM %.2f".format(p.amount),
                                        fontWeight = FontWeight.Bold,
                                        color = if (p.status.equals("REFUNDED", true)) Color(0xFFE53935) else MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        p.status,
                                        fontSize = 11.sp,
                                        color = when {
                                            p.status.equals("COMPLETED", true) -> Color(0xFF2E7D32)
                                            p.status.equals("REFUNDED", true) -> Color(0xFFE53935)
                                            else -> Color(0xFFFF9800)
                                        },
                                        fontWeight = FontWeight.SemiBold
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