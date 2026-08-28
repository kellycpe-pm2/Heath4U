package com.example.healt4u.screen.FamilyMode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
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
import com.example.healt4u.model.CaregiverProfile
import com.example.healt4u.model.FamilyAlert
import com.example.healt4u.screen.componentUI.button

private val AppBlue = Color(0xFF3779EE)
private val ScreenBlue = Color(0xFFE6F8FC)
private val AlertOrange = Color(0xFFFFA33A)
private val AlertRed = Color(0xFFD32F2F)
private val ResolvedGreen = Color(0xFF4CAF50)

@Composable
fun FamilyModeScreen(
    vm: FamilyModeViewModel,
    onBack: () -> Unit = {},
    onAddCaregiverClick: () -> Unit = {},
    onSetPhoneClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val caregivers by vm.caregivers.collectAsStateWithLifecycle()
    val alerts by vm.alerts.collectAsStateWithLifecycle()
    val patientPhone by vm.patientPhone.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.loadPatientPhone(context)
        vm.refreshCaregivers(context)
        vm.loadAlerts(context)
        vm.checkOverdueAndCreateAlerts(context)
    }

    val pendingAlerts = alerts.filter { it.status == "PENDING" }
    val resolvedAlerts = alerts.filter { it.status == "RESOLVED" }

    Column(
        modifier = Modifier.fillMaxSize().background(ScreenBlue)
    ) {
        FamilyModeHeader(onBack = onBack)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Family Mode",
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101820)
                )
                Text(
                    text = "Caregivers will be alerted when doses are missed",
                    modifier = Modifier.padding(start = 20.dp, top = 2.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF61717D)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { onSetPhoneClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, null, tint = AppBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("My Phone Number", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = patientPhone.ifEmpty { "Tap to set your phone number" },
                            fontSize = 13.sp,
                            color = if (patientPhone.isEmpty()) AlertOrange else Color(0xFF101820)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("Caregivers", caregivers.size.toString(), AppBlue, Modifier.weight(1f))
                    StatCard("Pending", pendingAlerts.size.toString(), AlertOrange, Modifier.weight(1f))
                    StatCard("Resolved", resolvedAlerts.size.toString(), ResolvedGreen, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Linked Caregivers", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF101820))
                    Box(
                        modifier = Modifier.size(30.dp)
                            .background(AppBlue, RoundedCornerShape(10.dp))
                            .clickable { onAddCaregiverClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, "Add caregiver", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (caregivers.isEmpty()) {
                item {
                    EmptyCaregiverCard()
                }
            } else {
                items(caregivers, key = { it.id }) { caregiver ->
                    CaregiverCard(
                        caregiver = caregiver,
                        onRemove = { vm.removeCaregiver(context, caregiver.id) }
                    )
                }
            }

            if (pendingAlerts.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Alerts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF101820))
                        Text("${pendingAlerts.size} pending", fontSize = 12.sp, color = AlertOrange, fontWeight = FontWeight.SemiBold)
                    }
                }
                items(pendingAlerts, key = { it.id }) { alert ->
                    AlertCard(
                        alert = alert,
                        onResolve = { vm.resolveAlert(context, alert) }
                    )
                }
            }

            if (resolvedAlerts.isNotEmpty()) {
                item {
                    Text(
                        "Recent Resolved",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF101820)
                    )
                }
                items(resolvedAlerts.take(5), key = { it.id }) { alert ->
                    ResolvedAlertCard(alert)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun FamilyModeHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF101820))
        }
        Box(
            modifier = Modifier.size(38.dp).background(AppBlue, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Groups, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("HEALTH4U", color = AppBlue, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
            Text("FAMILY MODE", color = Color(0xFF63727D), fontSize = 9.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = color)
            Text(label, fontSize = 10.sp, color = Color(0xFF61717D))
        }
    }
}

@Composable
private fun CaregiverCard(caregiver: CaregiverProfile, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(38.dp).background(Color(0xFFE8F0FE), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Groups, null, tint = AppBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(caregiver.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${caregiver.relationship} — ${caregiver.phone}", fontSize = 11.sp, color = Color(0xFF61717D))
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, "Remove", tint = AlertRed, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun AlertCard(
    alert: FamilyAlert,
    onResolve: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(Color(0xFFFFF0DD), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, null, tint = AlertOrange, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(alert.medicineName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Scheduled ${alert.scheduledTime} — Notified ${alert.caregiverName}", fontSize = 10.sp, color = Color(0xFF61717D))
                }
            }

            Spacer(Modifier.padding(top = 10.dp))

            button(
                text = "Confirm Taken",
                onClick = onResolve,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ResolvedAlertCard(alert: FamilyAlert) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(30.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, null, tint = ResolvedGreen, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(alert.medicineName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Resolved — ${alert.caregiverName}", fontSize = 10.sp, color = ResolvedGreen)
            }
        }
    }
}

@Composable
private fun EmptyCaregiverCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Groups, null, tint = Color(0xFFB0BEC5), modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(8.dp))
            Text("No caregivers linked yet", color = Color(0xFF61717D), fontSize = 13.sp)
            Text("Add a family member who will be notified if you miss a dose.", fontSize = 11.sp, color = Color(0xFF90A4AE))
        }
    }
}
