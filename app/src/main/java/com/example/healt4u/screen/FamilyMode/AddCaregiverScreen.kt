package com.example.healt4u.screen.FamilyMode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healt4u.ViewModel.FamilyModeViewModel
import com.example.healt4u.screen.componentUI.TextFieldInput
import com.example.healt4u.screen.componentUI.button

private val relationshipOptions = listOf(
    "Daughter", "Son", "Spouse", "Dad", "Mom",
    "Relative", "Siblings", "Friends", "Others"
)

private fun isValidPhone(phone: String): Boolean {
    val digitsOnly = phone.filter { it.isDigit() }
    return digitsOnly.length in 10..15
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCaregiverScreen(
    vm: FamilyModeViewModel,
    onBack: () -> Unit = {},
    onAdded: () -> Unit = {}
) {
    val context = LocalContext.current
    val caregivers by vm.caregivers.collectAsStateWithLifecycle()
    val patientPhone by vm.patientPhone.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Text("Add Caregiver", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(16.dp))

        Text("Caregiver Name", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        TextFieldInput(
            value = name,
            onValueChange = { name = it },
            label = "Full name",
            modifier = Modifier.fillMaxWidth(),
            readOnly = false,
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        Text("Phone Number", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        TextFieldInput(
            value = phone,
            onValueChange = { phone = it },
            label = "e.g. 0123456789",
            modifier = Modifier.fillMaxWidth(),
            readOnly = false,
            singleLine = true,
            keyboardType = KeyboardType.Phone
        )

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
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                relationshipOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
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
                    name.isBlank() -> error = "Name is required"
                    phone.isBlank() -> error = "Phone number is required"
                    !isValidPhone(phone) -> error = "Invalid phone number format (10-15 digits)"
                    phone == patientPhone -> error = "Phone number cannot be the same as your phone number"
                    caregivers.any { it.phone == phone } -> error = "This phone number is already linked to another caregiver"
                    relationship.isBlank() -> error = "Relationship is required"
                    else -> {
                        vm.addCaregiver(context, name.trim(), phone.trim(), relationship.trim())
                        onAdded()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
