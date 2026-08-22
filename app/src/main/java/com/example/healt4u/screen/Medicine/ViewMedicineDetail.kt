package com.example.healt4u.screen.Medicine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.data.MedicineData
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.screen.componentUI.button

@Composable
fun MedicineDetailScreen(
    medicine: Medicine?,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    colorTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Medicine Details",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = " ",
                        fontSize = 48.sp
                    )
                    Text(
                        text = medicine?.name_medicine?:"",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    DetailRow(
                        icon = Icons.Filled.Category,
                        label = "Category",
                        value = medicine?.category?: "${MedicineData.categories.first()}"
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        icon = Icons.Filled.Medication,
                        label = "Dosage",
                        value = "${medicine?.dosage} mg"
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        icon = Icons.Filled.Inventory,
                        label = "Quantity",
                        value = "${medicine?.quantity}"
                    )

                    medicine?.quantityLeft?.let { left ->
                        if (left > 0) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow(
                                icon = Icons.Filled.LocalOffer,
                                label = "Quantity Left",
                                value = "${left}",
                                valueColor = if (left < 10) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        icon = Icons.Filled.CalendarToday,
                        label = "Expired Date",
                        value = formatDate(medicine?.expiredDate),
                        valueColor = getExpiryColor(medicine?.expiredDate)
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        icon = Icons.Filled.Restaurant,
                        label = "When to Take",
                        value = if (medicine?.afterEat == true) "After Eating" else "Before Eating",
                        valueColor = if (medicine?.afterEat == true) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        icon = Icons.Filled.Flag,
                        label = "Priority",
                        value = "${(medicine?.priority ?: 0f).toInt()} / 10",
                        valueColor = getPriorityColor(medicine?.priority ?: 0f)
                    )

                    medicine?.remark?.takeIf { it.isNotEmpty() }?.let { remark ->
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            icon = Icons.Filled.Description,
                            label = "Remark",
                            value = remark,
                            isMultiline = true
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        icon = Icons.Filled.Create,
                        label = "Created Date",
                        value = formatDate(medicine?.createDate)
                    )

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                button(
                    modifier = Modifier.weight(1f),
                    text = " Edit",
                    onClick = onEditClick
                )

                button(
                    modifier = Modifier.weight(1f),
                    text = " Back",
                    onClick = onBackClick
                )
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.secondary,
    isMultiline: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (isMultiline) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

