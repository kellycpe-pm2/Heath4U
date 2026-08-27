package com.example.healt4u.screen.Admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AppBlue = Color(0xFF3779EE)
private val ScreenBlue = Color(0xFFE6F8FC)

@Composable
fun AdminDashboardScreen(
    onDoctorsClick: () -> Unit = {},
    onHospitalsClick: () -> Unit = {},
    onSubscriptionClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize().background(ScreenBlue)
    ) {
        AdminHeader(onBack = onBack)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Admin Dashboard",
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101820)
                )
                Text(
                    text = "Manage your health service at a glance",
                    modifier = Modifier.padding(start = 20.dp, top = 2.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF61717D)
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
            }

            item {
                DashboardCard(
                    title = "Doctor Account Management",
                    subtitle = "Create, delete, and view all doctors",
                    icon = Icons.Default.PersonAdd,
                    iconBg = Color(0xFFE3F2FD),
                    onClick = onDoctorsClick
                )
            }

            item {
                DashboardCard(
                    title = "Hospital Management",
                    subtitle = "Add, remove hospitals and link doctors",
                    icon = Icons.Default.Business,
                    iconBg = Color(0xFFE8F5E9),
                    onClick = onHospitalsClick
                )
            }

            item {
                DashboardCard(
                    title = "Subscription Management",
                    subtitle = "View subscriptions and total revenue",
                    icon = Icons.Default.CardMembership,
                    iconBg = Color(0xFFF3E5F5),
                    onClick = onSubscriptionClick
                )
            }
        }

        AdminBottomNavigation(
            onHomeClick = { },
            onDoctorsClick = onDoctorsClick,
            onSettingsClick = onSettingsClick
        )
    }
}

@Composable
private fun AdminHeader(onBack: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        onBack?.let {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(AppBlue, RoundedCornerShape(12.dp))
                    .clickable { it() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("HEALTH4U", color = AppBlue, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
            Text("ADMIN PORTAL", color = Color(0xFF63727D), fontSize = 9.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(iconBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AppBlue, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF101820))
                Spacer(Modifier.height(4.dp))
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF61717D))
            }
        }
    }
}

@Composable
private fun AdminBottomNavigation(
    onHomeClick: () -> Unit,
    onDoctorsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBlue)
            .padding(vertical = 10.dp, horizontal = 36.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BottomItem("Home", Icons.Default.Home, true, onHomeClick)
        BottomItem("Doctors", Icons.Default.PersonAdd, false, onDoctorsClick)
        BottomItem("Settings", Icons.Default.Settings, false, onSettingsClick)
    }
}

@Composable
private fun BottomItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, label, tint = if (selected) Color.White else Color(0xFFCFE0FF), modifier = Modifier.size(23.dp))
        Text(label.uppercase(), color = if (selected) Color.White else Color(0xFFCFE0FF), fontSize = 7.sp, fontWeight = FontWeight.Bold)
    }
}
