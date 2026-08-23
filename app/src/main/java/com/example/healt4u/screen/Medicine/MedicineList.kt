package com.example.healt4u.screen.Medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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

@Composable
fun MedicineListScreen(
    vm: ViewModelMedicine = viewModel(),
    onAddClick: () -> Unit,
    onDel: (Medicine) -> Unit,
    onEdit: (Medicine) -> Unit,
    onClickRow: (Medicine) -> Unit,
    onCloudSync :()->Unit,
    onUploadToCloud :()->Unit

    ) {
    val medicines by vm.medicines.collectAsStateWithLifecycle()
    val isPendingDel = remember { mutableStateListOf<Medicine>() }
    var searchQuery by remember { mutableStateOf("") }

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
                Text(
                    text = "All Medicines",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCloudSync,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = "Sync from Cloud",
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text("Upload", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 30.dp)
                        )

                    }

                    IconButton(
                        onClick = onUploadToCloud,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = "Upload to Cloud",
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text("Upload", style = MaterialTheme.typography.labelSmall, color =MaterialTheme.colorScheme.secondary,modifier = Modifier.padding(top = 30.dp)
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

            // Medicine List
            if (filteredMedicines.isEmpty()) {
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
                modifier = Modifier.size(56.dp)
            )
        }

        isPendingDel?.let {
            it.forEach { med ->
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
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.onError,
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary
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
                                contentColor = MaterialTheme.colorScheme.onBackground,
                                disabledContainerColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("CANCEL", color = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    title = { Text("Delete Medicine") },
                    text = { Text("Do you want to delete ${med.name_medicine}?") }
                )
            }
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
            color =MaterialTheme.colorScheme.onBackground
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

@Preview(showBackground = true)
@Composable
fun PreviewMedicineListScreen() {

}