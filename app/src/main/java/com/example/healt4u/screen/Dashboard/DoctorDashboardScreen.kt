package com.example.healt4u.screen.Dashboard

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.getConversationsByDoctor
import com.example.healt4u.Storage.getPatientById
import com.example.healt4u.model.Conversation
import com.example.healt4u.screen.componentUI.AppSnackbarHost
import com.example.healt4u.screen.componentUI.Theme.colorTheme

@SuppressLint("ConfigurationScreenWidthHeight")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DoctorDashboardScreen(
    currentDoctorId: Int = 2,
    onPatientClick: (patientId: Int, conversation: Conversation) -> Unit = { _, _ -> },
    onMedicineClick: () -> Unit = {},
    onListClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onChangeStatus: (String) -> Unit = {},
    onStatisticClick: () -> Unit = {},
    onSettingClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val scheduleCardMinHeight = (screenHeightDp * 0.45f).dp
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedStatus by remember { mutableStateOf("AVAILABLE") }

    var patients by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var isLoadingPatients by remember { mutableStateOf(true) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentDoctorId, reloadKey) {
        try {
            isLoadingPatients = true
            patients = getConversationsByDoctor(currentDoctorId)
        } catch (e: Exception) {
            Log.e("Dashboard", "Load patients failed", e)
        } finally {
            isLoadingPatients = false
        }
    }

    colorTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HEALTH4U",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = { onProfileClick() }) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .padding(10.dp)
                            .heightIn(min = scheduleCardMinHeight)
                            .clickable { onListClick() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 20.sp
                            )
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(30.dp),
                                color = MaterialTheme.colorScheme.secondary
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Patient List",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))

                            when {
                                isLoadingPatients -> {
                                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), Alignment.Center) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                                patients.isEmpty() -> {
                                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "No patients yet",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    LazyColumn(
                                        modifier = Modifier.heightIn(max = 280.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(patients, key = { it.patientId }) { conversation ->
                                            DashboardPatientItem(
                                                conversation = conversation,
                                                onClick = {
                                                    onPatientClick(conversation.patientId, conversation)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                    ) {
                        Column(modifier = Modifier.padding(15.dp)) {
                            Text(
                                text = "Availability Status",
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 20.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(Modifier.height(10.dp))
                            val statusPairs = listOf(
                                "AVAILABLE" to Color(0xFF4CAF50),
                                "BUSY" to Color(0xFFFF5722),
                                "OFFLINE" to Color(0xFF9E9E9E)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                statusPairs.forEach { (status, statusColor) ->
                                    Row(
                                        modifier = Modifier.selectable(
                                            selected = selectedStatus == status,
                                            onClick = { selectedStatus = status; onChangeStatus(status) },
                                            role = Role.RadioButton
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedStatus == status,
                                            onClick = { selectedStatus = status; onChangeStatus(status) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = statusColor,
                                                unselectedColor = statusColor
                                            )
                                        )
                                        Text(text = status, color = statusColor, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardTile(
                            label = "Doctor-Patient Chat",
                            icon = Icons.Filled.Message,
                            modifier = Modifier.weight(1f),
                            onClick = onChatClick
                        )
                        DashboardTile(
                            label = "Patient List",
                            icon = Icons.Filled.PersonalInjury,
                            modifier = Modifier.weight(1f),
                            onClick = onListClick
                        )
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onPrimary)
                    .padding(top = 10.dp, bottom = 22.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomNavItem("Home", Icons.Filled.Home, true, {})
                BottomNavItem("Chat", Icons.Filled.Message, false, onChatClick)
                Spacer(Modifier.width(56.dp))
                BottomNavItem("Statistic", Icons.Filled.QueryStats, false, onStatisticClick)
                BottomNavItem("Settings", Icons.Filled.Settings, false, onSettingClick)
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 38.dp)
                    .size(56.dp)
                    .clickable { onScanClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.QrCodeScanner,
                        contentDescription = "Scan",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DashboardPatientItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val patientId = conversation.patientId
    var realPatientName by remember { mutableStateOf(conversation.patientName ?: "") }

    LaunchedEffect(patientId) {
        getPatientById(patientId)?.name?.let { realPatientName = it }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = realPatientName
                        .split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .take(2)
                        .joinToString(""),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.width(14.dp))

            Text(
                text = realPatientName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            Icon(
                Icons.Filled.ChevronRight,
                "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardTile(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, fontSize = 14.sp)
        }
    }
}

@Composable
private fun BottomNavItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(icon, contentDescription = label, tint = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Text(text = label, fontSize = 10.sp, color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewDoctorDashboard() {
    colorTheme {
        DoctorDashboardScreen()
    }
}