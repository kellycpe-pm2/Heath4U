package com.example.healt4u.screen.FamilyMode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healt4u.ViewModel.FamilyModeViewModel
import com.example.healt4u.model.CaregiverLink
import com.example.healt4u.model.FamilyAlert

private val ListScreenBlue = Color(0xFFE6F8FC)
private val ListAppBlue = Color(0xFF3779EE)
private val ListResolvedGreen = Color(0xFF4CAF50)

@Composable
fun AllResolvedAlertsScreen(
    vm: FamilyModeViewModel,
    currentUserId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val alerts by vm.alerts.collectAsStateWithLifecycle()
    val caregiverAlerts by vm.caregiverAlerts.collectAsStateWithLifecycle()

    LaunchedEffect(currentUserId) {
        vm.loadAlerts(context)
        vm.loadCaregiverAlerts(currentUserId)
    }

    val patientAlerts = alerts.filter { it.patientUserId == currentUserId }
    val resolvedAlerts = (patientAlerts + caregiverAlerts.filter { it.status == "RESOLVED" })
        .distinctBy { it.id }
        .sortedByDescending { it.resolvedAt ?: 0L }

    FamilyListScreenLayout(
        title = "Recent Resolved",
        onBack = onBack
    ) {
        if (resolvedAlerts.isEmpty()) {
            item { Text("No resolved alerts", modifier = Modifier.padding(20.dp), color = Color.Gray) }
        } else {
            items(resolvedAlerts, key = { it.id }) { alert ->
                ResolvedListCard(alert)
            }
        }
    }
}

@Composable
fun AllPatientsScreen(
    vm: FamilyModeViewModel,
    currentUserId: Int,
    onBack: () -> Unit
) {
    val myPatients by vm.myPatients.collectAsStateWithLifecycle()
    val caregiverAlerts by vm.caregiverAlerts.collectAsStateWithLifecycle()

    LaunchedEffect(currentUserId) {
        vm.refreshMyPatients(currentUserId)
        vm.loadCaregiverAlerts(currentUserId)
    }

    val pendingCaregiverAlerts = caregiverAlerts.filter { it.status == "PENDING" }

    FamilyListScreenLayout(
        title = "My Patients",
        onBack = onBack
    ) {
        if (myPatients.isEmpty()) {
            item { Text("No patients linked", modifier = Modifier.padding(20.dp), color = Color.Gray) }
        } else {
            items(myPatients, key = { it.id }) { patient ->
                PatientListCard(
                    patient = patient,
                    pendingCount = pendingCaregiverAlerts.count { it.patientUserId == patient.patientUserId }
                )
            }
        }
    }
}

@Composable
private fun FamilyListScreenLayout(
    title: String,
    onBack: () -> Unit,
    content: LazyListScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(ListScreenBlue)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(ListAppBlue).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ResolvedListCard(alert: FamilyAlert) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, null, tint = ListResolvedGreen, modifier = Modifier.size(28.dp))
            Column(Modifier.padding(start = 12.dp)) {
                Text(alert.medicineName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Resolved — ${alert.caregiverName}", color = ListResolvedGreen, fontSize = 13.sp)
                Text("Scheduled ${alert.scheduledTime}", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PatientListCard(patient: CaregiverLink, pendingCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Groups, null, tint = ListResolvedGreen, modifier = Modifier.size(30.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(patient.patientName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(patient.patientPhone, color = Color.Gray, fontSize = 13.sp)
            }
            if (pendingCount > 0) {
                Text("$pendingCount pending", color = Color(0xFFD32F2F), fontSize = 12.sp)
            }
        }
    }
}
