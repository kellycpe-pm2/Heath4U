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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val AppBlue = Color(0xFF3779EE)
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
    onAdminLoginSuccess: (AdminUser) -> Unit,
    onPatientLoginClick: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onDoctorSuccessClick : ()->Unit  ={}
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var loginMethod by remember { mutableStateOf("email") }
    var isLoading by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("admin@gmail.com") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("admin123") }
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

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(AppBlue, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }

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
                    onDoctorClick = {onDoctorSuccessClick()  }
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
                        email = "admin@gmail.com"
                        phone = ""
                        password = "admin123"
                    },
                    onForgotPassword = onForgotPassword,
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
    onDoctorClick : ()->Unit

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
                text = "Login To Your\nAccount",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Choose your role",
            color = Color(0xFF101820),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAdminClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(AppBlue, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Admin", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF101820))
                    Text("Manage inventory, hospitals, doctors", fontSize = 12.sp, color = Color(0xFF61717D))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPatientClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalHospital, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Patient", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF101820))
                    Text("View medicine schedule & chat with doctors", fontSize = 12.sp, color = Color(0xFF61717D))
                }
            }
        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDoctorClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Emergency, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Doctor", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF101820))
                }
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
    onForgotPassword: () -> Unit,
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
            text = "Forgot Password?",
            color = AppBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onForgotPassword() }
        )

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
