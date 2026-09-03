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

    val totalRevenue = remember(payments) {
        payments.filter { it.status.equals("completed", true) }.sumOf { it.amount }
    }
    val todayRevenue = remember(payments) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        payments.filter { it.status.equals("completed", true) && it.date == today }.sumOf { it.amount }
    }
    val monthRevenue = remember(payments) {
        val fmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val thisMonth = fmt.format(Date())
        payments.filter { it.status.equals("completed", true) && it.date.startsWith(thisMonth) }.sumOf { it.amount }
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
                                    color = MaterialTheme.colorScheme.secondary // ✅ THEME BLUE
                                )
                                Spacer(Modifier.height(20.dp))
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
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            p.status.uppercase(),
                                            fontSize = 11.sp,
                                            color = if (p.status.equals("completed", true))
                                                Color(0xFF2E7D32)
                                            else
                                                Color(0xFFEF6C00),
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

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Revenue Statistics", showSystemUi = true)
@Composable
fun PreviewRevenueStatisticScreen() {
    colorTheme {
        val samplePayments = listOf(
            Payment("p1", 1, 2, "Dr. Sarah Tan", 100.00, "2026-09-03", "10:30", "COMPLETED", "TnG"),
            Payment("p2", 2, 2, "Dr. Sarah Tan", 80.00, "2026-09-03", "14:00", "COMPLETED", "FPX")
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
                val total = samplePayments.sumOf { it.amount }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(24.dp)) {
                                Text("Total Revenue", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("RM %.2f".format(total), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.height(16.dp))
                                Divider(color = MaterialTheme.colorScheme.secondaryContainer)
                                Spacer(Modifier.height(16.dp))
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Today", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("RM 180.00", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Column {
                                        Text("This Month", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("RM 180.00", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Text("Payment History", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
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
                                    Text("RM %.2f".format(p.amount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Text(p.status, fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}