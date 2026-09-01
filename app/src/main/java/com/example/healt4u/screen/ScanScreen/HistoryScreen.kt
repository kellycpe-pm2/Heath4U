package com.example.healt4u.screen.ScanScreen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.model.NPRAMedicine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    medicines: List<NPRAMedicine>,
    onItemClick: (String) -> Unit,
    onClearHistory: () -> Unit = {}
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredMedicines = remember(medicines, searchQuery) {
        if (searchQuery.isBlank()) medicines
        else medicines.filter {
            it.product.contains(searchQuery, ignoreCase = true) ||
                    it.genericName?.contains(searchQuery, ignoreCase = true) == true ||
                    it.regNo.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("扫描历史", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    if (medicines.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "清空", tint = Color.White)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (medicines.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("搜索药品") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            if (filteredMedicines.isEmpty() && searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, modifier = Modifier.size(64.dp),contentDescription = "")
                        Text("未找到 \"$searchQuery\"", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                }
            } else if (filteredMedicines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, modifier = Modifier.size(64.dp), contentDescription = "")
                        Text("暂无扫描记录", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text("扫描药品后这里会显示历史记录", fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMedicines) { medicine ->
                        HistoryItem(medicine = medicine, onClick = { onItemClick(medicine.regNo) })
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空历史记录") },
            text = { Text("确定要清空所有扫描历史吗？") },
            confirmButton = {
                Button(
                    onClick = { onClearHistory(); showClearDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("确定清空")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
fun HistoryItem(medicine: NPRAMedicine, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    medicine.product,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("MAL: ${medicine.regNo}", fontSize = 12.sp)
                    medicine.manufacturer?.let {
                        Text("• $it", fontSize = 12.sp)
                    }
                }
                medicine.status?.let { status ->
                    Text(
                        "状态: $status",
                        fontSize = 11.sp,
                        color = when (status.lowercase()) {
                            "active" -> Color(0xFF4CAF50)
                            "expired" -> Color(0xFFF44336)
                            else -> Color(0xFFFF9800)
                        }
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "查看", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}