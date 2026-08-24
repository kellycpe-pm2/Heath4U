package com.example.healt4u.screen.Medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
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
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.screen.componentUI.button
import kotlinx.coroutines.delay

@Composable
fun MedicineListScreen(
    vm: ViewModelMedicine = viewModel(),
    onAddClick: () -> Unit,
    onDel: (Medicine) -> Unit,
    onEdit: (Medicine) -> Unit,
    onClickRow: (Medicine) -> Unit,
    onCloudSync : () -> Unit,
    onUploadToCloud : () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val medicines by vm.medicines.collectAsStateWithLifecycle()
    val isPendingDel = remember { mutableStateListOf<Medicine>() }
    var searchQuery by remember { mutableStateOf("") }

    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val successMessage by vm.successMessage.collectAsStateWithLifecycle()

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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        onBack?.let {
                            IconButton(onClick = it) {
                                Icon(
                                    androidx.compose.material.icons.Icons.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        Text(
                            text = "All Medicines",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onCloudSync,
                            modifier = Modifier.size(48.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {

                            } else {
                                Icon(
                                    Icons.Default.Cloud,
                                    contentDescription = "Sync",
                                    modifier = Modifier.size(30.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Text(
                                if (isLoading) "Syncing..." else "Sync",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(top = 30.dp)
                            )
                        }
                        IconButton(
                            onClick = onUploadToCloud,
                            modifier = Modifier.size(48.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                            } else {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = "Upload",
                                    modifier = Modifier.size(30.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Text(
                                if (isLoading) "Uploading..." else "Upload",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(top = 30.dp)
                            )
                        }


                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                        ) {
                            Text(
                                text = "${medicines.size}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                SearchMedicineScreen(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it }
                )

                Spacer(Modifier.height(0.5f.dp))

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
                                color = MaterialTheme.colorScheme.onBackground,
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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMedicines) { medicine ->
                            MedicineRow(
                                medicine,
                                onDel = { med -> isPendingDel.add(med) },
                                onClick = { med -> onClickRow(med) },
                                onEdit = { med -> onEdit(med) }
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
                                onClick = {
                                    vm.clearError()
                                },
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
                            containerColor = MaterialTheme.colorScheme.secondary
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
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    vm.clearSuccess()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End
            ) {
                button(
                    text = "+",
                    onClick = { onAddClick() },
                    modifier = Modifier.size(56.dp),
                    enabled = !isLoading
                )
            }
        }

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
                            containerColor = MaterialTheme.colorScheme.onError,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("DELETE", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            isPendingDel.clear()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text("CANCEL", color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                title = { Text("Delete Medicine") },
                text = { Text("Do you want to delete ${med.name_medicine}?") }
            )
        }

        if (isLoading) {
            AlertDialog(
                onDismissRequest = {  },
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

                        // Animated dots
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
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun EmptyStateView() {
    colorTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))
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
                color = MaterialTheme.colorScheme.onBackground
            )
        }


    }
}

