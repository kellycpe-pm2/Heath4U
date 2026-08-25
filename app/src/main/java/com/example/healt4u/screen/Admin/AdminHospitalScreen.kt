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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healt4u.ViewModel.AdminManagementViewModel
import com.example.healt4u.screen.componentUI.TextFieldInput
import com.example.healt4u.screen.componentUI.button

@Composable
fun AdminHospitalScreen(vm: AdminManagementViewModel, onBack: () -> Unit) {
    val hospitals by vm.hospitals.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadAll() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Hospital management", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.padding(top = 12.dp))
        Text("Add new hospital", fontWeight = FontWeight.Bold)

        TextFieldInput(name, { name = it }, "Hospital name", Modifier.fillMaxWidth(), false, singleLine = true)
        TextFieldInput(address, { address = it }, "Address", Modifier.fillMaxWidth(), false, singleLine = true)
        TextFieldInput(phone, { phone = it }, "Phone", Modifier.fillMaxWidth(), false, singleLine = true)

        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

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
            items(hospitals, key = { it.id }) { hospital ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(hospital.name, fontWeight = FontWeight.Bold)
                            Text(hospital.address, fontSize = 12.sp)
                            Text(hospital.phone, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}