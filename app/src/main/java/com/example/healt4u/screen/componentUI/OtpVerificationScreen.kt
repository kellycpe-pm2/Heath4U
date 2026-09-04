package com.example.healt4u.screen.componentUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.requestOtp
import com.example.healt4u.Storage.verifyOtp
import kotlinx.coroutines.launch

private val AppBlue = Color(0xFF3779EE)

// Generic OTP entry screen — takes whatever identifier (email/phone) the
// previous screen found an account for, and calls onVerified() once the
// 6-digit code checks out. Used by both Admin and Patient forgot-password.
@Composable
fun OtpVerificationScreen(
    identifier: String,
    themeColor: Color = AppBlue,
    onBack: () -> Unit,
    onVerified: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var demoOtp by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun sendOtp(isResend: Boolean) {
        scope.launch {
            if (isResend) isResending = true else isLoading = true
            val result = requestOtp(identifier)
            if (isResend) isResending = false else isLoading = false

            result.fold(
                onSuccess = { code ->
                    demoOtp = code
                    snackbarHostState.showSnackbar(
                        if (isResend) "A new OTP has been sent" else "OTP sent to $identifier"
                    )
                },
                onFailure = { e ->
                    snackbarHostState.showSnackbar(e.message ?: "Failed to send OTP")
                }
            )
        }
    }

    LaunchedEffect(identifier) { sendOtp(isResend = false) }

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
                Text("Verify OTP", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(themeColor, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Enter Verification Code",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101820),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We've sent a 6-digit code to $identifier",
                fontSize = 14.sp,
                color = Color(0xFF61717D),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
            )

            // Demo-mode banner: since this project has no real SMS/email
            // gateway wired in, the OTP is shown here so the flow can be
            // demonstrated end-to-end. Remove this in a production build.
            if (demoOtp != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "Demo mode — no SMS/email gateway configured",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8A6D00)
                        )
                        Text(
                            "Your OTP: $demoOtp",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8A6D00)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
                        placeholder = { Text("6-digit code", color = Color(0xFF9E9E9E)) },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 8.sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = themeColor,
                            unfocusedIndicatorColor = Color(0xFFBDBDBD),
                            cursorColor = themeColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                if (otpCode.length != 6) {
                                    snackbarHostState.showSnackbar("Please enter the 6-digit code")
                                    return@launch
                                }
                                isLoading = true
                                val result = verifyOtp(identifier, otpCode)
                                isLoading = false
                                result.fold(
                                    onSuccess = { onVerified() },
                                    onFailure = { e -> snackbarHostState.showSnackbar(e.message ?: "Verification failed") }
                                )
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("VERIFY", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isResending) "Sending..." else "Didn't get a code? Resend",
                        color = themeColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(enabled = !isResending) { sendOtp(isResend = true) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}
