package com.example.healt4u.screen.Admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.ui.window.Dialog
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
import com.example.healt4u.model.Hospital
import com.example.healt4u.screen.componentUI.TextFieldInput
import com.example.healt4u.screen.componentUI.button

private val AppBlue = Color(0xFF3779EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHospitalScreen(vm: AdminManagementViewModel, onBack: () -> Unit) {
    val hospitals by vm.hospitals.collectAsStateWithLifecycle()
    val doctors by vm.doctors.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val success by vm.success.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showLinkDialog by remember { mutableStateOf(false) }
    var selectedHospital by remember { mutableStateOf<Hospital?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.loadAll() }

    LaunchedEffect(success) {
        success?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSuccess()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Hospital Management",
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Spacer(Modifier.padding(top = 4.dp))
            Text("Add new hospital", fontWeight = FontWeight.Bold)

            TextFieldInput(name, { name = it }, "Hospital name", Modifier.fillMaxWidth(), false, singleLine = true)
            TextFieldInput(address, { address = it }, "Address", Modifier.fillMaxWidth(), false, singleLine = true)
            TextFieldInput(phone, { phone = it }, "Phone", Modifier.fillMaxWidth(), false, singleLine = true)

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
    }

    if (showLinkDialog && selectedHospital != null) {
        LinkDoctorDialog(
            hospital = selectedHospital!!,
            doctors = doctors.filter { doctor -> doctor.hospitalId != selectedHospital!!.id },
            onDismiss = {
                showLinkDialog = false
                selectedHospital = null
            },
            onLink = { doctor ->
                vm.linkDoctorToHospital(doctor.id, selectedHospital!!.id)
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
    var linkedDoctor by remember { mutableStateOf<Doctor?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (linkedDoctor != null) {
                    // Success state
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "Success!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            "${linkedDoctor!!.name} has been linked to ${hospital.name}",
                            fontSize = 14.sp,
                            color = Color(0xFF61717D),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
                        ) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Doctor list state
                    Text("Link Doctor to ${hospital.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))

                    if (doctors.isEmpty()) {
                        Text("No available doctors to link.", color = Color(0xFF61717D))
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            doctors.forEach { doctor ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF0F4FF),
                                    onClick = {
                                        onLink(doctor)
                                        linkedDoctor = doctor
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Link, null, tint = AppBlue, modifier = Modifier.size(22.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(doctor.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                            Text(doctor.specialization, fontSize = 12.sp, color = Color(0xFF61717D))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
