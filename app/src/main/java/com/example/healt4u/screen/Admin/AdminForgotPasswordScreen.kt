package com.example.healt4u.screen.Admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.adminFindAccount
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
fun AdminForgotPasswordScreen(
    onBack: () -> Unit,
    onAccountFound: (emailOrPhone: String, method: String) -> Unit
) {
    var method by remember { mutableStateOf("email") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
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
                Text("Forgot Password", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(AppBlue, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LockReset,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Reset Your Password",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101820),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your registered email or phone number and we'll help you reset your password.",
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choose recovery method",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF101820)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        MethodChip(
                            label = "Email",
                            selected = method == "email",
                            onClick = {
                                method = "email"
                                email = ""
                                phone = ""
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        MethodChip(
                            label = "Phone",
                            selected = method == "phone",
                            onClick = {
                                method = "phone"
                                email = ""
                                phone = ""
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (method == "email") {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("Registered Email Address", color = Color(0xFF9E9E9E)) },
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
                            onValueChange = { phone = it },
                            placeholder = { Text("Registered Phone Number", color = Color(0xFF9E9E9E)) },
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

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val credential = if (method == "email") email else phone
                                if (credential.isBlank()) {
                                    snackbarHostState.showSnackbar(
                                        if (method == "email") "Please enter your email" else "Please enter your phone number"
                                    )
                                    return@launch
                                }
                                if (method == "email" && !isValidEmail(email)) {
                                    snackbarHostState.showSnackbar("Please enter a valid email address")
                                    return@launch
                                }
                                if (method == "phone" && !isValidPhone(phone)) {
                                    snackbarHostState.showSnackbar("Please enter a valid phone number (10-15 digits)")
                                    return@launch
                                }

                                isLoading = true
                                val result = adminFindAccount(credential, method)
                                isLoading = false
                                result.fold(
                                    onSuccess = {
                                        onAccountFound(credential, method)
                                    },
                                    onFailure = { e ->
                                        snackbarHostState.showSnackbar(e.message ?: "Account not found")
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
                                "CONTINUE",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
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
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
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
