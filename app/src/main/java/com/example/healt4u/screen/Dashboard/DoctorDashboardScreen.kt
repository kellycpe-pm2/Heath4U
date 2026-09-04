package com.example.healt4u.screen.Dashboard

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.getConversationsByDoctor
import com.example.healt4u.Storage.getDoctorById
import com.example.healt4u.Storage.getPatientById
import com.example.healt4u.model.Conversation
import com.example.healt4u.screen.componentUI.AppSnackbarHost
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import kotlinx.coroutines.launch

@SuppressLint("ConfigurationScreenWidthHeight")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DoctorDashboardScreen(
    doctorId: Int = 2,
    onPatientClick: (patientId: Int, conversation: Conversation) -> Unit = { _, _ -> },
    onMedicineClick: () -> Unit = {},
    onListClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onChangeStatus: (String) -> Unit = {},
    onStatisticClick: () -> Unit = {},
    onSettingClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val scheduleCardMinHeight = (screenHeightDp * 0.35f).dp
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedStatus by remember { mutableStateOf("loading") }

    LaunchedEffect(doctorId) {
        coroutineScope.launch {
            try {
                val doctor = getDoctorById(doctorId)
                val savedStatus = doctor?.status?.lowercase() ?: "available"
                selectedStatus = savedStatus
                Log.d("DashboardStatus", "Loaded doctor $doctorId → status=$savedStatus")
            } catch (e: Exception) {
                Log.e("DashboardStatus", "Failed to load status", e)
                selectedStatus = "available"
            }
        }
    }

    var patients by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var isLoadingPatients by remember { mutableStateOf(true) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(doctorId, reloadKey) {
        try {
            isLoadingPatients = true
            patients = getConversationsByDoctor(doctorId)
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
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE9FCFF),
                                Color(0xFFD4F0FF)
                            )
                        )
                    )
            ) {
                // ===== Top Bar =====
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Health4U",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = Color.White
                                ),
                                color = Color.White
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onSettingClick,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            IconButton(
                                onClick = onProfileClick,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.Person,
                                            contentDescription = "Profile",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ===== Main Content =====
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ===== Patient List Card =====
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Good ${getTimeOfDay()}!",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Welcome back, Doctor",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Favorite,
                                        contentDescription = "Health",
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = scheduleCardMinHeight)
                                .clickable { onListClick() },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.PersonalInjury,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = "My Patients",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "${patients.size} patients",
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))

                                when {
                                    isLoadingPatients -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(32.dp),
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                Text(
                                                    text = "Loading patients...",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    patients.isEmpty() -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    Icons.Outlined.PersonalInjury,
                                                    contentDescription = null,
                                                    tint = Color(0xFFBDBDBD),
                                                    modifier = Modifier.size(40.dp)
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                Text(
                                                    text = "No patients yet",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "Patients will appear here when assigned",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                                                DashboardDoctorItem(
                                                    conversation = conversation,
                                                    onClick = {
                                                        onPatientClick(conversation.patientId, conversation)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                if (patients.isNotEmpty() && patients.size > 3) {
                                    Text(
                                        text = "View all patients →",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .clickable { onListClick() }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
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
                                    "available" to Color(0xFF4CAF50),
                                    "busy" to Color(0xFFFF5722),
                                    "offline" to Color(0xFF9E9E9E)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    statusPairs.forEach { (statusValue, statusColor) ->
                                        Row(
                                            modifier = Modifier.selectable(
                                                selected = selectedStatus == statusValue,
                                                onClick = {
                                                    selectedStatus = statusValue
                                                    onChangeStatus(statusValue)
                                                },
                                                role = Role.RadioButton
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selectedStatus == statusValue,
                                                onClick = {
                                                    selectedStatus = statusValue
                                                    onChangeStatus(statusValue)
                                                },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = statusColor,
                                                    unselectedColor = statusColor.copy(alpha = 0.4f)
                                                )
                                            )
                                            Text(
                                                text = statusValue.uppercase(),
                                                color = statusColor,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ===== Quick Access Tiles =====
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardTile(
                                label = "Chat",
                                icon = Icons.Filled.Chat,
                                iconColor = Color(0xFF9C27B0),
                                modifier = Modifier.weight(1f),
                                onClick = onChatClick
                            )
                            DashboardTile(
                                label = "Patients",
                                icon = Icons.Filled.PersonalInjury,
                                iconColor = Color(0xFF2196F3),
                                modifier = Modifier.weight(1f),
                                onClick = onListClick
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardTile(
                                label = "Statistics",
                                icon = Icons.Filled.QueryStats,
                                iconColor = Color(0xFF00BCD4),
                                modifier = Modifier.weight(1f),
                                onClick = onStatisticClick
                            )
                            DashboardTile(
                                label = "Settings",
                                icon = Icons.Filled.Settings,
                                iconColor = Color(0xFFFF6B6B),
                                modifier = Modifier.weight(1f),
                                onClick = onSettingClick
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }

            // ===== Bottom Navigation =====
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        label = "Home",
                        icon = Icons.Filled.Home,
                        selected = true,
                        onClick = {}
                    )
                    BottomNavItem(
                        label = "Chat",
                        icon = Icons.Filled.Chat,
                        selected = false,
                        onClick = onChatClick
                    )
                    BottomNavItem(
                        label = "Stats",
                        icon = Icons.Filled.QueryStats,
                        selected = false,
                        onClick = onStatisticClick
                    )
                    BottomNavItem(
                        label = "Settings",
                        icon = Icons.Filled.Settings,
                        selected = false,
                        onClick = onSettingClick
                    )
                }
            }

            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )
        }
    }
}

// ===== Helper Composables =====

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DashboardDoctorItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val patientId = conversation.patientId
    var realPatientName by remember { mutableStateOf(conversation.patientName ?: "") }

    LaunchedEffect(patientId) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            getPatientById(patientId)?.name?.let { realPatientName = it }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = realPatientName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                conversation.lastMessage?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

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
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else Color.Transparent,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun getTimeOfDay(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Morning"
        in 12..16 -> "Afternoon"
        in 17..20 -> "Evening"
        else -> "Night"
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