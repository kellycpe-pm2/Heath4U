package com.example.healt4u.screen.Admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.example.healt4u.Storage.adminChangePassword
import com.example.healt4u.Storage.adminGetProfile
import com.example.healt4u.Storage.adminSignUp
import com.example.healt4u.Storage.adminUpdateEmail
import com.example.healt4u.Storage.adminUpdatePhone
import com.example.healt4u.Storage.adminUpdateUsername
import com.example.healt4u.model.AdminUser
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
fun AdminSettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = onBack,
    adminUsername: String = ""
) {
    var showProfile by remember { mutableStateOf(false) }
    var showCreateAdmin by remember { mutableStateOf(false) }

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Admin Profile",
                    subtitle = "View and edit your account",
                    onClick = { showProfile = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Create Admin Account",
                    subtitle = "Add a new admin to the system",
                    onClick = { showCreateAdmin = true }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
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

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
        ) {
            Text("Logout", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showProfile) {
        AdminProfileScreen(
            adminUsername = adminUsername,
            onBack = { showProfile = false }
        )
    }

    if (showCreateAdmin) {
        CreateAdminAccountDialog(
            onDismiss = { showCreateAdmin = false }
        )
    }
}

@Composable
private fun AdminProfileScreen(
    adminUsername: String,
    onBack: () -> Unit
) {
    var profile by remember { mutableStateOf<AdminUser?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showEditUsername by remember { mutableStateOf(false) }
    var showEditEmail by remember { mutableStateOf(false) }
    var showEditPhone by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun refreshProfile() {
        scope.launch {
            isLoading = true
            errorMessage = null
            val result = adminGetProfile(adminUsername)
            result.fold(
                onSuccess = { profile = it },
                onFailure = { errorMessage = it.message }
            )
            isLoading = false
        }
    }

    LaunchedEffect(adminUsername) {
        refreshProfile()
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                Text("Admin Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppBlue)
                    }
                }
                errorMessage != null -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F0))
                    ) {
                        Text(
                            text = errorMessage ?: "Failed to load profile",
                            color = Color(0xFFE53935),
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }
                }
                profile != null -> {
                    val user = profile!!

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(AppBlue, RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            ProfileField(
                                icon = Icons.Default.Person,
                                label = "Username",
                                value = user.username,
                                onClick = { showEditUsername = true }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            ProfileField(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = user.email ?: "Tap to set email",
                                isPlaceholder = user.email == null,
                                onClick = { showEditEmail = true }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            ProfileField(
                                icon = Icons.Default.Phone,
                                label = "Phone",
                                value = user.phone ?: "Tap to set phone number",
                                isPlaceholder = user.phone == null,
                                onClick = { showEditPhone = true }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        SettingsItem(
                            icon = Icons.Default.Visibility,
                            title = "Change Password",
                            subtitle = "Update your account password",
                            onClick = { showChangePassword = true }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        AppSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }

    if (showEditUsername && profile != null) {
        EditUsernameDialog(
            currentUsername = profile!!.username,
            onDismiss = { showEditUsername = false },
            onUsernameUpdated = { newUsername ->
                showEditUsername = false
                profile = profile!!.copy(username = newUsername)
                scope.launch { snackbarHostState.showSnackbar("Username updated successfully") }
            }
        )
    }

    if (showEditEmail && profile != null) {
        EditEmailDialog(
            adminUsername = profile!!.username,
            currentEmail = profile!!.email ?: "",
            onDismiss = { showEditEmail = false },
            onEmailUpdated = { newEmail ->
                showEditEmail = false
                profile = profile!!.copy(email = newEmail.ifBlank { null })
                scope.launch { snackbarHostState.showSnackbar("Email updated successfully") }
            }
        )
    }

    if (showEditPhone && profile != null) {
        EditPhoneDialog(
            adminUsername = profile!!.username,
            currentPhone = profile!!.phone ?: "",
            onDismiss = { showEditPhone = false },
            onPhoneUpdated = { newPhone ->
                showEditPhone = false
                profile = profile!!.copy(phone = newPhone.ifBlank { null })
                scope.launch { snackbarHostState.showSnackbar("Phone number updated successfully") }
            }
        )
    }

    if (showChangePassword && profile != null) {
        ChangePasswordDialog(
            adminUsername = profile!!.username,
            onDismiss = { showChangePassword = false }
        )
    }
}

@Composable
private fun ProfileField(
    icon: ImageVector,
    label: String,
    value: String,
    isPlaceholder: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
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
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = Color(0xFF61717D))
            Text(
                value,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isPlaceholder) AppBlue else Color(0xFF101820)
            )
        }
    }
}

@Composable
private fun EditUsernameDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onUsernameUpdated: (String) -> Unit
) {
    var newUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Change Username", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("New Username") },
                    placeholder = { Text(currentUsername) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Current Password") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = AppBlue
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val usernameToSave = newUsername.ifBlank { currentUsername }
                        if (newUsername.isNotBlank() && newUsername.length < 3) {
                            snackbarHostState.showSnackbar("Username must be at least 3 characters")
                            return@launch
                        }
                        if (password.isBlank()) {
                            snackbarHostState.showSnackbar("Please enter your current password")
                            return@launch
                        }

                        isLoading = true
                        val result = adminUpdateUsername(currentUsername, usernameToSave, password)
                        isLoading = false
                        result.fold(
                            onSuccess = { onUsernameUpdated(usernameToSave) },
                            onFailure = { e ->
                                snackbarHostState.showSnackbar(e.message ?: "Update failed")
                            }
                        )
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
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
private fun EditEmailDialog(
    adminUsername: String,
    currentEmail: String,
    onDismiss: () -> Unit,
    onEmailUpdated: (String) -> Unit
) {
    var newEmail by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Change Email", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentEmail.isNotBlank()) {
                    Text(
                        text = "Current: $currentEmail",
                        fontSize = 13.sp,
                        color = Color(0xFF61717D)
                    )
                }
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text(if (currentEmail.isBlank()) "Enter Email" else "New Email") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = AppBlue)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        if (newEmail.isBlank()) {
                            snackbarHostState.showSnackbar("Please enter an email address")
                            return@launch
                        }
                        if (!isValidEmail(newEmail)) {
                            snackbarHostState.showSnackbar("Please enter a valid email address")
                            return@launch
                        }

                        isLoading = true
                        val result = adminUpdateEmail(adminUsername, newEmail)
                        isLoading = false
                        result.fold(
                            onSuccess = { onEmailUpdated(newEmail) },
                            onFailure = { e ->
                                snackbarHostState.showSnackbar(e.message ?: "Update failed")
                            }
                        )
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
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
private fun EditPhoneDialog(
    adminUsername: String,
    currentPhone: String,
    onDismiss: () -> Unit,
    onPhoneUpdated: (String) -> Unit
) {
    var newPhone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Change Phone Number", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentPhone.isNotBlank()) {
                    Text(
                        text = "Current: $currentPhone",
                        fontSize = 13.sp,
                        color = Color(0xFF61717D)
                    )
                }
                OutlinedTextField(
                    value = newPhone,
                    onValueChange = { newPhone = it },
                    label = { Text(if (currentPhone.isBlank()) "Enter Phone Number" else "New Phone Number") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = AppBlue)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        if (newPhone.isBlank()) {
                            snackbarHostState.showSnackbar("Please enter a phone number")
                            return@launch
                        }
                        if (!isValidPhone(newPhone)) {
                            snackbarHostState.showSnackbar("Please enter a valid phone number (10-15 digits)")
                            return@launch
                        }

                        isLoading = true
                        val result = adminUpdatePhone(adminUsername, newPhone)
                        isLoading = false
                        result.fold(
                            onSuccess = { onPhoneUpdated(newPhone) },
                            onFailure = { e ->
                                snackbarHostState.showSnackbar(e.message ?: "Update failed")
                            }
                        )
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
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
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != {}) { onClick() }
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
private fun ChangePasswordDialog(
    adminUsername: String,
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
                            val result = adminChangePassword(adminUsername, currentPassword, newPassword)
                            isLoading = false
                            result.fold(
                                onSuccess = {
                                    showSuccess = true
                                },
                                onFailure = { e ->
                                    snackbarHostState.showSnackbar(e.message ?: "Password change failed")
                                }
                            )
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
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
}

@Composable
private fun CreateAdminAccountDialog(
    onDismiss: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Create Admin Account", fontWeight = FontWeight.Bold) },
        text = {
            if (showSuccess) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Admin account created successfully!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = AppBlue)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = AppBlue)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone (optional)") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = AppBlue)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password",
                                    tint = AppBlue
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle confirm password",
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
                            if (username.isBlank()) {
                                snackbarHostState.showSnackbar("Please enter a username")
                                return@launch
                            }
                            if (username.length < 3) {
                                snackbarHostState.showSnackbar("Username must be at least 3 characters")
                                return@launch
                            }
                            if (email.isBlank()) {
                                snackbarHostState.showSnackbar("Please enter an email address")
                                return@launch
                            }
                            if (!isValidEmail(email)) {
                                snackbarHostState.showSnackbar("Please enter a valid email address")
                                return@launch
                            }
                            if (password.isBlank()) {
                                snackbarHostState.showSnackbar("Please enter a password")
                                return@launch
                            }
                            if (password.length < 6) {
                                snackbarHostState.showSnackbar("Password must be at least 6 characters")
                                return@launch
                            }
                            if (password != confirmPassword) {
                                snackbarHostState.showSnackbar("Passwords do not match")
                                return@launch
                            }

                            isLoading = true
                            val result = adminSignUp(
                                username = username,
                                password = password,
                                email = email,
                                phone = phone.ifBlank { null }
                            )
                            isLoading = false
                            result.fold(
                                onSuccess = { showSuccess = true },
                                onFailure = { e ->
                                    snackbarHostState.showSnackbar(e.message ?: "Failed to create account")
                                }
                            )
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Create Account", color = Color.White)
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
}
