package com.example.healt4u.screen.DoctorPatientChat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healt4u.ViewModel.HospitalViewModel
import com.example.healt4u.model.Doctor
import com.example.healt4u.model.Hospital
import com.example.healt4u.screen.componentUI.Theme.colorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorListScreen(
    hospital: Hospital,
    onDoctorSelected: (Doctor) -> Unit,
    onBack: () -> Unit,
    getDoctorStatus: (Int) -> String, // "available" / "busy" / "offline"
    viewModel: HospitalViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var showBusyConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(hospital.id) {
        viewModel.selectHospital(hospital)
    }

    val doctors by viewModel.doctors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val filteredDoctors = remember(searchQuery, doctors) {
        if (searchQuery.isBlank()) {
            doctors
        } else {
            doctors.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.specialization.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Select Doctor",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .padding(end = 48.dp)
                        )
                        Text(
                            text = hospital.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .padding(end = 48.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search doctor or specialty...") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            if (searchQuery.isNotBlank() || filteredDoctors.isNotEmpty()) {
                Text(
                    text = "${filteredDoctors.size} doctor${if (filteredDoctors.size != 1) "s" else ""} found",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                }

                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = errorMessage ?: "Something went wrong", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadDoctorsForHospital(hospital.id) }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                filteredDoctors.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No doctors found" else "No doctors available",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (searchQuery.isNotBlank()) {
                                Text(
                                    text = "Try a different search term",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredDoctors, key = { it.id }) { doctor ->
                            DoctorCard(
                                doctor = doctor,
                                status = getDoctorStatus(doctor.id),
                                onClick = {
                                    val status = getDoctorStatus(doctor.id)
                                    when (status.lowercase()) {
                                        "offline" -> { /* Blocked — card handles it */ }
                                        "busy" -> {
                                            selectedDoctor = doctor
                                            showBusyConfirmDialog = true
                                        }
                                        "available" -> {
                                            onDoctorSelected(doctor)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBusyConfirmDialog && selectedDoctor != null) {
        AlertDialog(
            title = { Text("Doctor is busy") },
            text = {
                Text(
                    "${selectedDoctor!!.name} is currently busy.\n\n" +
                            "You may still proceed, but response may be delayed.\n\n" +
                            "Continue to payment?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBusyConfirmDialog = false
                        onDoctorSelected(selectedDoctor!!)
                        selectedDoctor = null
                    }
                ) {
                    Text("Continue", color = MaterialTheme.colorScheme.secondary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBusyConfirmDialog = false
                        selectedDoctor = null
                    }
                ) {
                    Text("Cancel")
                }
            },
            onDismissRequest = {
                showBusyConfirmDialog = false
                selectedDoctor = null
            }
        )
    }
}

@Composable
fun DoctorCard(
    doctor: Doctor,
    status: String,
    onClick: () -> Unit
) {
    val safeStatus = status.lowercase()
    val statusColor = when (safeStatus) {
        "available" -> Color(0xFF4CAF50)
        "busy" -> Color(0xFFFF5722)
        "offline" -> Color(0xFF9E9E9E)
        else -> Color(0xFF9E9E9E)
    }
    val isClickable = safeStatus != "offline"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isClickable) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isClickable)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isClickable) 2.dp else 0.dp),
        border = if (!isClickable)
            BorderStroke(1.dp, Color(0xFFE0E0E0))
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = doctor.name.take(2).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isClickable)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                }
                // Status dot on avatar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = doctor.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isClickable)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusBadge(status = doctor.verificationStatus)
                        // Live Status Badge — all lowercase
                        Text(
                            text = safeStatus,
                            fontSize = 10.sp,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = doctor.specialization,
                    fontSize = 14.sp,
                    color = if (isClickable)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = doctor.email,
                    fontSize = 12.sp,
                    color = if (isClickable)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    maxLines = 1
                )
                Text(
                    text = "RM %.2f".format(doctor.consultationFee),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isClickable)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )
            }

            if (isClickable) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Select",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "unavailable",
                    fontSize = 11.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val safeStatus = status.lowercase()
    val (color, text) = when (safeStatus) {
        "verified" -> Color(0xFF4CAF50) to "verified"
        "pending" -> Color(0xFFFF9800) to "pending"
        "rejected" -> Color(0xFFF44336) to "rejected"
        else -> Color(0xFF9E9E9E) to "unknown"
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = text, fontSize = 10.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDoctorListScreen() {
    colorTheme {
        val sampleHospital = Hospital(
            id = 1,
            name = "Penang General Hospital",
            address = "Jalan Residensi, 10450 George Town, Pulau Pinang",
            phone = "04-2225333"
        )

        // Simulate different statuses — all lowercase
        val doctorStatuses = mapOf(
            1 to "available",
            2 to "busy",
            3 to "offline"
        )

        DoctorListScreen(
            hospital = sampleHospital,
            getDoctorStatus = { id -> doctorStatuses[id] ?: "offline" },
            onDoctorSelected = {},
            onBack = {}
        )
    }
}