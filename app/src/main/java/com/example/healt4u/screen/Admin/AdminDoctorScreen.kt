package com.example.healt4u.screen.Admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healt4u.ViewModel.AdminManagementViewModel
import com.example.healt4u.model.Doctor
import com.example.healt4u.screen.componentUI.TextFieldInput
import com.example.healt4u.screen.componentUI.button

@Composable
fun AdminDoctorScreen(vm: AdminManagementViewModel, onBack: () -> Unit) {
    val doctors by vm.doctors.collectAsStateWithLifecycle()
    val hospitals by vm.hospitals.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    var showAddForm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadAll() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Doctor management", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        button(
            text = if (showAddForm) "Hide form" else "+ Add doctor",
            onClick = { showAddForm = !showAddForm },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        if (showAddForm) {
            AddDoctorForm(vm = vm, hospitalIds = hospitals.map { it.id to it.name })
        }

        Spacer(Modifier.padding(top = 8.dp))
        Text("All doctors (${doctors.size})", fontWeight = FontWeight.Bold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            items(doctors, key = { it.id }) { doctor ->
                DoctorCard(
                    doctor = doctor,
                    onApprove = { vm.approveDoctor(doctor.id) },
                    onReject = { vm.rejectDoctor(doctor.id) },
                    onDelete = { vm.removeDoctor(doctor.id) }
                )
            }
        }
    }
}

@Composable
private fun AddDoctorForm(vm: AdminManagementViewModel, hospitalIds: List<Pair<Int, String>>) {
    var name by remember { mutableStateOf("") }
    var ic by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }

    val error by vm.error.collectAsStateWithLifecycle()

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.padding(12.dp)) {
            TextFieldInput(name, { name = it }, "Full name", Modifier.fillMaxWidth(), false, singleLine = true)
            TextFieldInput(ic, { ic = it }, "IC number", Modifier.fillMaxWidth(), false, singleLine = true)
            TextFieldInput(phone, { phone = it }, "Phone", Modifier.fillMaxWidth(), false, singleLine = true)
            TextFieldInput(email, { email = it }, "Email", Modifier.fillMaxWidth(), false, singleLine = true)
            TextFieldInput(specialization, { specialization = it }, "Specialization", Modifier.fillMaxWidth(), false, singleLine = true)

            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

            // Simplified: uses first hospital in list.
            // Swap for dropDownMenu (already in componentUI) once you want a picker.
            button(
                text = "Submit doctor",
                onClick = {
                    vm.addDoctor(
                        name = name, ic = ic, phone = phone, email = email,
                        specialization = specialization,
                        hospitalId = hospitalIds.firstOrNull()?.first
                    )
                    name = ""; ic = ""; phone = ""; email = ""; specialization = ""
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun DoctorCard(
    doctor: Doctor,
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

            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (doctor.verificationStatus == "pending") {
                    button(text = "Approve", onClick = onApprove, modifier = Modifier.weight(1f))
                    button(text = "Reject", onClick = onReject, modifier = Modifier.weight(1f))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                }
            }
        }
    }
}