package com.example.healt4u.screen.Admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.example.healt4u.Storage.adminSignUp
import kotlinx.coroutines.launch

private val AppBlue = Color(0xFF3779EE)
private val ScreenBlue = Color(0xFFE6F8FC)

@Composable
fun AdminLoginScreen(
    onAdminLoginSuccess: () -> Unit,
    onPatientLoginSuccess: () -> Unit
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().background(ScreenBlue),
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
                    onPatientClick = { onPatientLoginSuccess() }
                )
            } else {
                AdminAuthContent(
                    isSignUp = isSignUp,
                    username = username,
                    password = password,
                    passwordVisible = passwordVisible,
                    isLoading = isLoading,
                    onUsernameChange = { username = it },
                    onPasswordChange = { password = it },
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    onToggleMode = {
                        isSignUp = !isSignUp
                        username = ""
                        password = ""
                    },
                    onBack = {
                        selectedRole = null
                        username = ""
                        password = ""
                        isSignUp = false
                    },
                    onSubmit = {
                        if (username.isBlank() || password.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please fill in all fields") }
                            return@AdminAuthContent
                        }
                        isLoading = true
                        scope.launch {
                            if (isSignUp) {
                                val signUpResult = adminSignUp(username, password)
                                isLoading = false
                                signUpResult.fold(
                                    onSuccess = {
                                        isSignUp = false
                                        scope.launch { snackbarHostState.showSnackbar("Account created! Logging in...") }
                                        isLoading = true
                                        val signInResult = adminSignIn(username, password)
                                        isLoading = false
                                        signInResult.fold(
                                            onSuccess = { onAdminLoginSuccess() },
                                            onFailure = { e ->
                                                scope.launch { snackbarHostState.showSnackbar(e.message ?: "Login failed. Try logging in manually.") }
                                            }
                                        )
                                    },
                                    onFailure = { e ->
                                        scope.launch { snackbarHostState.showSnackbar(e.message ?: "Registration failed") }
                                    }
                                )
                            } else {
                                val result = adminSignIn(username, password)
                                isLoading = false
                                result.fold(
                                    onSuccess = { onAdminLoginSuccess() },
                                    onFailure = { e ->
                                        scope.launch { snackbarHostState.showSnackbar(e.message ?: "Login failed") }
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun RoleSelectionContent(
    onAdminClick: () -> Unit,
    onPatientClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
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
    }
}

@Composable
private fun AdminAuthContent(
    isSignUp: Boolean,
    username: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onToggleMode: () -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
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
                text = if (isSignUp) "Create Admin\nAccount" else "Admin\nLogin",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            placeholder = { Text("Username", color = Color(0xFF9E9E9E)) },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null, tint = AppBlue)
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
                    if (isSignUp) "REGISTER" else "LOGIN",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isSignUp) "Already have an account? " else "Don't have an account? ",
                color = Color(0xFF61717D),
                fontSize = 12.sp
            )
            Text(
                if (isSignUp) "Login Here" else "Register Here",
                color = AppBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onToggleMode() }
            )
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
