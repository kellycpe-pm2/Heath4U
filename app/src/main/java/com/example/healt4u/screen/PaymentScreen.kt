package com.example.healt4u.screen.Payment

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.model.Payment
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale
import com.example.healt4u.Storage.getHospitalById

private const val PAYMENTS_FILE = "payments.json"

private val BluePrimary = Color(0xFF1565C0)
private val BlueLight = Color(0xFFE3F2FD)
private val BlueDark = Color(0xFF0D47A1)
private val White = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF1A1A1A)
private val GreenSuccess = Color(0xFF4CAF50)

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
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showQrCode by remember { mutableStateOf(false) } // ✅ Show QR after Pay button click
    val date = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val time = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

    val tngDeepLink = "tngdwallet://client/dl/mp?mpid=123456789"
    val tngWebUrl = "https://www.touchngo.com.my"
    val fpxWebUrl = "https://paynet.my/personal-solutions/fpx.html"

    var hospitalName by remember { mutableStateOf("Hospital") }

    LaunchedEffect(hospitalId) {
        val hospital = getHospitalById(hospitalId)
        hospitalName = hospital?.name ?: "Hospital"
    }

    fun openTNG() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tngDeepLink)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            Toast.makeText(context, "Opening TNG eWallet...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "TNG App not installed. Opening website...", Toast.LENGTH_SHORT).show()
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tngWebUrl))
            context.startActivity(webIntent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Payment",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .padding(end = 48.dp)
                    )
                },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(White)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ========== Fee Card ==========
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BlueLight
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Consultation Fee",
                            style = MaterialTheme.typography.titleMedium,
                            color = BlueDark
                        )
                        Text(
                            text = "RM %.2f".format(amount),
                            style = MaterialTheme.typography.headlineLarge,
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = doctorName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextDark
                        )
                        Text(
                            text = hospitalName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDark.copy(alpha = 0.6f)
                        )
                    }
                }

                // ========== Order Summary ==========
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Order Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Consultation", color = TextDark.copy(alpha = 0.7f))
                            Text("RM %.2f".format(amount), color = TextDark)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Service Fee", color = TextDark.copy(alpha = 0.7f))
                            Text("RM 0.00", color = TextDark)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                "RM %.2f".format(amount),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        }
                    }
                }

                // ========== Payment Methods ==========
                Text(
                    text = "Select Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    color = BlueDark,
                    fontWeight = FontWeight.SemiBold
                )

                // TNG Option
                PaymentOptionCard(
                    icon = "📱",
                    title = "Touch 'n Go eWallet",
                    subtitle = "Pay with TNG eWallet",
                    isSelected = selectedMethod == "TnG",
                    onClick = { selectedMethod = "TnG" }
                )

                // FPX Option
                PaymentOptionCard(
                    icon = "🏦",
                    title = "FPX (Online Banking)",
                    subtitle = "Maybank, CIMB, Public Bank, etc.",
                    isSelected = selectedMethod == "FPX",
                    onClick = { selectedMethod = "FPX" }
                )

                // DuitNow Option
                PaymentOptionCard(
                    icon = "🏷️",
                    title = "DuitNow QR",
                    subtitle = "Scan DuitNow QR code",
                    isSelected = selectedMethod == "DuitNow",
                    onClick = { selectedMethod = "DuitNow" }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ========== Pay Button — ORIGINAL LOGIC UNCHANGED ==========
                Button(
                    onClick = {
                        // ✅ Show QR Card ONLY for DuitNow QR
                        if (selectedMethod == "DuitNow") {
                            showQrCode = true
                        } else {
                            showQrCode = false
                        }

                        // ✅ ALL ORIGINAL LOGIC REMAINS THE SAME
                        if (selectedMethod == "TnG") {
                            openTNG()
                        } else {
                            val url = when (selectedMethod) {
                                "FPX" -> fpxWebUrl
                                "GrabPay" -> "https://www.grab.com/my/pay/"
                                "DuitNow" -> "https://www.duitnow.my/"
                                else -> tngWebUrl
                            }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                            Toast.makeText(context, "Opening $selectedMethod...", Toast.LENGTH_SHORT).show()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Payment,
                            contentDescription = "Pay",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Pay with $selectedMethod — RM %.2f".format(amount),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ✅ DUITNOW QR IMAGE CARD — APPEARS AFTER PAY BUTTON CLICK
                if (showQrCode && selectedMethod == "DuitNow") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BlueLight
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, BluePrimary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Scan DuitNow QR",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BlueDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Amount: RM %.2f".format(amount),
                                style = MaterialTheme.typography.bodyMedium,
                                color = BluePrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Image(
                                painter = painterResource(id = com.example.healt4u.R.drawable.duitnow_qr),
                                contentDescription = "DuitNow QR Code",
                                modifier = Modifier
                                    .size(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        BorderStroke(1.dp, Color.LightGray),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Open your bank app or eWallet → Scan QR code above",
                                fontSize = 13.sp,
                                color = TextDark.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // ========== Confirm Payment Button — ORIGINAL LOGIC UNCHANGED ==========
                OutlinedButton(
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

                            showSuccessDialog = true
                            onPaymentSuccess(chatExpiryTime, latest.paymentMethod)
                        } else {
                            Toast.makeText(context, "No pending payment found", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BlueDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Confirm",
                            modifier = Modifier.size(18.dp)
                        )
                        Text("I Have Completed Payment", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                // ========== Security Note ==========
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Secure",
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "All transactions are secure and encrypted",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // ========== Success Dialog ==========
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
                    icon = {
                        Text(
                            text = "✅",
                            fontSize = 48.sp
                        )
                    },
                    title = {
                        Text(
                            text = "Payment Successful!",
                            color = GreenSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Your payment of RM %.2f has been processed successfully.".format(amount),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Chat access granted for 24 hours",
                                color = GreenSuccess,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Expires: ${SimpleDateFormat("dd MMM yyyy HH:mm", LocalLocale.current.platformLocale).format(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenSuccess
                            )
                        ) {
                            Text("Done", color = White)
                        }
                    },
                    containerColor = White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
fun PaymentOptionCard(
    icon: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) BluePrimary else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BlueLight else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) BluePrimary else TextDark
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextDark.copy(alpha = 0.6f)
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = BluePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Payment Screen Preview")
@Composable
fun PreviewPaymentScreen() {
    colorTheme {
        PaymentScreen(
            doctorId = 1,
            hospitalId = 1,
            doctorName = "Dr. Ahmad Ismail",
            consultationFee = 50.0,
            patientId = 2,
            onPaymentSuccess = { time, method ->
                println("Payment successful! Time: $time, Method: $method")
            },
            onBack = {
                println("Back pressed")
            }
        )
    }
}

@Preview(showBackground = true, name = "Payment Screen - High Fee")
@Composable
fun PreviewPaymentScreenHighFee() {
    colorTheme {
        PaymentScreen(
            doctorId = 2,
            hospitalId = 1,
            doctorName = "Dr. Sarah Tan",
            consultationFee = 150.0,
            patientId = 2,
            onPaymentSuccess = { time, method ->
                println("Payment successful! Time: $time, Method: $method")
            },
            onBack = {
                println("Back pressed")
            }
        )
    }
}