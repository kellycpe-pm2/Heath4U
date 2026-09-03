package com.example.healt4u.screen.Dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.getDoctorById
import com.example.healt4u.model.Doctor
import com.example.healt4u.screen.componentUI.AppSnackbarHost
import kotlinx.coroutines.launch

private val AppBlue = Color(0xFF3779EE)

@Composable
fun DoctorSettingScreen(
    doctorId: Int,
    onBack: () -> Unit,
    onSwitchAccount: () -> Unit,
    onLogout: () -> Unit
) {
    var showProfile by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF101820))
            }
            Text("Doctor Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Text(
            "Account Management",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF61717D)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Doctor Profile",
                    subtitle = "View your account details",
                    onClick = { showProfile = true }
                )
            }
        }

        Text(
            "About & Support",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF61717D)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "HEALTH4U v1.0.0",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    icon = Icons.Default.SupportAgent,
                    title = "Support",
                    subtitle = "health4u.support@email.com",
                    onClick = {}
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onSwitchAccount,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppBlue)
        ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Switch Account", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
        ) {
            Text("Logout", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showProfile) {
        DoctorProfileScreen(
            doctorId = doctorId,
            onBack = { showProfile = false }
        )
    }
}

@Composable
private fun DoctorProfileScreen(
    doctorId: Int,
    onBack: () -> Unit
) {
    var doctor by remember { mutableStateOf<Doctor?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(doctorId) {
        isLoading = true
        doctor = getDoctorById(doctorId)
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF101820))
                }
                Text("Doctor Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppBlue)
                }
            } else if (doctor == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Profile not found")
                }
            } else {
                val d = doctor!!
                Column(modifier = Modifier.padding(20.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ProfileInfoItem("Name", d.name)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            ProfileInfoItem("Specialization", d.specialization)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            ProfileInfoItem("Consultation Fee", "RM ${d.consultationFee}")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            ProfileInfoItem("Status", d.verificationStatus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = Color(0xFF61717D))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF101820))
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(AppBlue.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AppBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF101820))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF61717D))
        }
    }
}
