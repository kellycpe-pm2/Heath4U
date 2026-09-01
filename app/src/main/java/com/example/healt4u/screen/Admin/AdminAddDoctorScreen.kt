package com.example.healt4u.screen.Admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import kotlinx.coroutines.launch

private val specializations = listOf(
    "Cardiologist", "Neurologist", "Orthopedic", "Pediatrician",
    "Dermatologist", "Surgeon", "Nephrologist", "Gynecologist",
    "Oncologist", "Infectious Disease", "General Medicine",
    "Psychiatrist", "Ophthalmologist", "ENT Specialist"
)

private val AppBlue = Color(0xFF3779EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddDoctorScreen(
    vm: AdminManagementViewModel,
    onBack: () -> Unit,
    onDoctorAdded: () -> Unit
) {
    val hospitals by vm.hospitals.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var ic by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedSpecialization by remember { mutableStateOf("") }
    var selectedHospitalId by remember { mutableStateOf<Int?>(null) }

    var specExpanded by remember { mutableStateOf(false) }
    var hospitalExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { vm.loadAll() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Doctor",
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = ic,
                onValueChange = { ic = it },
                label = { Text("IC Number") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text("Hospital", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
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
                    shape = RoundedCornerShape(12.dp),
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

            Spacer(Modifier.height(16.dp))

            Text("Specialization", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
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
                    shape = RoundedCornerShape(12.dp),
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

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please enter doctor name") }
                        return@Button
                    }
                    if (ic.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please enter IC number") }
                        return@Button
                    }
                    if (phone.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please enter phone number") }
                        return@Button
                    }
                    if (phone.length !in 10..11 || !phone.all { it.isDigit() }) {
                        scope.launch { snackbarHostState.showSnackbar("Phone number must be 10 to 11 digits only") }
                        return@Button
                    }
                    if (email.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please enter email") }
                        return@Button
                    }
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        scope.launch { snackbarHostState.showSnackbar("Invalid email format") }
                        return@Button
                    }
                    if (selectedSpecialization.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please select a specialization") }
                        return@Button
                    }
                    if (selectedHospitalId == null) {
                        scope.launch { snackbarHostState.showSnackbar("Please select a hospital") }
                        return@Button
                    }

                    vm.addDoctor(
                        name = name,
                        ic = ic,
                        phone = phone,
                        email = email,
                        specialization = selectedSpecialization,
                        hospitalId = selectedHospitalId
                    )
                    scope.launch { snackbarHostState.showSnackbar("Doctor added successfully") }
                    onDoctorAdded()
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "SUBMIT DOCTOR",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
