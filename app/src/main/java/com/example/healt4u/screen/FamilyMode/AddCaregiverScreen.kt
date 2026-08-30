package com.example.healt4u.screen.FamilyMode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healt4u.Storage.getPatientByPhone
import com.example.healt4u.ViewModel.FamilyModeViewModel
import com.example.healt4u.model.PatientUser
import com.example.healt4u.screen.componentUI.TextFieldInput
import com.example.healt4u.screen.componentUI.button
import kotlinx.coroutines.launch

private val relationshipOptions = listOf(
    "Daughter", "Son", "Spouse", "Dad", "Mom",
    "Relative", "Siblings", "Friends", "Others"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCaregiverScreen(
    vm: FamilyModeViewModel,
    currentUserId: Int,
    currentUserName: String,
    currentUserPhone: String,
    onBack: () -> Unit = {},
    onAdded: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val addResult by vm.addCaregiverResult.collectAsStateWithLifecycle()
    var phone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var isLookingUp by remember { mutableStateOf(false) }
    var lookupResult by remember { mutableStateOf<PatientUser?>(null) }
    var lookupDone by remember { mutableStateOf(false) }

    LaunchedEffect(addResult) {
        if (addResult == "SUCCESS") {
            vm.clearAddCaregiverResult()
            onAdded()
        } else if (addResult != null) {
            error = addResult ?: ""
            vm.clearAddCaregiverResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Text("Add Caregiver", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        Text("Caregiver Phone Number", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(
            "Enter the phone number of the person you want to add as your caregiver.",
            fontSize = 11.sp,
            color = Color(0xFF61717D)
        )
        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextFieldInput(
                value = phone,
                onValueChange = {
                    phone = it
                    lookupResult = null
                    lookupDone = false
                    error = ""
                },
                label = "e.g. 0123456789",
                modifier = Modifier.weight(1f),
                readOnly = false,
                singleLine = true,
                keyboardType = KeyboardType.Phone
            )
            Spacer(Modifier.width(8.dp))
            button(
                text = if (isLookingUp) "" else "Look Up",
                onClick = {
                    if (phone.isBlank()) {
                        error = "Please enter a phone number"
                        return@button
                    }
                    if (phone.trim() == currentUserPhone) {
                        error = "You cannot add yourself as a caregiver"
                        return@button
                    }
                    isLookingUp = true
                    error = ""
                    scope.launch {
                        val user = getPatientByPhone(phone.trim())
                        lookupResult = user
                        lookupDone = true
                        isLookingUp = false
                        if (user == null) {
                            error = "No user found with this phone number"
                        }
                    }
                },
                modifier = Modifier.height(48.dp)
            )
        }

        if (isLookingUp) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.height(16.dp).width(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Looking up user...", fontSize = 12.sp, color = Color(0xFF61717D))
            }
        }

        if (lookupDone && lookupResult != null) {
            Spacer(Modifier.height(12.dp))
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(lookupResult!!.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Phone: ${lookupResult!!.phone ?: "N/A"}", fontSize = 12.sp, color = Color(0xFF61717D))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("Relationship", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value = relationship,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select relationship") },
                placeholder = { Text("Select relationship") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                relationshipOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.Black) },
                        onClick = {
                            relationship = option
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color(0xFFD32F2F), fontSize = 12.sp)
        }

        Spacer(Modifier.height(20.dp))

        button(
            text = "Add Caregiver",
            onClick = {
                when {
                    phone.isBlank() -> error = "Phone number is required"
                    !lookupDone || lookupResult == null -> error = "Please look up the user first"
                    relationship.isBlank() -> error = "Relationship is required"
                    else -> {
                        error = ""
                        vm.addCaregiver(
                            patientUserId = currentUserId,
                            phone = phone.trim(),
                            relationship = relationship,
                            patientName = currentUserName,
                            patientPhone = currentUserPhone
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
