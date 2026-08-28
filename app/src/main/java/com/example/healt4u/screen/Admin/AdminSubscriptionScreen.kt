package com.example.healt4u.screen.Admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AppBlue = Color(0xFF3779EE)

data class SubscriptionPlan(
    val id: Int,
    val name: String,
    val price: Double,
    val features: List<String>,
    val subscriberCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSubscriptionScreen(onBack: () -> Unit) {
    val plans = remember {
        listOf(
            SubscriptionPlan(1, "Free", 0.0, listOf("Basic medicine tracking", "1 reminder per day"), 150),
            SubscriptionPlan(2, "Premium", 9.99, listOf("Unlimited reminders", "Doctor chat", "Family mode"), 85),
            SubscriptionPlan(3, "Enterprise", 29.99, listOf("All Premium features", "Multiple admin accounts", "Priority support"), 12)
        )
    }

    val totalRevenue = plans.sumOf { it.price * it.subscriberCount }
    val totalSubscribers = plans.sumOf { it.subscriberCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Subscription Management",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF101820))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Total Revenue",
                        value = "$${String.format("%.2f", totalRevenue)}",
                        icon = Icons.Default.AttachMoney,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Subscribers",
                        value = totalSubscribers.toString(),
                        icon = Icons.Default.Person,
                        color = AppBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text("Subscription Plans", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF101820))
            }

            items(plans) { plan ->
                SubscriptionCard(plan)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF101820))
            Text(title, fontSize = 11.sp, color = Color(0xFF61717D))
        }
    }
}

@Composable
private fun SubscriptionCard(plan: SubscriptionPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    if (plan.price == 0.0) "Free" else "$${plan.price}/mo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AppBlue
                )
            }

            Spacer(Modifier.height(8.dp))

            plan.features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(feature, fontSize = 12.sp, color = Color(0xFF61717D))
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${plan.subscriberCount} subscribers", fontSize = 12.sp, color = Color(0xFF61717D))
                Icon(Icons.Default.Person, null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(16.dp))
            }
        }
    }
}
