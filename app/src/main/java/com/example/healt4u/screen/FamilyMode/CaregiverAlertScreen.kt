package com.example.healt4u.screen.FamilyMode

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healt4u.ViewModel.FamilyModeViewModel
import com.example.healt4u.model.FamilyAlert
import com.example.healt4u.screen.componentUI.button

private val AppBlue = Color(0xFF3779EE)
private val AlertOrange = Color(0xFFFFA33A)
private val AlertRed = Color(0xFFD32F2F)
private val ResolvedGreen = Color(0xFF4CAF50)

@Composable
fun CaregiverAlertScreen(
    vm: FamilyModeViewModel,
    onBack: () -> Unit = {}
) {
    val alerts by vm.alerts.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.loadAlerts(context)
    }

    val pendingAlerts = alerts.filter { it.status == "PENDING" }
    val calledAlerts = alerts.filter { it.status == "CALLED" }
    val resolvedAlerts = alerts.filter { it.status == "RESOLVED" }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F6FA))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF101820))
            }
            Box(
                modifier = Modifier.size(38.dp).background(AlertOrange, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text("Missed Dose Alerts", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF101820))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            if (pendingAlerts.isEmpty() && calledAlerts.isEmpty() && resolvedAlerts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = ResolvedGreen, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No alerts", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("The patient has not missed any doses.", fontSize = 12.sp, color = Color(0xFF61717D))
                        }
                    }
                }
            }

            if (pendingAlerts.isNotEmpty()) {
                item {
                    Text("Active Alerts (${pendingAlerts.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AlertRed)
                }
                items(pendingAlerts, key = { it.id }) { alert ->
                    CaregiverAlertCard(
                        alert = alert,
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${alert.patientPhone}")
                            }
                            context.startActivity(intent)
                            vm.markAlertCalled(context, alert)
                        },
                        onMessage = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:${alert.patientPhone}")
                                putExtra("sms_body", "Hi, I noticed you missed your ${alert.medicineName} dose at ${alert.scheduledTime}. Please confirm you've taken it.")
                            }
                            context.startActivity(intent)
                            vm.markAlertCalled(context, alert)
                        },
                        onResolve = {
                            vm.resolveAlert(context, alert)
                        }
                    )
                }
            }

            if (calledAlerts.isNotEmpty()) {
                item {
                    Text("Awaiting Confirmation (${calledAlerts.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AlertOrange)
                }
                items(calledAlerts, key = { it.id }) { alert ->
                    CalledAlertCard(
                        alert = alert,
                        onResolve = { vm.resolveAlert(context, alert) }
                    )
                }
            }

            if (resolvedAlerts.isNotEmpty()) {
                item {
                    Text("Resolved", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ResolvedGreen)
                }
                items(resolvedAlerts.take(5), key = { it.id }) { alert ->
                    ResolvedAlertCard(alert)
                }
            }
        }
    }
}

@Composable
private fun CaregiverAlertCard(
    alert: FamilyAlert,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onResolve: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFFFF0DD), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, null, tint = AlertOrange, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(alert.medicineName, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Scheduled at ${alert.scheduledTime}", fontSize = 12.sp, color = Color(0xFF61717D))
                    Text("Patient phone: ${alert.patientPhone}", fontSize = 11.sp, color = Color(0xFF90A4AE))
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Call button - direct dial
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppBlue),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Call", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Message button - SMS
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ResolvedGreen),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Message, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Message", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            button(
                text = "Mark Resolved",
                onClick = onResolve,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CalledAlertCard(
    alert: FamilyAlert,
    onResolve: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(Color(0xFFFFF0DD), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Call, null, tint = AlertOrange, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(alert.medicineName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Called — awaiting patient confirmation", fontSize = 10.sp, color = AlertOrange)
            }
            button(text = "Confirm", onClick = onResolve, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun ResolvedAlertCard(alert: FamilyAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = ResolvedGreen, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(alert.medicineName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Resolved", fontSize = 10.sp, color = ResolvedGreen)
            }
        }
    }
}
