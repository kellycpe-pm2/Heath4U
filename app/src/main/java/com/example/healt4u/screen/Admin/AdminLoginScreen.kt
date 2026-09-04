package com.example.healt4u.screen.Admin

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.adminSignIn
import com.example.healt4u.model.AdminUser
import com.example.healt4u.screen.componentUI.AppLogo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import androidx.compose.ui.graphics.Brush

private val AppBlue = Color(0xFF3779EE)
private val AppBlueDark = Color(0xFF1E56C5)
private val ScreenBlue = Color(0xFFE6F8FC)

private fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun isValidPhone(phone: String): Boolean {
    val digitsOnly = phone.filter { it.isDigit() }
    return digitsOnly.length in 10..15
}

@Composable
fun AdminLoginScreen(
    initialRole: String? = null,
    onAdminLoginSuccess: (AdminUser) -> Unit,
    onPatientLoginClick: () -> Unit = {},
    onDoctorLoginClick : ()->Unit  ={}
) {
    var selectedRole by remember { mutableStateOf<String?>(initialRole) }
    var loginMethod by remember { mutableStateOf("email") }
    var isLoading by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBlue)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            AppLogo(modifier = Modifier.size(90.dp))

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "HEALTH4U",
                color = AppBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (selectedRole == null) {
                RoleSelectionContent(
                    onAdminClick = { selectedRole = "admin" },
                    onPatientClick = { onPatientLoginClick() },
                    onDoctorClick = {onDoctorLoginClick()  }
                )
            } else {
                AdminAuthContent(
                    loginMethod = loginMethod,
                    email = email,
                    phone = phone,
                    password = password,
                    passwordVisible = passwordVisible,
                    isLoading = isLoading,
                    onEmailChange = { email = it },
                    onPhoneChange = { phone = it },
                    onPasswordChange = { password = it },
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    onLoginMethodChange = { loginMethod = it },
                    onBack = {
                        selectedRole = null
                        email = ""
                        phone = ""
                        password = ""
                    },
                    onSubmit = {
                        scope.launch {
                            handleSignIn(
                                loginMethod = loginMethod,
                                email = email,
                                phone = phone,
                                password = password,
                                snackbarHostState = snackbarHostState,
                                scope = scope,
                                isLoading = { isLoading = it },
                                onSuccess = onAdminLoginSuccess
                            )
                        }
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

private suspend fun handleSignIn(
    loginMethod: String,
    email: String,
    phone: String,
    password: String,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    isLoading: (Boolean) -> Unit,
    onSuccess: (AdminUser) -> Unit
) {
    val credential = if (loginMethod == "email") email else phone
    if (credential.isBlank()) {
        scope.launch {
            snackbarHostState.showSnackbar(
                if (loginMethod == "email") "Please enter your email address" else "Please enter your phone number"
            )
        }
        return
    }
    if (loginMethod == "email" && !isValidEmail(email)) {
        scope.launch { snackbarHostState.showSnackbar("Please enter a valid email address") }
        return
    }
    if (loginMethod == "phone" && !isValidPhone(phone)) {
        scope.launch { snackbarHostState.showSnackbar("Please enter a valid phone number") }
        return
    }
    if (password.isBlank()) {
        scope.launch { snackbarHostState.showSnackbar("Please enter your password") }
        return
    }

    isLoading(true)
    val result = adminSignIn(credential, password, loginMethod)
    isLoading(false)
    result.fold(
        onSuccess = { admin -> onSuccess(admin) },
        onFailure = { e ->
            scope.launch { snackbarHostState.showSnackbar(e.message ?: "Login failed") }
        }
    )
}

@Composable
private fun RoleSelectionContent(
    onAdminClick: () -> Unit,
    onPatientClick: () -> Unit,
    onDoctorClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(listOf(AppBlue, AppBlueDark)),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Login To Your\nAccount",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Choose your role",
            color = Color(0xFF101820),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        RoleCard(
            label = "Admin",
            description = "Manage inventory, hospitals, and medical staff",
            icon = Icons.Default.AdminPanelSettings,
            iconColor = AppBlue,
            onClick = onAdminClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoleCard(
            label = "Patient",
            description = "View schedules, medication, and chat with doctors",
            icon = Icons.Default.LocalHospital,
            iconColor = Color(0xFF4CAF50),
            onClick = onPatientClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoleCard(
            label = "Doctor",
            description = "Manage consultations and view patient records",
            icon = Icons.Filled.Emergency,
            iconColor = Color(0xFFFF5722),
            onClick = onDoctorClick
        )
    }
}

@Composable
private fun RoleCard(
    label: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101820)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF61717D),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun AdminAuthContent(
    loginMethod: String,
    email: String,
    phone: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onLoginMethodChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBlue, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Admin\nLogin",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            MethodChip(
                label = "Email",
                selected = loginMethod == "email",
                onClick = { onLoginMethodChange("email") }
            )
            Spacer(Modifier.width(12.dp))
            MethodChip(
                label = "Phone",
                selected = loginMethod == "phone",
                onClick = { onLoginMethodChange("phone") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loginMethod == "email") {
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = { Text("Email Address", color = Color(0xFF9E9E9E)) },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = AppBlue)
                },
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = AppBlue,
                    unfocusedIndicatorColor = Color(0xFFBDBDBD),
                    cursorColor = AppBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                placeholder = { Text("Phone Number", color = Color(0xFF9E9E9E)) },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = AppBlue)
                },
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = AppBlue,
                    unfocusedIndicatorColor = Color(0xFFBDBDBD),
                    cursorColor = AppBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("Password", color = Color(0xFF9E9E9E)) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = AppBlue)
            },
            trailingIcon = {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password",
                        tint = AppBlue
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(50.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = AppBlue,
                unfocusedIndicatorColor = Color(0xFFBDBDBD),
                cursorColor = AppBlue
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onSubmit,
            enabled = !isLoading,
            shape = RoundedCornerShape(50.dp),
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
                    "LOGIN",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Back to role selection",
            color = AppBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onBack() }
        )
    }
}

@Composable
private fun MethodChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) AppBlue else Color.White
    val textColor = if (selected) Color.White else AppBlue
    val borderColor = if (selected) AppBlue else Color(0xFFBDBDBD)

    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(50.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
        )
    }
}
