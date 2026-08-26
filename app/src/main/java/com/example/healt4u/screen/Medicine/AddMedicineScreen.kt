package com.example.healt4u.screen.Medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healt4u.R
import com.example.healt4u.ViewModel.ViewModelMedicine
import com.example.healt4u.data.MedicineData
import com.example.healt4u.screen.componentUI.*
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import kotlinx.coroutines.delay

@Composable
fun AddMedicineScreen(
    vm: ViewModelMedicine = viewModel(),
    onAddClick: () -> Unit,
    onBack: () -> Unit  // ← ADDED onBack parameter
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val error by vm.error.collectAsStateWithLifecycle()
    val success by vm.success.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val validationErrors by vm.validationErrors.collectAsStateWithLifecycle()

    // ========== FORM FIELDS ==========
    val medName by vm.input_med_name.collectAsStateWithLifecycle()
    val medCategory by vm.input_category.collectAsStateWithLifecycle()
    val medDosage by vm.input_dosage.collectAsStateWithLifecycle()
    val medQuantity by vm.input_quantity.collectAsStateWithLifecycle()
    val medExpiredDate by vm.input_ExpiredDate.collectAsStateWithLifecycle()
    val medIsBeforeEating by vm.input_afterEat.collectAsStateWithLifecycle()
    val medPriority by vm.input_priority.collectAsStateWithLifecycle()
    val medRemark by vm.input_remark.collectAsStateWithLifecycle()
    val medReminderTime by vm.input_reminderTime.collectAsStateWithLifecycle()
    val medTimesPerDay by vm.input_timesPerDay.collectAsStateWithLifecycle()


    LaunchedEffect(error) {
        if (error != null) {
            delay(3000)
            vm.clearError()
        }
    }


    colorTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add Medicine",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(25.dp))

            Column {
                TextFieldInput(
                    medName,
                    vm::on_Med_Name_Change,
                    "Medicine Name *",
                    Modifier.fillMaxWidth(),
                    false,
                    singleLine = true
                )
                validationErrors["name"]?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                dropDownMenu(
                    Modifier.fillMaxSize(),
                    medCategory,
                    MedicineData.categories.map { it.second },
                    "Category *",
                    vm::on_Category_Change
                )
                validationErrors["category"]?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }


            Column {
                NumericStepper(
                    1,
                    10000,
                    medDosage,
                    50,
                    vm::on_Dos_Change
                )
                validationErrors["dosage"]?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            Column {
                NumericStepper(
                    0,
                    1000,
                    medQuantity,
                    1,
                    vm::on_Quantity_Change
                )
                validationErrors["quantity"]?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            Column {
                DatePickerPopupOnClick(
                    modifier = Modifier.fillMaxWidth(),
                    "Expired Date *",
                    value = medExpiredDate,
                    vm::on_ExpiredDate_Change
                )
                validationErrors["expiredDate"]?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimingButton(
                    text = "Before Eating",
                    isSelected = medIsBeforeEating,
                    id = R.drawable.eatbefore,
                    onClick = { vm.on_AfterEat_Change(true) }
                )

                Spacer(Modifier.width(16.dp))

                TimingButton(
                    text = "After Eating",
                    isSelected = !medIsBeforeEating,
                    id = R.drawable.eatafter,
                    onClick = { vm.on_AfterEat_Change(false) }
                )
            }

            // ========== REMINDER TIME (first dose of the day) ==========
            Text(
                text = "First reminder time",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            )


            // ========== TIMES PER DAY ==========
            Text(
                text = "Times per day: $medTimesPerDay",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
            )
            NumericStepper(
                1,
                6,
                medTimesPerDay,
                1,
                vm::on_TimesPerDay_Change
            )

            slider(
                medPriority,
                vm::on_Priority_Change
            )

            TextFieldInput(
                medRemark,
                vm::on_Remark_Change,
                "Remark",
                Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                false,
                singleLine = false
            )

            button(
                modifier = Modifier.fillMaxWidth(),
                text = if (isLoading) "Adding..." else "Submit",
                onClick = {
                    vm.addMedicineWithValidation(context)
                },
                enabled = !isLoading
            )

            button(
                modifier = Modifier.fillMaxWidth(),
                text = "Cancel",
                onClick = onBack,
                enabled = !isLoading
            )

            error?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = it,
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


            if (isLoading) {
                AlertDialog(
                    onDismissRequest = { },
                    confirmButton = {},
                    dismissButton = {},
                    title = {
                        Text(
                            text = "Add...",
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
}
