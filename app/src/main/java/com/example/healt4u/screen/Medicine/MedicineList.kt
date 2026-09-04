package com.example.healt4u.screen.Medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healt4u.ViewModel.ViewModelMedicine
import com.example.healt4u.data.local.getMedicines_ByPatientId
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.screen.componentUI.button
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListScreen(
    vm: ViewModelMedicine = viewModel(),
    onAddClick: () -> Unit,
    onDel: (Medicine) -> Unit,
    onEdit: (Medicine) -> Unit,
    onClickRow: (Medicine) -> Unit,
    onCloudSync: () -> Unit,
    onUploadToCloud: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val medicines by vm.medicines.collectAsStateWithLifecycle()
    val isPendingDel = remember { mutableStateListOf<Medicine>() }
    var searchQuery by remember { mutableStateOf("") }

    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val successMessage by vm.successMessage.collectAsStateWithLifecycle()

    // ===== STOCK DIALOG STATE =====
    var showStockDialog by remember { mutableStateOf(false) }
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    var isUpdatingStock by remember { mutableStateOf(false) }

    // ===== HANDLE STOCK UPDATE =====
    fun handleStockUpdate(newQuantity: Int) {
        selectedMedicine?.let { medicine ->
            isUpdatingStock = true
            vm.updateStock(
                medicine = medicine,
                newQuantityLeft = newQuantity,
                context = context,
                onSuccess = {
                    isUpdatingStock = false
                    showStockDialog = false
                    selectedMedicine = null
                }
            )
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            delay(3000)
            vm.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(3000)
            vm.clearSuccess()
        }
    }

    val filteredMedicines by remember {
        derivedStateOf {
            if (searchQuery.isEmpty()) {
                medicines
            } else {
                medicines.filter { medicine ->
                    medicine.name_medicine.contains(searchQuery, ignoreCase = true) ||
                            medicine.category.contains(searchQuery, ignoreCase = true) ||
                            medicine.remark?.contains(searchQuery, ignoreCase = true) == true
                }
            }
        }
    }

    colorTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "All Medicines",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .padding(end=48.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBack?.invoke() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Sync button
                        IconButton(
                            onClick = onCloudSync,
                            enabled = !isLoading
                        ) {
                            Icon(
                                Icons.Default.Cloud,
                                contentDescription = "Sync",
                                tint = Color.White
                            )
                        }
                        // Upload button
                        IconButton(
                            onClick = onUploadToCloud,
                            enabled = !isLoading
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                tint = Color.White
                            )
                        }
                        // Count badge
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "${medicines.size}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White,
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Medicine",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                // Search bar
                SearchMedicineScreen(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it }
                )

                Spacer(Modifier.height(4.dp))

                // Content
                if (isLoading && medicines.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading medicines...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                } else if (filteredMedicines.isEmpty() && searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No results found",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Try a different search term",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else if (filteredMedicines.isEmpty()) {
                    EmptyStateView()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMedicines) { medicine ->
                            MedicineRow(
                                med = medicine,
                                onDel = { med -> isPendingDel.add(med) },
                                onClick = { med -> onClickRow(med) },
                                onEdit = { med -> onEdit(med) },
                                onChangeStock = { med ->
                                    selectedMedicine = med
                                    showStockDialog = true
                                }
                            )
                        }

                        if (isLoading && medicines.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ===== ERROR MESSAGE =====
            error?.let {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 80.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { vm.clearError() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ===== SUCCESS MESSAGE =====
            successMessage?.let {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 80.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = successMessage!!,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { vm.clearSuccess() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ===== DELETE CONFIRMATION DIALOG =====
        isPendingDel.firstOrNull()?.let { med ->
            AlertDialog(
                onDismissRequest = { isPendingDel.clear() },
                confirmButton = {
                    Button(
                        onClick = {
                            onDel(med)
                            isPendingDel.clear()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = Color.White
                        )
                    ) {
                        Text("DELETE")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { isPendingDel.clear() }
                    ) {
                        Text("CANCEL")
                    }
                },
                title = {
                    Text(
                        "Delete Medicine",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("Do you want to delete ${med.name_medicine}?")
                }
            )
        }

        // ===== LOADING DIALOG =====
        if (isLoading) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {},
                dismissButton = {},
                title = {
                    Text(
                        text = "Syncing...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(72.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            strokeWidth = 6.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Please wait...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        var dotCount by remember { mutableStateOf(0) }

                        LaunchedEffect(Unit) {
                            while (true) {
                                delay(500)
                                dotCount = (dotCount + 1) % 4
                            }
                        }

                        Text(
                            text = ".".repeat(dotCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
        }

        // ===== STOCK UPDATE DIALOG =====
        if (showStockDialog && selectedMedicine != null) {
            StockUpdateDialog(
                medicine = selectedMedicine!!,
                onDismiss = {
                    if (!isUpdatingStock) {
                        showStockDialog = false
                        selectedMedicine = null
                    }
                },
                onUpdate = { newQuantity ->
                    handleStockUpdate(newQuantity)
                },
                isLoading = isUpdatingStock
            )
        }
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Medication,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Medicines Found",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to add your first medicine",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}
