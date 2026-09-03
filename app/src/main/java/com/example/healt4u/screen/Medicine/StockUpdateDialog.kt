package com.example.healt4u.screen.Medicine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restore
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
import com.example.healt4u.model.Medicine
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StockUpdateDialog(
    medicine: Medicine,
    onDismiss: () -> Unit,
    onUpdate: (Int) -> Unit,
    isLoading: Boolean = false
) {
    // State for the new quantity
    var quantity by remember { mutableStateOf(medicine.quantityLeft ?: medicine.quantity) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }

    // Format date helper
    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return "N/A"
        return try {
            val date = Date(timestamp)
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            "Invalid"
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📦 Update Stock",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ===== MEDICINE NAME =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = medicine.name_medicine,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${medicine.dosage}mg • ${medicine.category}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===== CURRENT STOCK INFO =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Current Stock
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Current",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${medicine.quantityLeft ?: medicine.quantity}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                            Text(
                                text = "in stock",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Total Quantity
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${medicine.quantity}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF388E3C)
                            )
                            Text(
                                text = "capacity",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===== EXPIRY INFO =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val expiryColor = if (medicine.expiredDate?.let {
                            it < System.currentTimeMillis()
                        } == true) {
                        Color.Red
                    } else if (medicine.expiredDate?.let {
                            it < System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
                        } == true) {
                        Color(0xFFFF9800)
                    } else {
                        Color(0xFF4CAF50)
                    }

                    Text(
                        text = "Expires: ${formatDate(medicine.expiredDate)}",
                        fontSize = 13.sp,
                        color = expiryColor,
                        fontWeight = FontWeight.Medium
                    )

                    // Stock status badge
                    val stockStatus = when {
                        (medicine.quantityLeft ?: medicine.quantity) <= 0 -> "Out of Stock"
                        (medicine.quantityLeft ?: medicine.quantity) <= 5 -> "Critical"
                        (medicine.quantityLeft ?: medicine.quantity) <= 10 -> "Low"
                        else -> "In Stock"
                    }

                    val statusColor = when {
                        (medicine.quantityLeft ?: medicine.quantity) <= 0 -> Color.Red
                        (medicine.quantityLeft ?: medicine.quantity) <= 5 -> Color(0xFFFF5722)
                        (medicine.quantityLeft ?: medicine.quantity) <= 10 -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stockStatus,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ===== QUANTITY INPUT =====
                Text(
                    text = "New Stock Quantity",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minus button
                    IconButton(
                        onClick = {
                            if (quantity > 0) {
                                quantity--
                                errorMessage = null
                                showError = false
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = if (quantity > 0) Color(0xFF1976D2) else Color.Gray
                        )
                    }

                    // Quantity display
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(56.dp)
                    ) {
                        OutlinedTextField(
                            value = quantity.toString(),
                            onValueChange = {
                                val newValue = it.toIntOrNull()
                                if (newValue != null && newValue >= 0) {
                                    quantity = newValue
                                    errorMessage = null
                                    showError = false
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            isError = showError,
                            enabled = !isLoading,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0xFFBDBDBD)
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    // Plus button
                    IconButton(
                        onClick = {
                            if (quantity < medicine.quantity) {
                                quantity++
                                errorMessage = null
                                showError = false
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = if (quantity < medicine.quantity) Color(0xFF1976D2) else Color.Gray
                        )
                    }
                }

                // Error message
                if (showError && errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Quick actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reset to original quantity
                    OutlinedButton(
                        onClick = {
                            quantity = medicine.quantity
                            errorMessage = null
                            showError = false
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {

                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", color = MaterialTheme.colorScheme.onBackground, fontSize = 10.sp)
                    }

                    // Set to half
                    OutlinedButton(
                        onClick = {
                            val half = medicine.quantity / 2
                            quantity = if (half > 0) half else 1
                            errorMessage = null
                            showError = false
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("½",color = MaterialTheme.colorScheme.onBackground)
                    }

                    // Set to quarter
                    OutlinedButton(
                        onClick = {
                            val quarter = medicine.quantity / 4
                            quantity = if (quarter > 0) quarter else 1
                            errorMessage = null
                            showError = false
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("¼",color = MaterialTheme.colorScheme.onBackground)
                    }
                }

                // Progress indicator for loading state
                if (isLoading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate
                    if (quantity < 0) {
                        errorMessage = "Quantity cannot be negative"
                        showError = true
                        return@Button
                    }

                    if (quantity > medicine.quantity) {
                        errorMessage = "Cannot exceed total (${medicine.quantity})"
                        showError = true
                        return@Button
                    }

                    if (quantity == (medicine.quantityLeft ?: medicine.quantity)) {
                        errorMessage = "Stock already at $quantity"
                        showError = true
                        return@Button
                    }

                    onUpdate(quantity)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    Row(horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Updating...", color = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    Text(
                        "Update Stock",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { if (!isLoading) onDismiss() },
                enabled = !isLoading
            ) {
                Text("Cancel", fontSize = 14.sp)
            }
        }
    )
}