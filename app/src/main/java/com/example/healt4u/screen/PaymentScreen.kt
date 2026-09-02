package com.example.healt4u.screen.Payment

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.healt4u.model.Payment
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val PAYMENTS_FILE = "payments.json"

fun savePayments(context: android.content.Context, payments: List<Payment>) {
    try {
        val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
        val jsonString = json.encodeToString(payments)
        context.openFileOutput(PAYMENTS_FILE, android.content.Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    } catch (e: Exception) { e.printStackTrace() }
}

fun loadPayments(context: android.content.Context): List<Payment> {
    val file = File(context.filesDir, PAYMENTS_FILE)
    if (!file.exists()) return emptyList()
    return try {
        Json { ignoreUnknownKeys = true }.decodeFromString<List<Payment>>(file.readText())
    } catch (e: Exception) { emptyList() }
}

fun addPayment(context: android.content.Context, payment: Payment): Boolean {
    return try {
        val list = loadPayments(context).toMutableList()
        list.add(payment)
        savePayments(context, list)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    patientId: Int,
    doctorId: Int,
    doctorName: String,
    onBack: () -> Unit,
    onPaymentComplete: () -> Unit
) {
    val context = LocalContext.current
    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("TnG") }
    val date = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val time = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

    val tngPackage = "com.touchngo.touchngowallet"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay to Dr. $doctorName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (RM)") },
                prefix = { Text("RM ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Select Payment Method", style = MaterialTheme.typography.titleMedium)

            Card(
                onClick = { selectedMethod = "TnG" },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMethod == "TnG")
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedMethod == "TnG", onClick = { selectedMethod = "TnG" })
                    Text("Touch 'n Go eWallet", Modifier.padding(start = 12.dp),
                        style = MaterialTheme.typography.bodyLarge)
                }
            }

            Card(
                onClick = { selectedMethod = "FPX" },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMethod == "FPX")
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedMethod == "FPX", onClick = { selectedMethod = "FPX" })
                    Text("FPX — Online Banking", Modifier.padding(start = 12.dp),
                        style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        Toast.makeText(context, "Please enter valid amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val paymentDesc = "Payment to Dr. $doctorName — RM $amount"

                    if (selectedMethod == "TnG") {
                        val tngUri = Uri.parse("tngwallet://payment?amount=$amount")
                        val intent = Intent(Intent.ACTION_VIEW, tngUri)
                        intent.setPackage(tngPackage)
                        try {
                            context.startActivity(intent)
                            Toast.makeText(context, "Opening Touch 'n Go eWallet...\n\nPay RM $amount then tap below", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Touch 'n Go not installed!\nPlease install from Play Store", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                    } else {
                        val fpxUri = Uri.parse("https://www.fpx.com.my")
                        context.startActivity(Intent(Intent.ACTION_VIEW, fpxUri))
                        Toast.makeText(context, "✅ Opening FPX Online Banking...\n\nPay RM $amount then tap below", Toast.LENGTH_LONG).show()
                    }

                    val payment = Payment(
                        id = "pay_${System.currentTimeMillis()}",
                        patientId = patientId,
                        doctorId = doctorId,
                        doctorName = doctorName,
                        amount = amount,
                        date = date,
                        time = time,
                        status = "PENDING",
                        paymentMethod = selectedMethod
                    )
                    addPayment(context, payment)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = amountText.isNotBlank()
            ) {
                Text("Open $selectedMethod to Pay")
            }

            Button(
                onClick = {
                    val payments = loadPayments(context).toMutableList()
                    val latest = payments.lastOrNull { it.status == "PENDING" && it.paymentMethod == selectedMethod }
                    if (latest != null) {
                        val updated = latest.copy(status = "PAID")
                        payments[payments.indexOf(latest)] = updated
                        savePayments(context, payments)
                        Toast.makeText(context, "Payment Marked as PAID!\nAmount: RM ${latest.amount}", Toast.LENGTH_LONG).show()
                        onPaymentComplete()
                    } else {
                        Toast.makeText(context, "No pending payment found", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("I Have Completed Payment")
            }
        }
    }
}