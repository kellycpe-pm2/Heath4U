package com.example.healt4u.screen.Admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.healt4u.screen.componentUI.button

private val specializations = listOf(
    "Cardiologist", "Neurologist", "Orthopedic", "Pediatrician",
    "Dermatologist", "Surgeon", "Nephrologist", "Gynecologist",
    "Oncologist", "Infectious Disease", "General Medicine",
    "Psychiatrist", "Ophthalmologist", "ENT Specialist"
)

private val AppBlue = Color(0xFF3779EE)

@Composable
fun AdminDoctorScreen(vm: AdminManagementViewModel, onBack: () -> Unit) {
    val doctors by vm.doctors.collectAsStateWithLifecycle()
    val hospitals by vm.hospitals.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    var showAddForm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadAll() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Doctor management", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        button(
            text = if (showAddForm) "Hide form" else "+ Add doctor",
            onClick = { showAddForm = !showAddForm },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (showAddForm) {
            AddDoctorForm(
                vm = vm,
                hospitals = hospitals,
                isLoading = isLoading,
                onDoctorAdded = { showAddForm = false }
            )
        }

        Spacer(Modifier.padding(top = 8.dp))
        Text("All doctors (${doctors.size})", fontWeight = FontWeight.Bold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDoctorForm(
    vm: AdminManagementViewModel,
    hospitals: List<com.example.healt4u.model.Hospital>,
    isLoading: Boolean,
    onDoctorAdded: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ic by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedSpecialization by remember { mutableStateOf("") }
    var selectedHospitalId by remember { mutableStateOf<Int?>(null) }

    var specExpanded by remember { mutableStateOf(false) }
    var hospitalExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = ic,
                onValueChange = { ic = it },
                label = { Text("IC number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Hospital dropdown - OutlinedTextField style
            Text("Hospital", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            ExposedDropdownMenuBox(
                expanded = hospitalExpanded,
                onExpandedChange = { hospitalExpanded = it }
            ) {
                OutlinedTextField(
                    value = hospitals.find { it.id == selectedHospitalId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select hospital") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hospitalExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                ExposedDropdownMenu(
                    expanded = hospitalExpanded,
                    onDismissRequest = { hospitalExpanded = false }
                ) {
                    hospitals.forEach { hospital ->
                        DropdownMenuItem(
                            text = { Text(hospital.name, color = Color.Black) },
                            onClick = {
                                selectedHospitalId = hospital.id
                                hospitalExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Specialization dropdown - OutlinedTextField style
            Text("Specialization", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            ExposedDropdownMenuBox(
                expanded = specExpanded,
                onExpandedChange = { specExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedSpecialization,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select specialization") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = specExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                ExposedDropdownMenu(
                    expanded = specExpanded,
                    onDismissRequest = { specExpanded = false }
                ) {
                    specializations.forEach { spec ->
                        DropdownMenuItem(
                            text = { Text(spec, color = Color.Black) },
                            onClick = {
                                selectedSpecialization = spec
                                specExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            button(
                text = if (isLoading) "Adding..." else "Submit doctor",
                onClick = {
                    vm.addDoctor(
                        name = name, ic = ic, phone = phone, email = email,
                        specialization = selectedSpecialization,
                        hospitalId = selectedHospitalId
                    )
                    name = ""; ic = ""; phone = ""; email = ""
                    selectedSpecialization = ""; selectedHospitalId = null
                    onDoctorAdded()
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
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
