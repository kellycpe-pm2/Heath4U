package com.example.healt4u.screen.Dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR2)
@Composable
fun DoctorDashboardScreen(
    onMedicineClick: () -> Unit = {},
    onListClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onChangeStatus: (String) -> Unit = {},
    onStatisticClick: () -> Unit = {},
    onSettingClick:()-> Unit ={},
    onScanClick: ()->Unit ={},
    onProfileClick : ()->Unit ={}
    
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val scheduleCardMinHeight = (screenHeightDp * 0.45f).dp
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedStatus by remember { mutableStateOf("AVAILABLE") }


    colorTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // ===== Top bar =====
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
                                text = "Today,",
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

                                    Row(
                                        horizontalArrangement = Arrangement.End
                                    ){
                                        Text("")
                                    }

                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            // Add patient list items here
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
                        border = BorderStroke(2.dp, color = MaterialTheme.colorScheme.secondary)
                    ) {
                        Column(
                            modifier = Modifier.padding(5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "AVAILABILITY STATUS",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontSize = 20.sp
                                )
                            }
                            val statusPairs = listOf(
                                "AVAILABLE" to Color(0xFF4CAF50),
                                "BUSY" to Color(0xFFFF5722),
                                "OFFLINE" to Color(0xFF9E9E9E)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                statusPairs.forEach { (status, statusColor) ->
                                    Row(
                                        modifier = Modifier.selectable(
                                            selected = (selectedStatus == status),
                                            onClick = { selectedStatus = status },
                                            role = Role.RadioButton
                                        ),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        RadioButton(
                                            selected = (selectedStatus == status),
                                            onClick = {
                                                selectedStatus = status
                                                onChangeStatus(status)
                                            },
                                            colors = RadioButtonColors(
                                                selectedColor = statusColor,
                                                unselectedColor = statusColor,
                                                disabledSelectedColor = MaterialTheme.colorScheme.onPrimary,
                                                disabledUnselectedColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                        Text(
                                            text = status,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = statusColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ===== Quick access tiles =====
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
                            onClick = onMedicineClick
                        )
                        DashboardTile(
                            label = "Patient List",
                            icon = Icons.Filled.PersonalInjury,
                            modifier = Modifier.weight(1f),
                            onClick = onListClick
                        )
                    }

                    Spacer(Modifier.height(80.dp)) // Space for bottom nav
                }
            }

            // ===== Bottom navigation =====
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onPrimary)
                    .padding(top = 10.dp, bottom = 22.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "Home",
                    icon = Icons.Filled.CheckCircle,
                    selected = true,
                    onClick = {}
                )
                BottomNavItem(
                    label = "Chat",
                    icon = Icons.Filled.Message,
                    selected = false,
                    onClick = onChatClick
                )
                Spacer(modifier = Modifier.width(56.dp))
                BottomNavItem(
                    label = "Statistic",
                    icon = Icons.Filled.QueryStats,
                    selected = false,
                    onClick = onStatisticClick
                )
                BottomNavItem(
                    label = "Settings",
                    icon = Icons.Filled.Settings,
                    selected = false,
                    onClick = { onSettingClick() }
                )
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

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            )
        }
    }
}

// ===== Helper Composables =====
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 14.sp
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
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected)
                MaterialTheme.colorScheme.secondary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected)
                MaterialTheme.colorScheme.secondary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ===== Preview =====
@Preview(showBackground = true)
@Composable
fun PreviewDoctorDashboard() {
    colorTheme {
        DoctorDashboardScreen(
        )
    }
}