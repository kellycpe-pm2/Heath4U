package com.example.healt4u.screen.Admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
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
import com.example.healt4u.Storage.adminResetPassword
import kotlinx.coroutines.launch

private val AppBlue = Color(0xFF3779EE)

private fun isValidPassword(password: String): Boolean {
    return password.length >= 6
}

@Composable
fun AdminResetPasswordScreen(
    emailOrPhone: String,
    method: String,
    onBack: () -> Unit,
    onPasswordReset: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var resetSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF101820))
                }
                Text("Reset Password", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        if (resetSuccess) Color(0xFF4CAF50) else AppBlue,
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (resetSuccess) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (resetSuccess) {
                Text(
                    text = "Password Reset\nSuccessful!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your password has been reset successfully.\nYou can now log in with your new password.",
                    fontSize = 14.sp,
                    color = Color(0xFF61717D),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onPasswordReset,
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(52.dp)
                ) {
                    Text(
                        "GO TO LOGIN",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Set New Password",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101820),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                val maskedCredential = if (method == "email") {
                    val parts = emailOrPhone.split("@")
                    if (parts.size == 2) {
                        val name = parts[0]
                        val masked = if (name.length > 2) {
                            name.first() + "*".repeat(name.length - 2) + name.last()
                        } else {
                            "*".repeat(name.length)
                        }
                        "$masked@${parts[1]}"
                    } else {
                        "***"
                    }
                } else {
                    if (emailOrPhone.length > 4) {
                        "*".repeat(emailOrPhone.length - 4) + emailOrPhone.takeLast(4)
                    } else {
                        "*".repeat(emailOrPhone.length)
                    }
                }

                Text(
                    text = "Resetting password for:\n$maskedCredential",
                    fontSize = 14.sp,
                    color = Color(0xFF61717D),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            placeholder = { Text("New Password", color = Color(0xFF9E9E9E)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AppBlue)
                            },
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

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = { Text("Confirm New Password", color = Color(0xFF9E9E9E)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AppBlue)
                            },
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

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    if (newPassword.isBlank()) {
                                        snackbarHostState.showSnackbar("Please enter a new password")
                                        return@launch
                                    }
                                    if (!isValidPassword(newPassword)) {
                                        snackbarHostState.showSnackbar("Password must be at least 6 characters")
                                        return@launch
                                    }
                                    if (newPassword != confirmPassword) {
                                        snackbarHostState.showSnackbar("Passwords do not match")
                                        return@launch
                                    }

                                    isLoading = true
                                    val result = adminResetPassword(emailOrPhone, method, newPassword)
                                    isLoading = false
                                    result.fold(
                                        onSuccess = {
                                            resetSuccess = true
                                        },
                                        onFailure = { e ->
                                            snackbarHostState.showSnackbar(e.message ?: "Password reset failed")
                                        }
                                    )
                                }
                            },
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
                                    "RESET PASSWORD",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!resetSuccess) {
                Text(
                    text = "Back to Login",
                    color = AppBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}
