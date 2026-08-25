package com.example.healt4u.screen.Admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private val SettingsBlue = Color(0xFF3779EE)

@Composable
fun AdminSettingsScreen(onBack: () -> Unit) {
    var pushNotifications by remember { mutableStateOf(true) }
    var lowStockAlerts by remember { mutableStateOf(true) }
    var expiryAlerts by remember { mutableStateOf(true) }
    var autoSync by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.padding(top = 12.dp))
        Text("Notifications", fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(Modifier.padding(12.dp)) {
                SettingToggle("Push notifications", "Receive general app alerts", pushNotifications) { pushNotifications = it }
                SettingToggle("Low stock alerts", "Warn when medicine stock is 5 or below", lowStockAlerts) { lowStockAlerts = it }
                SettingToggle("Expiry alerts", "Warn when medicines are near expiry", expiryAlerts) { expiryAlerts = it }
            }
        }

        Spacer(Modifier.padding(top = 16.dp))
        Text("Data", fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(Modifier.padding(12.dp)) {
                SettingToggle("Auto cloud sync", "Sync inventory with server automatically", autoSync) { autoSync = it }
            }
        }

        Spacer(Modifier.padding(top = 16.dp))
        Text("About", fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("HEALTH4U Admin Portal", fontWeight = FontWeight.Bold)
                Text("Version 1.0.0", fontSize = 12.sp, color = Color(0xFF61717D))
                Text(
                    "Manage hospitals, doctors and medicine inventory for your health service.",
                    fontSize = 12.sp,
                    color = Color(0xFF61717D),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Text(
            "Notification and sync preferences are stored locally on this device.",
            fontSize = 11.sp,
            color = Color(0xFF61717D),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun SettingToggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF61717D))
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = SettingsBlue)
        )
    }
}