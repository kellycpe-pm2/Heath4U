package com.example.healt4u.screen.Doctor

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import com.example.healt4u.Storage.doctorSignIn
import com.example.healt4u.model.Doctor
import com.example.healt4u.screen.componentUI.AppLogo
import com.example.healt4u.screen.componentUI.AppSnackbarHost
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
fun DoctorLoginScreen(
    onLoginSuccess: (userId: Int, userName: String, userPhone: String) -> Unit,
    onBack: () -> Unit = {}
) {
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
            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF101820))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AppLogo(modifier = Modifier.size(90.dp))

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "HEALTH4U",
                color = AppBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            DoctorAuthContent(
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
                onBack = onBack,
                onSubmit = {
                    scope.launch {
                        handleDoctorSignIn(
                            loginMethod = loginMethod,
                            email = email,
                            phone = phone,
                            password = password,
                            snackbarHostState = snackbarHostState,
                            isLoading = { isLoading = it },
                            onSuccess = { user ->
                                onLoginSuccess(user.id, user.name, user.phone)
                            }
                        )
                    }
                }
            )
        }

        AppSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

private suspend fun handleDoctorSignIn(
    loginMethod: String,
    email: String,
    phone: String,
    password: String,
    snackbarHostState: SnackbarHostState,
    isLoading: (Boolean) -> Unit,
    onSuccess: (Doctor) -> Unit
) {
    val credential = if (loginMethod == "email") email else phone
    if (credential.isBlank()) {
        snackbarHostState.showSnackbar(
            if (loginMethod == "email") "Please enter your email address" else "Please enter your phone number"
        )
        return
    }

    if (loginMethod == "email" && !isValidEmail(email)) {
        snackbarHostState.showSnackbar("Please enter a valid email address")
        return
    }

    if (loginMethod == "phone" && !isValidPhone(phone)) {
        snackbarHostState.showSnackbar("Please enter a valid phone number")
        return
    }

    if (password.isBlank()) {
        snackbarHostState.showSnackbar("Please enter a password")
        return
    }

    isLoading(true)
    val result = doctorSignIn(credential, password, loginMethod)
    isLoading(false)
    result.fold(
        onSuccess = { user -> onSuccess(user) },
        onFailure = { e ->
            snackbarHostState.showSnackbar(e.message ?: "Login failed")
        }
    )
}

@Composable
private fun DoctorAuthContent(
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
                text = "Doctor\nLogin",
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
            MethodChip(label = "Email", selected = loginMethod == "email", onClick = { onLoginMethodChange("email") })
            Spacer(Modifier.width(12.dp))
            MethodChip(label = "Phone", selected = loginMethod == "phone", onClick = { onLoginMethodChange("phone") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loginMethod == "email") {
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = { Text("Email Address", color = Color(0xFF9E9E9E)) },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = AppBlue) },
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = AppBlue,
                    unfocusedIndicatorColor = Color(0xFFBDBDBD)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                placeholder = { Text("Phone Number", color = Color(0xFF9E9E9E)) },
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = AppBlue) },
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = AppBlue,
                    unfocusedIndicatorColor = Color(0xFFBDBDBD)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("Password", color = Color(0xFF9E9E9E)) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = AppBlue) },
            trailingIcon = {
                IconButton(onClick = onTogglePassword) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = AppBlue)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(50.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = AppBlue,
                unfocusedIndicatorColor = Color(0xFFBDBDBD)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onSubmit,
            enabled = !isLoading,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("LOGIN", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
private fun MethodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) AppBlue else Color.White
    val textColor = if (selected) Color.White else AppBlue
    val borderColor = if (selected) AppBlue else Color(0xFFBDBDBD)
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(50.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(text = label, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp))
    }
}
