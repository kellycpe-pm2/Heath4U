package com.example.healt4u.screen.Medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.R
import com.example.healt4u.data.MedicineData
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.componentUI.Theme.colorTheme
@Composable
fun MedicineRow(med: Medicine, onClick: (Medicine) -> Unit, onDel: (Medicine) -> Unit, onEdit: (Medicine) -> Unit,onChangeStock :(Int)-> Unit={}) {
    var expand by remember{ mutableStateOf(false) }
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
                .clickable { onClick(med) }
                .shadow(3.dp)
            ,

            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp).pointerInput(Unit){
                        detectTapGestures (
                            onTap = {onClick(med)},
                            onLongPress = {
                                expand = true
                            })
                    }
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.End){
                    IconButton(onClick={expand = true}) {
                        Icon(Icons.Filled.MoreVert,"Left",tint=MaterialTheme.colorScheme.secondary)
                    }
                }

                DropdownMenu(
                    expanded = expand,
                    onDismissRequest = {expand = false},
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            RoundedCornerShape(20.dp)
                        ).padding(30.dp)


                ) {

                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.onBackground,style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = {Icon(Icons.Filled.Delete,"list",tint= MaterialTheme.colorScheme.secondary) },
                        modifier= Modifier.padding(15.dp),
                        onClick = {
                            onDel(med)
                            expand = false }

                    )
                    Divider()

                    DropdownMenuItem(
                        text = { Text("Edit",color = MaterialTheme.colorScheme.onBackground,style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = {Icon(Icons.Filled.Edit,"list",tint= MaterialTheme.colorScheme.secondary) },
                        modifier= Modifier.padding(15.dp),

                        onClick = {
                            onEdit(med)
                            expand = false }
                    )

                    Divider()

                    DropdownMenuItem(
                        text = { Text("Update The Stack",color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.labelMedium)  },
                        leadingIcon = {Icon(Icons.Filled.Inventory,"stock",tint= MaterialTheme.colorScheme.secondary) },
                        modifier= Modifier.padding(15.dp),
                        onClick = {
                            onChangeStock(med.quantityLeft!!)
                            expand = false }
                    )

                    Divider()

                    DropdownMenuItem(
                        text = { Text("View More",color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.labelMedium)  },
                        leadingIcon = {Icon(Icons.Filled.List,"list",tint= MaterialTheme.colorScheme.secondary) },
                        modifier= Modifier.padding(15.dp),
                        onClick = {
                            onClick(med)
                            expand = false }
                    )





                }
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
                        if (med.priority != null && med.priority > 0f) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        when (med.priority) {
                                            1f -> Color.Green
                                            2f -> Color(0xFFFF9800)
                                            3f -> Color(0xFFB92D16)
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
                    horizontalArrangement = Arrangement.Start,
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


                    }

                // Eating indicator - BIGGER ICON
                Row(
                    modifier = Modifier.fillMaxWidth(),
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




