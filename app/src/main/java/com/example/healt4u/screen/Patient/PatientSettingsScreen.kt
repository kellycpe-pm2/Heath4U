package com.example.healt4u.screen.Patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.getPatientById
import com.example.healt4u.Storage.patientChangePassword
import com.example.healt4u.Storage.patientUpdateEmail
import com.example.healt4u.Storage.patientUpdateName
import com.example.healt4u.Storage.patientUpdatePhone
import com.example.healt4u.model.PatientUser
import com.example.healt4u.screen.componentUI.AppSnackbarHost
import kotlinx.coroutines.launch

private val AppBlue = Color(0xFF3779EE)

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun isValidPhone(phone: String): Boolean {
    val digitsOnly = phone.filter { it.isDigit() }
    return digitsOnly.length in 10..15
}

@Composable
fun PatientSettingsScreen(
    patientId: Int,
    startAtProfile: Boolean = false,
    onBack: () -> Unit,
    onSwitchAccount: () -> Unit,
    onLogout: () -> Unit
) {
    var showProfile by remember { mutableStateOf(startAtProfile) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF101820))
            }
            Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Text(
            "Account Management",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF61717D)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Patient Profile",
                    subtitle = "View and edit your account",
                    onClick = { showProfile = true }
                )
            }
        }

        Text(
            "About & Support",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF61717D)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "HEALTH4U v1.0.0",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    icon = Icons.Default.SupportAgent,
                    title = "Support",
                    subtitle = "health4u.support@email.com",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Terms of Service",
                    subtitle = "View terms and privacy policy",
                    onClick = {}
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onSwitchAccount,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppBlue)
        ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Switch Account", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
        ) {
            Text("Logout", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showProfile) {
        PatientProfileScreen(
            patientId = patientId,
            onBack = { showProfile = false }
        )
    }
}

@Composable
private fun PatientProfileScreen(
    patientId: Int,
    onBack: () -> Unit
) {
    var profile by remember { mutableStateOf<PatientUser?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var showEditName by remember { mutableStateOf(false) }
    var showEditEmail by remember { mutableStateOf(false) }
    var showEditPhone by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun refreshProfile() {
        scope.launch {
            isLoading = true
            profile = getPatientById(patientId)
            isLoading = false
        }
    }

    LaunchedEffect(patientId) { refreshProfile() }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF101820))
                }
                Text("Patient Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppBlue)
                }
            } else if (profile == null) {
                Text(
                    "Profile not found",
                    modifier = Modifier.padding(20.dp),
                    color = Color(0xFF61717D)
                )
            } else {
                val p = profile!!
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Person,
                            title = "Name",
                            subtitle = p.name,
                            onClick = { showEditName = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItem(
                            icon = Icons.Default.Email,
                            title = "Email",
                            subtitle = p.email?.ifBlank { "Not set" } ?: "Not set",
                            onClick = { showEditEmail = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItem(
                            icon = Icons.Default.Phone,
                            title = "Phone",
                            subtitle = p.phone?.ifBlank { "Not set" } ?: "Not set",
                            onClick = { showEditPhone = true }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Visibility,
                        title = "Change Password",
                        subtitle = "Update your login password",
                        onClick = { showChangePassword = true }
                    )
                }
            }
        }
    }

    if (showEditName && profile != null) {
        EditFieldDialog(
            title = "Edit Name",
            label = "Name",
            initialValue = profile!!.name,
            onDismiss = { showEditName = false },
            onSave = { newValue ->
                patientUpdateName(patientId, newValue)
            },
            onSaved = {
                showEditName = false
                refreshProfile()
            }
        )
    }

    if (showEditEmail && profile != null) {
        EditFieldDialog(
            title = "Edit Email",
            label = "Email",
            initialValue = profile!!.email ?: "",
            validate = { it.isBlank() || isValidEmail(it) },
            validationMessage = "Please enter a valid email address",
            onDismiss = { showEditEmail = false },
            onSave = { newValue ->
                patientUpdateEmail(patientId, newValue)
            },
            onSaved = {
                showEditEmail = false
                refreshProfile()
            }
        )
    }

    if (showEditPhone && profile != null) {
        EditFieldDialog(
            title = "Edit Phone",
            label = "Phone",
            initialValue = profile!!.phone ?: "",
            validate = { it.isBlank() || isValidPhone(it) },
            validationMessage = "Please enter a valid phone number",
            onDismiss = { showEditPhone = false },
            onSave = { newValue ->
                patientUpdatePhone(patientId, newValue)
            },
            onSaved = {
                showEditPhone = false
                refreshProfile()
            }
        )
    }

    if (showChangePassword) {
        PatientChangePasswordDialog(
            patientId = patientId,
            onDismiss = { showChangePassword = false }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(AppBlue.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AppBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF101820))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF61717D))
        }
    }
}

@Composable
private fun EditFieldDialog(
    title: String,
    label: String,
    initialValue: String,
    validate: (String) -> Boolean = { true },
    validationMessage: String = "Invalid value",
    onDismiss: () -> Unit,
    onSave: suspend (String) -> Result<String>,
    onSaved: () -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it; errorText = null },
                    label = { Text(label) },
                    singleLine = true,
                    isError = errorText != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Text(errorText!!, color = Color(0xFFD32F2F), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!validate(value)) {
                        errorText = validationMessage
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        val result = onSave(value)
                        isLoading = false
                        result.fold(
                            onSuccess = { onSaved() },
                            onFailure = { errorText = it.message }
                        )
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Save", color = Color.White)
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
private fun PatientChangePasswordDialog(
    patientId: Int,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box {
        AlertDialog(
            onDismissRequest = { if (!isLoading) onDismiss() },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                if (showSuccess) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Password changed successfully!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Current Password") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                    Icon(
                                        if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = AppBlue
                                    )
                                }
                            },
                            visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = AppBlue
                                    )
                                }
                            },
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm New Password") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = AppBlue
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (showSuccess) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("OK", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                if (currentPassword.isBlank()) {
                                    snackbarHostState.showSnackbar("Please enter your current password")
                                    return@launch
                                }
                                if (newPassword.isBlank()) {
                                    snackbarHostState.showSnackbar("Please enter a new password")
                                    return@launch
                                }
                                if (newPassword.length < 6) {
                                    snackbarHostState.showSnackbar("New password must be at least 6 characters")
                                    return@launch
                                }
                                if (newPassword != confirmPassword) {
                                    snackbarHostState.showSnackbar("New passwords do not match")
                                    return@launch
                                }
                                if (newPassword == currentPassword) {
                                    snackbarHostState.showSnackbar("New password must be different from current")
                                    return@launch
                                }

                                isLoading = true
                                val result = patientChangePassword(patientId, currentPassword, newPassword)
                                isLoading = false
                                result.fold(
                                    onSuccess = { showSuccess = true },
                                    onFailure = { e -> snackbarHostState.showSnackbar(e.message ?: "Password change failed") }
                                )
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            },
            dismissButton = {
                if (!showSuccess) {
                    Button(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                }
            }
        )

        AppSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
