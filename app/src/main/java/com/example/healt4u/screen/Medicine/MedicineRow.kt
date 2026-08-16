package com.example.healt4u.screen.Medicine

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.R
import com.example.healt4u.data.MedicineData
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.componentUI.Theme.colorTheme
@Composable
fun MedicineRow(med: Medicine, onClick: () -> Unit) {
    colorTheme {
        val progress = if (med.quantity > 0) {
            (med.quantityLeft?.toFloat() ?: med.quantity.toFloat()) / med.quantity.toFloat()
        } else 0f

        val progressColor = when {
            progress <= 0.2f -> Color.Red
            progress <= 0.5f -> Color(0xFFFF9800)
            else -> Color(0xFF4CAF50)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { onClick() }
                .shadow(3.dp),

            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top row: Name, Priority, and Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Medicine Name with priority indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Priority indicator
                        if (med.priority != null && med.priority > 0) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        when (med.priority) {
                                            0 -> Color.Green
                                            1 -> Color.Yellow
                                            2 -> Color.Yellow
                                            3 -> Color(0xFFFF9800)
                                            else -> Color.Red
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }

                        Text(
                            text = med.name_medicine,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = getCategoryColor(med.category),
                        modifier = Modifier
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(
                            text = MedicineData.getCategoryName(med.category),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar Section
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.inventory),
                                contentDescription = "Quantity",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${med.quantityLeft}/${med.quantity}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Percentage text
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = progressColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Custom Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            progressColor,
                                            progressColor.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Middle row: Dosage and Eating indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dosage
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.dosage),
                            contentDescription = "Dosage",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "${med.dosage} mg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Eating indicator - BIGGER ICON
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = if (med.afterEat == true) {
                                painterResource(R.drawable.eatafter)
                            } else {
                                painterResource(R.drawable.eatbefore)
                            },
                            contentDescription = if (med.afterEat == true) "After Eat" else "Before Eat",
                            modifier = Modifier.size(50.dp),
                            tint = if (med.afterEat == true) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            }
                        )
                        Text(
                            text = if (med.afterEat == true) "After meal" else "Before meal",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (med.afterEat == true) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }

                    // Low Stock Warning
                    if (med.quantityLeft != null && med.quantityLeft <= 5) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "⚠",
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Low Stock",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row: Expiry Date and Remark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Expiry Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.expireddate),
                            contentDescription = "Expiry Date",
                            modifier = Modifier.size(14.dp),
                            tint = if (isExpired(med.expiredDate)) Color.Red else MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = formatDate(med.expiredDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExpired(med.expiredDate)) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isExpired(med.expiredDate)) FontWeight.Bold else FontWeight.Normal
                        )

                        // Expiry warning badge
                        if (isExpiringSoon(med.expiredDate)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFFF3E0)
                            ) {
                                Text(
                                    text = "⚠ Expiring soon!",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = Color(0xFFFF9800),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Remark (if exists)
                    if (!med.remark.isNullOrEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = med.remark,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}




@Preview(showBackground = true, name = "Medicine Row States")
@Composable
fun PreviewMedicineRowStates() {
    colorTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Normal - After Eat
            MedicineRow(
                Medicine(
                    id = 1,
                    name_medicine = "Paracetamol 500mg",
                    category = "A",
                    dosage = 500,
                    quantity = 100,
                    quantityLeft = 50,
                    remark = "Take with food",
                    expiredDate = parseDateToLong("15-12-2026"),
                    afterEat = true,
                    priority = 1
                ),
                onClick = {}
            )

            // Before Eat
            MedicineRow(
                Medicine(
                    id = 2,
                    name_medicine = "Vitamin C 1000mg",
                    category = "N",
                    dosage = 1000,
                    quantity = 20,
                    quantityLeft = 15,
                    remark = null,
                    expiredDate = parseDateToLong("01-01-2027"),
                    afterEat = false,
                    priority = 0
                ),
                onClick = {}
            )

            // Low Stock + Expiring Soon
            MedicineRow(
                Medicine(
                    id = 3,
                    name_medicine = "Amoxicillin 250mg",
                    category = "X",
                    dosage = 250,
                    quantity = 30,
                    quantityLeft = 3,
                    remark = "Expiring soon!",
                    expiredDate = parseDateToLong("01-09-2026"),
                    afterEat = true,
                    priority = 2
                ),
                onClick = {}
            )

            // Expired
            MedicineRow(
                Medicine(
                    id = 4,
                    name_medicine = "Expired Medicine",
                    category = "T",
                    dosage = 200,
                    quantity = 10,
                    quantityLeft = 10,
                    remark = "Do not use",
                    expiredDate = parseDateToLong("01-01-2024"),
                    afterEat = false,
                    priority = 0
                ),
                onClick = {}
            )
        }
    }
}
