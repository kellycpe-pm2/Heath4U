package com.example.healt4u.screen.ScanScreen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.model.NPRAMedicine
import com.example.healt4u.model.toDisplayModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResult(
    barcode: String,
    medicine: NPRAMedicine?,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onAddToReminder: (NPRAMedicine) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("药品详情", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (medicine != null) {
                        IconButton(onClick = { onAddToReminder(medicine) }) {
                            Icon(Icons.Default.Alarm, contentDescription = "添加提醒")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在加载...")
                    }
                }
                errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❌", fontSize = 48.sp)
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBack) { Text("返回") }
                    }
                }
                medicine != null -> {
                    MedicineDetailContent(medicine = medicine)
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Text("未找到药品信息", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text("条形码: $barcode", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = onBack) { Text("返回") }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineDetailContent(medicine: NPRAMedicine) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(medicine.product, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        item {
            medicine.genericName?.let {
                Text(it, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "MAL: ${medicine.regNo}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item { Divider() }

        item {
            InfoCard("基本信息", listOf(
                "注册号" to medicine.regNo,
                "通用名" to (medicine.genericName ?: "未提供"),
                "状态" to (medicine.status ?: "未提供")
            ))
        }

        item {
            InfoCard("生产商信息", listOf(
                "持有者" to (medicine.holder ?: "未提供"),
                "生产商" to (medicine.manufacturer ?: "未提供")
            ))
        }

        item {
            medicine.activeIngredient?.let {
                InfoCard("成分信息", listOf(
                    "有效成分" to it,
                    "MDC代码" to (medicine.mdcCode ?: "未提供")
                ))
            }
        }

        item {
            InfoCard("注册信息", listOf(
                "注册日期" to (medicine.dateReg ?: "未提供"),
                "有效期" to (medicine.dateEnd ?: "未提供")
            ))
        }

        item {
            Text(
                text = "⚠️ 本信息仅供参考，请咨询医生或药师",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun InfoCard(title: String, items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Divider()
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Text(
                        value,
                        fontSize = 14.sp,
                        fontWeight = if (value == "未提供") null else FontWeight.Medium
                    )
                }
            }
        }
    }
}
