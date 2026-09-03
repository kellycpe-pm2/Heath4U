package com.example.healt4u.screen.Payment

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healt4u.model.Payment
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val PAYMENTS_FILE = "payments.json"

private val BluePrimary = Color(0xFF1565C0)
private val BlueLight = Color(0xFFE3F2FD)
private val BlueDark = Color(0xFF0D47A1)
private val White = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF1A1A1A)

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
    doctorId: Int,
    hospitalId: Int,
    doctorName: String,
    consultationFee: Double,
    patientId: Int,
    onPaymentSuccess: (time: Long, method: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val amount = consultationFee
    var selectedMethod by remember { mutableStateOf("TnG") }
    val date = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val time = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

    // ✅ TNG Deep Link (try app first, fallback to web)
    val tngDeepLink = "tngdwallet://client/dl/mp?mpid=123456789"
    val tngWebUrl = "https://www.touchngo.com.my"
    val fpxWebUrl = "https://paynet.my/personal-solutions/fpx.html"

    // ✅ Function to open TNG
    fun openTNG() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tngDeepLink)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            // ✅ TNG App installed - open app
            context.startActivity(intent)
            Toast.makeText(context, "Opening TNG eWallet...", Toast.LENGTH_SHORT).show()
        } else {
            // ❌ TNG App not installed - open website
            Toast.makeText(context, "TNG App not installed. Opening website...", Toast.LENGTH_SHORT).show()
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tngWebUrl))
            context.startActivity(webIntent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay to $doctorName", color = White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary
                )
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(20.dp)
                .fillMaxSize()
                .background(White)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = BlueLight
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Consultation Fee", style = MaterialTheme.typography.titleMedium, color = BlueDark)
                    val feeText = "RM %.2f".format(amount)
                    Text(feeText, style = MaterialTheme.typography.headlineMedium,
                        color = BluePrimary, fontWeight = FontWeight.Bold)
                    Text(doctorName, style = MaterialTheme.typography.bodyLarge, color = TextDark)
                }
            }

            Text("Select Payment Method", style = MaterialTheme.typography.titleMedium, color = BlueDark, fontWeight = FontWeight.SemiBold)

            Card(
                onClick = { selectedMethod = "TnG" },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMethod == "TnG") BlueLight else Color(0xFFF5F5F5)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedMethod == "TnG",
                        onClick = { selectedMethod = "TnG" },
                        colors = RadioButtonDefaults.colors(selectedColor = BluePrimary)
                    )
                    Text("Touch 'n Go eWallet", Modifier.padding(start = 12.dp),
                        style = MaterialTheme.typography.bodyLarge, color = TextDark, fontWeight = FontWeight.Medium)
                }
            }

            Card(
                onClick = { selectedMethod = "FPX" },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMethod == "FPX") BlueLight else Color(0xFFF5F5F5)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedMethod == "FPX",
                        onClick = { selectedMethod = "FPX" },
                        colors = RadioButtonDefaults.colors(selectedColor = BluePrimary)
                    )
                    Text("FPX — Online Banking", Modifier.padding(start = 12.dp),
                        style = MaterialTheme.typography.bodyLarge, color = TextDark, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.weight(1f))

            // ✅ Modified Pay Button - uses deep link for TNG
            val buttonText = "Open $selectedMethod to Pay — RM %.2f".format(amount)
            Button(
                onClick = {
                    if (selectedMethod == "TnG") {
                        // ✅ Try TNG App first
                        openTNG()
                    } else {
                        // FPX - open web
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fpxWebUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        Toast.makeText(context, "Opening FPX...", Toast.LENGTH_SHORT).show()
                    }

                    // Save pending payment
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = White
                )
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val payments = loadPayments(context).toMutableList()
                    val latest = payments.lastOrNull {
                        it.status == "PENDING" &&
                                it.paymentMethod == selectedMethod &&
                                it.doctorId == doctorId &&
                                it.patientId == patientId
                    }
                    if (latest != null) {
                        val updated = latest.copy(status = "PAID")
                        payments[payments.indexOf(latest)] = updated
                        savePayments(context, payments)

                        val chatExpiryTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000
                        val expireDate = Date(chatExpiryTime)
                        val expireStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(expireDate)
                        val successMsg = "Payment Successful!\nChat access: 24h\nExpires: $expireStr"
                        Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()

                        onPaymentSuccess(chatExpiryTime, latest.paymentMethod)
                    } else {
                        Toast.makeText(context, "No pending payment found", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueDark,
                    contentColor = White
                )
            ) {
                Text("I Have Completed Payment", fontWeight = FontWeight.Bold)
            }
        }
    }
}