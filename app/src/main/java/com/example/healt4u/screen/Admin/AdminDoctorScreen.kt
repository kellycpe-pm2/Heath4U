package com.example.healt4u.screen.Admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healt4u.ViewModel.AdminManagementViewModel
import com.example.healt4u.model.Doctor

private val AppBlue = Color(0xFF3779EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDoctorScreen(
    vm: AdminManagementViewModel,
    onBack: () -> Unit,
    onAddDoctor: () -> Unit = {}
) {
    val doctors by vm.doctors.collectAsStateWithLifecycle()
    val hospitals by vm.hospitals.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.loadAll() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Doctor Management",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF101820))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDoctor,
                containerColor = AppBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Doctor")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                "All doctors (${doctors.size})",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(doctors, key = { it.id }) { doctor ->
                    DoctorCard(
                        doctor = doctor,
                        hospitalName = hospitals.find { it.id == doctor.hospitalId }?.name ?: "Unassigned",
                        onApprove = { vm.approveDoctor(doctor.id) },
                        onReject = { vm.rejectDoctor(doctor.id) },
                        onDelete = { vm.removeDoctor(doctor.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DoctorCard(
    doctor: Doctor,
    hospitalName: String,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (doctor.verificationStatus) {
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFE53935)
        else -> Color(0xFFFF9800)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(doctor.name, fontWeight = FontWeight.Bold)
                    Text(doctor.specialization, fontSize = 12.sp)
                    Text("Hospital: $hospitalName", fontSize = 11.sp, color = Color(0xFF61717D))
                    Text(doctor.phone, fontSize = 12.sp)
                }
                Surface(color = statusColor.copy(alpha = 0.15f)) {
                    Text(
                        doctor.verificationStatus.uppercase(),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (doctor.verificationStatus == "pending") {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Approve", color = Color.White)
                    }
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Reject", color = Color.White)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                }
            }
        }
    }
}
