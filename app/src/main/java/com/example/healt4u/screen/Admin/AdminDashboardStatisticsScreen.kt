package com.example.healt4u.screen.Admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.AdminDashboardStatistics
import com.example.healt4u.Storage.getAdminDashboardStatistics

private val StatisticsBlue = Color(0xFFE6F8FC)
private val StatisticsAppBlue = Color(0xFF3779EE)

@Composable
fun AdminDashboardStatisticsScreen(onBack: () -> Unit) {
    var statistics by remember { mutableStateOf(AdminDashboardStatistics()) }

    LaunchedEffect(Unit) {
        statistics = getAdminDashboardStatistics()
    }

    Column(Modifier.fillMaxSize().background(StatisticsBlue)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(StatisticsAppBlue).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text("Dashboard Statistics", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("System Overview", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF101820))
                Text("Monitor the Health4U system at a glance", fontSize = 13.sp, color = Color(0xFF61717D))
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatisticsCard("Patients", statistics.patients.toString(), Icons.Default.Groups, Modifier.weight(1f))
                    StatisticsCard("Caregiver Links", statistics.caregiverLinks.toString(), Icons.Default.Groups, Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatisticsCard("Doctors", statistics.doctors.toString(), Icons.Default.LocalHospital, Modifier.weight(1f))
                    StatisticsCard("Hospitals", statistics.hospitals.toString(), Icons.Default.Business, Modifier.weight(1f))
                }
            }
            item {
                StatisticsCard("Missed Doses Today", statistics.missedDosesToday.toString(), Icons.Default.Warning, Modifier.fillMaxWidth())
            }
            item {
                StatisticsCard("Unresolved Alerts", statistics.unresolvedAlerts.toString(), Icons.Default.Warning, Modifier.fillMaxWidth())
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatisticsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = StatisticsAppBlue, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(10.dp))
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = StatisticsAppBlue)
            Text(title, fontSize = 12.sp, color = Color(0xFF61717D))
        }
    }
}
