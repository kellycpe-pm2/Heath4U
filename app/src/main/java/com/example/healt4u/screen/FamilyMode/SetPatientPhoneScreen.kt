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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healt4u.ViewModel.FamilyModeViewModel
import com.example.healt4u.screen.componentUI.TextFieldInput
import com.example.healt4u.screen.componentUI.button

@Composable
fun SetPatientPhoneScreen(
    vm: FamilyModeViewModel,
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentPhone by vm.patientPhone.collectAsStateWithLifecycle()
    var phone by remember { mutableStateOf(currentPhone) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Text("My Phone Number", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        Text(
            "This is the number your caregivers will call or message when you miss a dose.",
            fontSize = 12.sp,
            color = Color(0xFF61717D)
        )

        Spacer(Modifier.height(20.dp))

        Text("Phone Number", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        TextFieldInput(
            value = phone,
            onValueChange = { phone = it },
            label = "e.g. 0123456789",
            modifier = Modifier.fillMaxWidth(),
            readOnly = false,
            singleLine = true
        )

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color(0xFFD32F2F), fontSize = 12.sp)
        }

        Spacer(Modifier.height(20.dp))

        button(
            text = "Save Number",
            onClick = {
                if (phone.isBlank()) {
                    error = "Phone number is required"
                } else {
                    vm.savePatientPhone(context, phone.trim())
                    onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
