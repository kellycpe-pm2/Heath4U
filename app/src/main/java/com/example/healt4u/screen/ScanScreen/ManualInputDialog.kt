package com.example.healt4u.screen.ScanScreen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ManualInputDialog(
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFocused by remember { mutableStateOf(false) }

    // Animation states
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(300)) +
                    scaleIn(initialScale = 0.8f, animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )),
            exit = fadeOut(animationSpec = tween(200)) +
                    scaleOut(targetScale = 0.8f, animationSpec = tween(200))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFF8F9FA)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    // ===== HEADER WITH ICON =====
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Icon and Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Manual Input",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A2E)
                                )
                            }

                            Text(
                                text = "Enter the MAL number to search",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        // Close button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF9E9E9E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ===== DIVIDER =====
                    Divider(
                        color = Color(0xFFEEEEEE),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ===== INPUT FIELD =====
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Label with required indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MAL Number",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = "*",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "(e.g. MAL123456789X)",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }

                        // Input field with validation
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = {
                                val upper = it.uppercase()
                                inputText = upper
                                isError = false
                                errorMessage = null
                            },
                            label = {
                                Text(
                                    "Enter MAL Number",
                                    color = if (isFocused) MaterialTheme.colorScheme.secondary else Color(0xFF757575)
                                )
                            },
                            placeholder = {
                                Text(
                                    "MAL123456789X",
                                    color = Color(0xFFBDBDBD)
                                )
                            },
                            isError = isError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = if (isFocused) 4.dp else 0.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    clip = false
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedTextColor = Color(0xFF1A1A2E),
                                unfocusedTextColor = Color(0xFF1A1A2E),
                                cursorColor = MaterialTheme.colorScheme.secondary,
                                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                                unfocusedLabelColor = Color(0xFF757575),
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                errorLabelColor = MaterialTheme.colorScheme.error
                            ),
                            singleLine = true
                        )

                        // Error message below the text field
                        if (isError && errorMessage != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp, top = 2.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ===== ACTION BUTTONS =====
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel button
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(
                                "Cancel",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Search button
                        Button(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    // Simple validation
                                    val cleanInput = inputText.trim().uppercase()
                                    val isValid = cleanInput.matches(Regex("^MAL\\d{6,12}[A-Z]{0,2}$"))

                                    if (!isValid) {
                                        isError = true
                                        errorMessage = "Invalid format.\nValid formats: MAL123456789X"
                                        return@Button
                                    }

                                    onSearch(cleanInput)
                                    onDismiss()
                                } else {
                                    isError = true
                                    errorMessage = "Please enter a MAL number"
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = inputText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                disabledContainerColor = Color(0xFFE0E0E0)
                            )
                        ) {
                            Text(
                                "Search",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}