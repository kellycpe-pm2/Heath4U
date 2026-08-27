package com.example.healt4u.screen.Admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
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
import com.example.healt4u.model.Hospital
import com.example.healt4u.screen.componentUI.TextFieldInput
import com.example.healt4u.screen.componentUI.button

private val AppBlue = Color(0xFF3779EE)

@Composable
fun AdminHospitalScreen(vm: AdminManagementViewModel, onBack: () -> Unit) {
    val hospitals by vm.hospitals.collectAsStateWithLifecycle()
    val doctors by vm.doctors.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showLinkDialog by remember { mutableStateOf(false) }
    var selectedHospital by remember { mutableStateOf<Hospital?>(null) }

    LaunchedEffect(Unit) { vm.loadAll() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Hospital management", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.padding(top = 12.dp))
        Text("Add new hospital", fontWeight = FontWeight.Bold)

        TextFieldInput(name, { name = it }, "Hospital name", Modifier.fillMaxWidth(), false, singleLine = true)
        TextFieldInput(address, { address = it }, "Address", Modifier.fillMaxWidth(), false, singleLine = true)
        TextFieldInput(phone, { phone = it }, "Phone", Modifier.fillMaxWidth(), false, singleLine = true)

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }

        button(
            text = if (isLoading) "Adding..." else "Add hospital",
            onClick = {
                vm.addHospital(name, address, phone)
                name = ""; address = ""; phone = ""
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Spacer(Modifier.padding(top = 16.dp))
        Text("Registered hospitals (${hospitals.size})", fontWeight = FontWeight.Bold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            items(hospitals, key = { hospital -> hospital.id }) { hospital ->
                val linkedDoctors = doctors.filter { doctor -> doctor.hospitalId == hospital.id }
                HospitalCard(
                    hospital = hospital,
                    linkedDoctors = linkedDoctors,
                    onDelete = { vm.removeHospital(hospital.id) },
                    onLinkDoctor = {
                        selectedHospital = hospital
                        showLinkDialog = true
                    }
                )
            }
        }
    }

    if (showLinkDialog && selectedHospital != null) {
        LinkDoctorDialog(
            hospital = selectedHospital!!,
            doctors = doctors.filter { doctor -> doctor.hospitalId != selectedHospital!!.id },
            onDismiss = { showLinkDialog = false },
            onLink = { doctor ->
                vm.linkDoctorToHospital(doctor.id, selectedHospital!!.id)
                showLinkDialog = false
            }
        )
    }
}

@Composable
private fun HospitalCard(
    hospital: Hospital,
    linkedDoctors: List<Doctor>,
    onDelete: () -> Unit,
    onLinkDoctor: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(hospital.name, fontWeight = FontWeight.Bold)
                    Text(hospital.address, fontSize = 12.sp)
                    Text(hospital.phone, fontSize = 12.sp)
                    if (linkedDoctors.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text("${linkedDoctors.size} doctor(s) linked", fontSize = 11.sp, color = AppBlue)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                button(
                    text = "Link doctor",
                    onClick = onLinkDoctor,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                }
            }
        }
    }
}

@Composable
private fun LinkDoctorDialog(
    hospital: Hospital,
    doctors: List<Doctor>,
    onDismiss: () -> Unit,
    onLink: (Doctor) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Doctor to ${hospital.name}") },
        text = {
            if (doctors.isEmpty()) {
                Text("No available doctors to link.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(doctors) { doctor ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onLink(doctor) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Link, null, tint = AppBlue, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(doctor.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(doctor.specialization, fontSize = 12.sp, color = Color(0xFF61717D))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
