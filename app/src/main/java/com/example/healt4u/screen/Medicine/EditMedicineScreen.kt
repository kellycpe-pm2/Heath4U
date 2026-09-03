package com.example.healt4u.screen.Medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicineScreen(
    medicine: Medicine,
    onEdit: (Medicine) -> Unit,
    onBack: () -> Unit,
    vm: ViewModelMedicine = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val error by vm.error.collectAsStateWithLifecycle()
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

    var selectedPage by remember { mutableStateOf(0) }

    colorTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Edit Medicine",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onPrimary)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {

                // ========== TAB BUTTONS ==========
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedPage = 0 },
                        colors = if (selectedPage == 0) ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) else ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = if (selectedPage == 0) null else ButtonDefaults.outlinedButtonBorder,
                        modifier = Modifier.weight(1f)
                    ) {
                        val textColor = if (selectedPage == 0) Color.White else MaterialTheme.colorScheme.secondary
                        Text("Medicine Details", color = textColor)
                    }

                    Button(
                        onClick = { selectedPage = 1 },
                        colors = if (selectedPage == 1) ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) else ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = if (selectedPage == 1) null else ButtonDefaults.outlinedButtonBorder,
                        modifier = Modifier.weight(1f)
                    ) {
                        val textColor = if (selectedPage == 1) Color.White else MaterialTheme.colorScheme.secondary
                        Text("Reminder Settings", color = textColor)
                    }
                }

                // ========== FORM CONTENT ==========
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {

                    Spacer(Modifier.padding(10.dp))
                    Divider()
                    Spacer(Modifier.padding(10.dp))

                    when (selectedPage) {
                        0 -> {
                            // ========== MEDICINE NAME ==========
                            Column {
                                Text(
                                    text = "Medicine Name",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 1.dp, top = 4.dp)
                                )
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
                                Text(
                                    text = "Category",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 1.dp, top = 4.dp)
                                )
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
                                Text(
                                    text = "Dosage (MG)",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 1.dp, top = 4.dp)
                                )
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
                                Text(
                                    text = "Quantity",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 1.dp, top = 4.dp)
                                )
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
                            Text(
                                text = "Expired Date",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 1.dp, top = 4.dp)
                            )
                            Column {
                                DatePickerPopupOnClick(
                                    modifier = Modifier.fillMaxWidth(),
                                    label = "Expired Date *",
                                    value = medExpiredDate,
                                    onDateChange = { value ->
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

                            // ========== REMARK ==========
                            Text(
                                text = "Remark",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 1.dp, top = 4.dp)
                            )
                            TextFieldInput(
                                medRemark,
                                { value -> medRemark = value },
                                "Remark",
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                false,
                                singleLine = false
                            )

                            Spacer(Modifier.height(8.dp))

                            // ========== NEXT BUTTON ==========
                            button(
                                onClick = { selectedPage = 1 },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                text = "Next"
                            )
                        }

                        1 -> {
                            // ========== REMINDER TIME ==========
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
                            Text(
                                text = "Priority",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 1.dp, top = 4.dp)
                            )
                            Spacer(Modifier.padding(6.dp))
                            slider(
                                medPriority,
                                { value -> medPriority = value }
                            )

                            Spacer(Modifier.padding(10.dp))

                            // ========== BEFORE/AFTER EATING ==========
                            Text(
                                text = "When Eat ? ",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 1.dp, top = 4.dp)
                            )
                            Spacer(Modifier.padding(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TimingButton(
                                    text = "Before Eating",
                                    isSelected = medIsBeforeEating,
                                    id = R.drawable.eatbefore,
                                    onClick = { medIsBeforeEating = true }
                                )

                                TimingButton(
                                    text = "After Eating",
                                    isSelected = !medIsBeforeEating,
                                    id = R.drawable.eatafter,
                                    onClick = { medIsBeforeEating = false }
                                )
                            }

                            Spacer(Modifier.height(8.dp))
                            Spacer(Modifier.height(8.dp))


                            // ========== ERROR CARD ==========
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

                            // ========== SUBMIT BUTTON ==========
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
                                        reminderTime = medReminderTime,
                                        timesPerDay = medTimesPerDay,
                                        patientId = com.example.healt4u.Session.CurrentSession.patientId
                                    )
                                    onEdit(updatedMedicine)
                                },
                                enabled = !isLoading
                            )

                            // ========== RESET BUTTON ==========
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
                                },
                                enabled = !isLoading
                            )

                            // ========== CANCEL BUTTON ==========
                            button(
                                modifier = Modifier.fillMaxWidth(),
                                text = "Back",
                                onClick = onBack,
                                enabled = !isLoading
                            )

                            // ========== LOADING DIALOG ==========
                            if (isLoading) {
                                AlertDialog(
                                    onDismissRequest = { },
                                    confirmButton = { },
                                    dismissButton = { },
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
                        }
                    }
                }
            }
        }
    }
}