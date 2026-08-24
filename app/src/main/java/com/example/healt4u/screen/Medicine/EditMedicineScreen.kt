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
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.componentUI.*
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import kotlinx.coroutines.delay

@Composable
fun EditMedicineScreen(
    medicine: Medicine,
    onEdit: (Medicine) -> Unit,
    onBack: () -> Unit,
    vm: ViewModelMedicine = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val error by vm.error.collectAsStateWithLifecycle()
    val success by vm.success.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val validationErrors by vm.validationErrors.collectAsStateWithLifecycle()

    // ========== STATE VARIABLES ==========
    var medName by remember { mutableStateOf(medicine.name_medicine) }
    var medCategory by remember { mutableStateOf(medicine.category) }
    var medDosage by remember { mutableStateOf(medicine.dosage) }
    var medQuantity by remember { mutableStateOf(medicine.quantity) }
    var medExpiredDate by remember { mutableStateOf(medicine.expiredDate) }
    var medIsBeforeEating by remember { mutableStateOf(medicine.afterEat ?: true) }
    var medPriority by remember { mutableStateOf(medicine.priority ?: 1f) }
    var medRemark by remember { mutableStateOf(medicine.remark ?: "") }
    var medReminderTime by remember { mutableStateOf(medicine.reminderTime ?: "08:00") }
    var medTimesPerDay by remember { mutableStateOf(medicine.timesPerDay ?: 1) }

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
                text = "Edit Medicine",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(25.dp))

            // ========== MEDICINE NAME ==========
            Column {
                TextFieldInput(
                    medName,
                    { value ->
                        medName = value
                        vm.clearFieldError("name")
                    },
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

            // ========== CATEGORY ==========
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
                    { value ->
                        medCategory = value
                        vm.clearFieldError("category")
                    }
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

            // ========== DOSAGE ==========
            Column {
                NumericStepper(
                    1,
                    10000,
                    medDosage,
                    50,
                    { value ->
                        medDosage = value
                        vm.clearFieldError("dosage")
                    }
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

            // ========== QUANTITY ==========
            Column {
                NumericStepper(
                    0,
                    1000,
                    medQuantity,
                    1,
                    { value ->
                        medQuantity = value
                        vm.clearFieldError("quantity")
                    }
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

            // ========== EXPIRED DATE ==========
            Column {
                DatePickerPopupOnClick(
                    modifier = Modifier.fillMaxWidth(),
                    "Expired Date *",
                    value = medExpiredDate,
                    { value ->
                        medExpiredDate = value
                        vm.clearFieldError("expiredDate")
                    }
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

            // ========== BEFORE/AFTER EATING ==========
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
                    onClick = { medIsBeforeEating = true }
                )

                Spacer(Modifier.width(16.dp))

                TimingButton(
                    text = "After Eating",
                    isSelected = !medIsBeforeEating,
                    id = R.drawable.eatafter,
                    onClick = { medIsBeforeEating = false }
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
            TimePickerPopupOnClick(
                modifier = Modifier.fillMaxWidth(),
                label = "Reminder Time *",
                value = medReminderTime,
                onTimeChange = { value -> medReminderTime = value }
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
                { value -> medTimesPerDay = value }
            )

            // ========== PRIORITY ==========
            slider(
                medPriority,
                { value ->
                    medPriority = value
                }
            )

            // ========== REMARK ==========
            TextFieldInput(
                medRemark,
                { value ->
                    medRemark = value
                },
                "Remark",
                Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                false,
                singleLine = false
            )

            button(
                modifier = Modifier.fillMaxWidth(),
                text = if (isLoading) "Updating..." else "Submit",
                onClick = {
                    val updatedMedicine = Medicine(
                        id = medicine.id,
                        name_medicine = medName,
                        category = medCategory,
                        dosage = medDosage,
                        quantity = medQuantity,
                        quantityLeft = medicine.quantityLeft,
                        remark = medRemark,
                        expiredDate = medExpiredDate,
                        afterEat = medIsBeforeEating,
                        createDate = medicine.createDate,
                        priority = medPriority,
                        ic = medicine.ic,
                        reminderTime = medReminderTime,
                        timesPerDay = medTimesPerDay
                    )
                    onEdit(updatedMedicine)
                },
                enabled = !isLoading
            )

            button(
                modifier = Modifier.fillMaxWidth(),
                text = "Reset Changes",
                onClick = {
                    medName = medicine.name_medicine
                    medCategory = medicine.category
                    medDosage = medicine.dosage
                    medQuantity = medicine.quantity
                    medExpiredDate = medicine.expiredDate
                    medIsBeforeEating = medicine.afterEat ?: true
                    medPriority = medicine.priority ?: 1f
                    medRemark = medicine.remark ?: ""
                    medReminderTime = medicine.reminderTime ?: "08:00"
                    medTimesPerDay = medicine.timesPerDay ?: 1
                    vm.clearValidationErrors()
                }
            )

            // ========== CANCEL BUTTON ==========
            button(
                modifier = Modifier.fillMaxWidth(),
                text = "Cancel",
                onClick = onBack
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
                            text = "Update...",
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
