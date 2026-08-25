package com.example.healt4u.screen.Admin

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healt4u.ViewModel.ViewModelMedicine
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.Medicine.formatDate

private val AppBlue = Color(0xFF3779EE)
private val ScreenBlue = Color(0xFFE6F8FC)
private val SoftBlue = Color(0xFFDCEBFF)
private val AlertOrange = Color(0xFFFFA33A)


@Composable
fun AdminDashboardScreen(
    vm: ViewModelMedicine,
    onInventoryClick: () -> Unit = {},
    onUsersClick: () -> Unit = {},
    onReportsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val medicines by vm.medicines.collectAsStateWithLifecycle()
    val lowStock = medicines.filter { (it.quantityLeft ?: it.quantity) <= 5 }
    val expired = medicines.count { it.expiredDate?.let { date -> date < System.currentTimeMillis() } == true }

    Column(
        modifier = Modifier.fillMaxSize().background(ScreenBlue)
    ) {
        AdminHeader()

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Admin Dashboard",
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101820)
                )
                Text(
                    text = "Manage your health service at a glance",
                    modifier = Modifier.padding(start = 20.dp, top = 2.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF61717D)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatisticCard("Medicine", medicines.size.toString(), Icons.Default.Inventory2, SoftBlue, Modifier.weight(1f))
                    StatisticCard("Low stock", lowStock.size.toString(), Icons.Default.Warning, Color(0xFFFFE7C7), Modifier.weight(1f))
                    StatisticCard("Expired", expired.toString(), Icons.Default.CalendarMonth, Color(0xFFFFDEE0), Modifier.weight(1f))
                }
            }

            item {
                Text(
                    "Quick Management",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF101820)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAction("Inventory", Icons.Default.Inventory2, onInventoryClick, Modifier.weight(1f))
                    QuickAction("Users", Icons.Default.PersonAdd, onUsersClick, Modifier.weight(1f))
                    QuickAction("Reports", Icons.Default.Assessment, onReportsClick, Modifier.weight(1f))
                    QuickAction("Settings", Icons.Default.Settings, onSettingsClick, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Stock Alerts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF101820))
                    Text("${lowStock.size} item(s)", fontSize = 12.sp, color = AppBlue, fontWeight = FontWeight.SemiBold)
                }
            }

            if (lowStock.isEmpty()) {
                item {
                    EmptyAlertCard()
                }
            } else {
                items(lowStock.take(4), key = { it.id }) { medicine ->
                    StockAlertCard(medicine)
                }
            }
        }

        AdminBottomNavigation()
    }
}

@Composable
private fun AdminHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(38.dp).background(AppBlue, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Inventory2, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("HEALTH4U", color = AppBlue, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
            Text("ADMIN PORTAL", color = Color(0xFF63727D), fontSize = 9.sp, letterSpacing = 1.sp)
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier.size(38.dp).background(Color.White, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, "Admin profile", tint = Color(0xFF101820))
        }
    }
}

@Composable
private fun StatisticCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(11.dp)) {
            Box(Modifier.size(30.dp).background(color, RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = AppBlue, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF101820))
            Text(label, fontSize = 10.sp, color = Color(0xFF61717D), maxLines = 1)
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(vertical = 13.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = AppBlue, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 10.sp, color = AppBlue, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun StockAlertCard(medicine: Medicine) {
    val remaining = medicine.quantityLeft ?: medicine.quantity
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).background(Color(0xFFFFF0DD), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Warning, null, tint = AlertOrange)
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(medicine.name_medicine, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${remaining} of ${medicine.quantity} left • Expires ${formatDate(medicine.expiredDate)}", fontSize = 10.sp, color = Color(0xFF61717D), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("RESTOCK", fontSize = 9.sp, color = AppBlue, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun EmptyAlertCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text("All medicines have sufficient stock.", modifier = Modifier.padding(16.dp), color = Color(0xFF61717D), fontSize = 13.sp)
    }
}

@Composable
private fun AdminBottomNavigation() {
    Row(
        modifier = Modifier.fillMaxWidth().background(AppBlue).padding(vertical = 10.dp, horizontal = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BottomItem("Home", Icons.Default.Home, true)
        BottomItem("Inventory", Icons.Default.Inventory2, false)
        BottomItem("Reports", Icons.Default.Assessment, false)
        BottomItem("Profile", Icons.Default.Person, false)
    }
}

@Composable
private fun BottomItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, label, tint = if (selected) Color.White else Color(0xFFCFE0FF), modifier = Modifier.size(23.dp))
        Text(label.uppercase(), color = if (selected) Color.White else Color(0xFFCFE0FF), fontSize = 7.sp, fontWeight = FontWeight.Bold)
    }
}
