package com.example.healt4u.nav

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healt4u.Storage.clearMessagesByConversation
import com.example.healt4u.Storage.createConversation
import com.example.healt4u.Storage.deleteMessage
import com.example.healt4u.ViewModel.AdminManagementViewModel
import com.example.healt4u.ViewModel.FamilyModeViewModel
import com.example.healt4u.ViewModel.HospitalViewModel
import com.example.healt4u.ViewModel.ReminderViewModel
import com.example.healt4u.ViewModel.ViewModelMedicine
import com.example.healt4u.screen.Admin.AdminDashboardScreen
import com.example.healt4u.screen.Admin.AdminDoctorScreen
import com.example.healt4u.screen.Admin.AdminAddDoctorScreen
import com.example.healt4u.screen.Admin.AdminForgotPasswordScreen
import com.example.healt4u.screen.Admin.AdminHospitalScreen
import com.example.healt4u.screen.Admin.AdminLoginScreen
import com.example.healt4u.screen.Admin.AdminResetPasswordScreen
import com.example.healt4u.screen.Admin.AdminSettingsScreen
import com.example.healt4u.screen.Dashboard.HomeDashboardScreen
import com.example.healt4u.screen.Dashboard.ScheduleListScreen
import com.example.healt4u.screen.DoctorPatientChat.ChatListScreen
import com.example.healt4u.screen.DoctorPatientChat.ChatScreen
import com.example.healt4u.screen.DoctorPatientChat.DoctorListScreen
import com.example.healt4u.screen.DoctorPatientChat.HospitalListScreen
import com.example.healt4u.screen.FamilyMode.AddCaregiverScreen
import com.example.healt4u.screen.FamilyMode.FamilyModeScreen
import com.example.healt4u.screen.FamilyMode.SetPatientPhoneScreen
import com.example.healt4u.screen.Patient.PatientLoginScreen
import com.example.healt4u.screen.Patient.PatientSettingsScreen
import com.example.healt4u.notification.DailyRefreshScheduler
import com.example.healt4u.Session.CurrentSession
import com.example.healt4u.Storage.createPayment
import com.example.healt4u.Storage.getDoctorById
import com.example.healt4u.Storage.getHospitalById
import com.example.healt4u.screen.Medicine.AddMedicineScreen
import com.example.healt4u.screen.Medicine.EditMedicineScreen
import com.example.healt4u.screen.Medicine.MedicineDetailScreen
import com.example.healt4u.screen.Medicine.MedicineListScreen
import com.example.healt4u.screen.ScanScreen.ManualInputDialog
import com.example.healt4u.screen.ScanScreen.ScannerScreen
import com.example.healt4u.model.Message
import kotlinx.coroutines.launch
import com.example.healt4u.Storage.getMessagesByConversation
import com.example.healt4u.Storage.getPatientById
import com.example.healt4u.Storage.sendMessage
import com.example.healt4u.ViewModel.FindMedicineViewModel
import com.example.healt4u.model.Hospital
import com.example.healt4u.model.PatientUser
import com.example.healt4u.model.Payment
import com.example.healt4u.screen.AppointmentScreen
import com.example.healt4u.screen.Dashboard.DoctorDashboardScreen
import com.example.healt4u.screen.DoctorPatientChat.Notification
import com.example.healt4u.screen.PatientListScreen
import com.example.healt4u.screen.Payment.PaymentScreen
import com.example.healt4u.screen.ScanScreen.AddReminderScreen
import com.example.healt4u.screen.ScanScreen.HistoryScreen
import com.example.healt4u.screen.ScanScreen.ScanResult
import com.example.healt4u.screen.ScanScreen.getCurrentDate
import com.example.healt4u.screen.Statistics.AdherenceStatisticScreen
import com.example.healt4u.screen.Statistics.RevenueStatisticScreen
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import java.util.Locale

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.O)
@androidx.camera.core.ExperimentalGetImage
@OptIn(ExperimentalGetImage::class, DelicateCoroutinesApi::class)
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    vm_med: ViewModelMedicine = viewModel(),
    vm_reminder: ReminderViewModel = viewModel(),
    vm_admin: AdminManagementViewModel = viewModel(),
    vm_hospital: HospitalViewModel = viewModel(),
    vm_family: FamilyModeViewModel = viewModel(),
    viewModel: FindMedicineViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ✅ Load from persisted session if available
    var currentUserId by remember { mutableStateOf(CurrentSession.getUserId(context)) }
    var currentUserRole by remember { mutableStateOf(CurrentSession.getUserRole(context)) }
    var loggedInAdminUsername by remember { mutableStateOf(if (CurrentSession.getUserRole(context) == "admin") CurrentSession.getUserName(context) else "") }
    var currentUserName by remember { mutableStateOf(CurrentSession.getUserName(context)) }
    var currentUserPhone by remember { mutableStateOf(CurrentSession.getUserPhone(context)) }
    val patient = remember { mutableStateOf<PatientUser?>(null) }

    val startDest = remember {
        if (CurrentSession.isLoggedIn(context)) {
            val role = CurrentSession.getUserRole(context)
            when (role) {
                "admin" -> "admin"
                "doctor" -> "doctor"
                else -> "dashboard"
            }
        } else {
            "login"
        }
    }

    LaunchedEffect(Unit) {
        if (CurrentSession.isLoggedIn(context)) {
            CurrentSession.patientId = currentUserId
        }
    }

    LaunchedEffect(Unit) {
        vm_med.loadFromLocal(context)
    }

    LaunchedEffect(Unit) {
        Notification.createChannel(context)
        // Books the recurring 06:00 background refresh (DailyRefreshReceiver)
        // so dose/stock notifications keep firing even on days the user never
        // opens the app. BootReceiver re-books this chain after every reboot.
        DailyRefreshScheduler.scheduleNextRefresh(context)
    }

    val medicines by vm_med.medicines.collectAsStateWithLifecycle()
    val success by vm_med.success.collectAsStateWithLifecycle()
    var showManualDialog by remember { mutableStateOf(false) }
    val mutedConversations = remember { mutableStateListOf<String>() }

    vm_med.clearSuccessState()
    vm_med.clearError()
    vm_med.clearValidationErrors()
    vm_med.clearSuccess()

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable("login") {
            AdminLoginScreen(
                onAdminLoginSuccess = { username ->
                    loggedInAdminUsername = username
                    currentUserRole = "admin"
                    CurrentSession.saveSession(context, 1, "admin", username) 
                    navController.navigate("admin") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onPatientLoginClick = {
                    navController.navigate("patient_login")
                },
                onForgotPassword = {
                    navController.navigate("forgot_password")
                },
                onDoctorSuccessClick = {
                    currentUserRole = "doctor"
                    CurrentSession.saveSession(context, 2, "doctor", "Doctor")
                    navController.navigate("doctor") {
                         popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("patient_login") {
            PatientLoginScreen(
                onLoginSuccess = { userId, userName, userPhone ->
                    currentUserId = userId
                    currentUserName = userName
                    currentUserPhone = userPhone
                    currentUserRole = "patient"
                    CurrentSession.saveSession(context, userId, "patient", userName, userPhone)
                    vm_family.savePatientPhone(context, userPhone)
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("forgot_password") {
            AdminForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onAccountFound = { emailOrPhone, method ->
                    navController.navigate(
                        "reset_password/${method}/${
                            java.net.URLEncoder.encode(
                                emailOrPhone,
                                "UTF-8"
                            )
                        }"
                    ) {
                        popUpTo("forgot_password") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "reset_password/{method}/{emailOrPhone}",
            arguments = listOf(
                navArgument("method") { type = NavType.StringType },
                navArgument("emailOrPhone") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val method = backStackEntry.arguments?.getString("method") ?: "email"
            val emailOrPhone = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("emailOrPhone") ?: "",
                "UTF-8"
            )

            AdminResetPasswordScreen(
                emailOrPhone = emailOrPhone,
                method = method,
                onBack = { navController.popBackStack() },
                onPasswordReset = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            HomeDashboardScreen(
                vm = vm_reminder,
                patientId = currentUserId,
                onMedicineClick = { navController.navigate("list") },
                onScheduleClick = { navController.navigate("schedule") },
                onChatClick = { navController.navigate("chat_list") },
                onFamilyModeClick = { navController.navigate("family_mode") },
                onAppointmentClick = { navController.navigate("appointment_screen/$currentUserId") },
                onAdherenceClick = { navController.navigate("adherence_statistics/$currentUserId") },
                onScanClick = { navController.navigate("scan") },
                onProfileClick = { navController.navigate("patient_settings?startAtProfile=true") },
                onSettingsClick = { navController.navigate("patient_settings") }
            )
        }

        composable(
            route = "patient_settings?startAtProfile={startAtProfile}",
            arguments = listOf(
                navArgument("startAtProfile") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val startAtProfile = backStackEntry.arguments?.getBoolean("startAtProfile") ?: false

            PatientSettingsScreen(
                patientId = currentUserId,
                startAtProfile = startAtProfile,
                onBack = { navController.popBackStack() },
                onSwitchAccount = {
                    currentUserId = 0
                    currentUserRole = "patient"
                    currentUserName = ""
                    currentUserPhone = ""
                    CurrentSession.clearSession(context)
                    // Skips role selection since they're still a patient —
                    // just lets a different patient account sign in.
                    navController.navigate("patient_login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onLogout = {
                    currentUserId = 0
                    currentUserRole = "patient"
                    currentUserName = ""
                    currentUserPhone = ""
                    CurrentSession.clearSession(context)
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("schedule") {
            val context = LocalContext.current
            val reloadKey = remember { mutableIntStateOf(0) }

            //Reloads EVERY TIME screen appears
            LaunchedEffect(Unit, reloadKey.intValue) {
                // ✅ Directly use YOUR load function
                val allLogs = com.example.healt4u.data.local.loadReminderLogs(context)
                Log.d("SCHEDULE", "Total logs found: ${allLogs.size}")
                allLogs.forEach {
                    Log.d("SCHEDULE", "→ ${it.date} | ${it.medicineName}")
                }

                //Tell ViewModel to reload
                vm_reminder.loadTodaySchedule(context,currentUserId)
            }

            // Listen for refresh signal from Appointment
            LaunchedEffect(Unit) {
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("refresh_schedule", false)
                    ?.collect { shouldRefresh ->
                        if (shouldRefresh) {
                            reloadKey.intValue += 1
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("refresh_schedule", false)
                        }
                    }
            }

            ScheduleListScreen(
                vm = vm_reminder,
                patientId = currentUserId,
                onBack = { navController.popBackStack() }
            )
        }

        composable("list") {
            MedicineListScreen(
                vm = vm_med,
                onAddClick = { navController.navigate("add") },
                onDel = { medicine ->
                    vm_med.deleteMedicineBoth(medicine, context)
                },
                onEdit = { medicine ->
                    navController.navigate("edit/${medicine?.id ?: -1}")
                },
                onClickRow = { medicine ->
                    navController.navigate("viewMedicine/${medicine?.id ?: -1}")
                },
                onCloudSync = {
                    vm_med.syncWithServer(context)
                },
                onUploadToCloud = {
                    vm_med.uploadToServer(context)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("add") {
            LaunchedEffect(success) {
                if (success == true) {
                    navController.popBackStack()
                    vm_med.clearSuccessState()
                }
            }

            AddMedicineScreen(
                vm = vm_med,
                onAddClick = {
                    vm_med.addMedicineWithValidation(context)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit/{medicineId}",
            arguments = listOf(
                navArgument("medicineId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getInt("medicineId") ?: -1
            val medicine = medicines.find { it.id == medicineId }

            LaunchedEffect(success) {
                if (success == true) {
                    navController.popBackStack()
                    vm_med.clearSuccessState()
                }
            }

            if (medicine != null) {
                EditMedicineScreen(
                    medicine = medicine,
                    onEdit = { updatedMedicine ->
                        vm_med.updateMedicineWithValidation(updatedMedicine, context)
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    vm = vm_med
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(
            route = "viewMedicine/{medicineId}",
            arguments = listOf(
                navArgument("medicineId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getInt("medicineId") ?: -1
            val medicine = medicines.find { it.id == medicineId }

            if (medicine != null) {
                MedicineDetailScreen(
                    medicine = medicine,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = {
                        navController.navigate("edit/${medicine.id}")
                    }
                )
            } else {
                navController.popBackStack()
            }
        }

        composable("scan") {
            ScannerScreen(
                onBarcodeScanned = { scannedData ->
                    val cleanData = scannedData.trim()
                    if (cleanData.isBlank()) {
                        Log.d("SCAN_NAV", "Empty scan result")
                        return@ScannerScreen
                    }
                    Log.d("SCAN_NAV", "Final scan = $cleanData")
                    viewModel.searchUnified(cleanData)
                    val encodedData = Uri.encode(cleanData)
                    navController.navigate("detail/$encodedData")
                },
                onManualInput = { showManualDialog = true },
                onFlashToggle = { enabled ->
                    Log.d("SCAN_NAV", "Flashlight = $enabled")
                },
                onGalleryPick = {
                    Log.d("SCAN_NAV", "Gallery opened")
                }
            )

            if (showManualDialog) {
                ManualInputDialog(
                    onDismiss = { showManualDialog = false },
                    onSearch = { query ->
                        val cleanQuery = query.trim()
                        if (cleanQuery.isBlank()) return@ManualInputDialog
                        showManualDialog = false
                        Log.d("SCAN_NAV", "Manual search = $cleanQuery")
                        viewModel.searchUnified(cleanQuery)
                        val encodedQuery = Uri.encode(cleanQuery)
                        navController.navigate("detail/$encodedQuery")
                    }
                )
            }
        }

        /*composable("history") {
            HistoryScreen(
                medicines = viewModel.medicines.collectAsState().value,
                onItemClick = { regNo ->
                    navController.navigate("detail/$regNo")
                },
                onClearHistory = {
                    // 清空历史逻辑
                }
            )
        }*/

        composable(
            route = "detail/{barcode}",
            arguments = listOf(
                navArgument("barcode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedBarcode = backStackEntry
                .arguments
                ?.getString("barcode") ?: ""
            val barcode = Uri.decode(encodedBarcode)
            Log.d("DETAIL_SCREEN", "Showing result for = $barcode")

            val result by viewModel.searchResult.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val errorMessage by viewModel.errorMessage.collectAsState()

            ScanResult(
                result = result,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onBack = {
                    viewModel.clearResult()
                    navController.popBackStack()
                },
                onAddToReminder = { _ ->
                    val medicineCode = barcode.trim()
                    if (medicineCode.isBlank()) return@ScanResult
                    val encodedMedicineCode = Uri.encode(medicineCode)
                    navController.navigate("add_reminder/$encodedMedicineCode")
                }
            )
        }

        /*composable(
            route = "add_reminder/{regNo}",
            arguments = listOf(navArgument("regNo") { type = NavType.StringType })
        ) { backStackEntry ->
            val regNo = backStackEntry.arguments?.getString("regNo") ?: ""
            val medicine = viewModel.medicines.value.find { it.regNo == regNo }
                ?: viewModel.searchResult.collectAsState().value

            AddReminderScreen(
                onBack = { navController.popBackStack() },
                onSave = {
                }
            )
        }*/


        composable(
            route = "appointment_screen/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.IntType })
        ) { backStack ->
            val patientId = backStack.arguments?.getInt("patientId") ?: 0
            AppointmentScreen(
                patientId = patientId,
                onBack = { navController.popBackStack() },
                onConfirm = { _, _, _, _ ->
                    // Tell schedule to refresh
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_schedule", true)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "adherence_statistics/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.IntType })
        ) { backStack ->
            val pid = backStack.arguments?.getInt("patientId") ?: 1
            AdherenceStatisticScreen(
                patientId = pid,
                onBack = { navController.popBackStack() }
            )
        }

        composable("patient_list") {
            PatientListScreen(
                doctorId = currentUserId,
                onPatientClick = { patientId, conversation ->
                    navController.navigate("adherence_statistics/$patientId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("chat_list") {
            LaunchedEffect(currentUserId, currentUserRole) {
                Log.d("CHAT_ROLE_DEBUG", "userId=$currentUserId, role=$currentUserRole")
            }
            ChatListScreen(
                userId = currentUserId,
                userRole = currentUserRole,
                onConversationClick = { conversation, chatName ->
                    val expiryTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000
                    navController.navigate(
                        "chat_with_expiry/${conversation.id}/${conversation.doctorId}/${conversation.patientId}/$expiryTime"
                    )
                },
                onNewChatClick = {
                    navController.navigate("hospital_list")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("hospital_list") {
            HospitalListScreen(
                onHospitalSelected = { hospital ->
                    navController.navigate("doctor_list/${hospital.id}")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "doctor_list/{hospitalId}",
            arguments = listOf(
                navArgument("hospitalId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val hospitalId = backStackEntry.arguments?.getInt("hospitalId") ?: 0

            LaunchedEffect(hospitalId) {
                vm_hospital.selectHospital(
                    Hospital(
                        id = hospitalId,
                        name = "",
                        address = "",
                        phone = ""
                    )
                )
            }

            val selectedHospital by vm_hospital.selectedHospital.collectAsState()
            val doctors by vm_hospital.doctors.collectAsState()

            if (selectedHospital?.id == hospitalId) {
                DoctorListScreen(
                    hospital = selectedHospital!!,
                    onDoctorSelected = { doctor ->
                        navController.navigate("payment/${doctor.id}/$hospitalId")
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        composable(
            route = "payment/{doctorId}/{hospitalId}",
            arguments = listOf(
                navArgument("doctorId") { type = NavType.IntType },
                navArgument("hospitalId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getInt("doctorId") ?: 0
            val hospitalId = backStackEntry.arguments?.getInt("hospitalId") ?: 0
            var doctorName by remember { mutableStateOf("Doctor") }
            var consultationFee by remember { mutableDoubleStateOf(50.0) }

            LaunchedEffect(doctorId) {
                coroutineScope.launch {
                    val doctor = getDoctorById(doctorId)
                    doctorName = doctor?.name ?: "Dr. Unknown"
                    consultationFee = doctor?.consultationFee ?: 50.0
                    Log.d("PAYMENT_DEBUG", "doctorId=$doctorId → name=$doctorName")
                }
            }

            PaymentScreen (
                doctorId = doctorId,
                hospitalId = hospitalId,
                doctorName = doctorName,
                consultationFee = consultationFee,
                patientId = currentUserId,
                onPaymentSuccess = { chatExpiryTime, paymentMethod ->
                    coroutineScope.launch {
                        val hospitalName = getHospitalById(hospitalId)?.name ?: ""
                        val conversation = createConversation(
                            doctorId = doctorId,
                            patientId = currentUserId,
                            doctorName = doctorName,
                            patientName = currentUserName,
                            hospitalId = hospitalId,
                            hospitalName = hospitalName
                        )

                        if (conversation != null) {
                            val paymentId = java.util.UUID.randomUUID().toString()

                            val payment = Payment(
                                id = paymentId,
                                patientId = currentUserId,
                                doctorId = doctorId,
                                doctorName = doctorName,
                                amount = consultationFee,
                                paymentMethod = paymentMethod,
                                status = "completed",
                                date = getCurrentDate(),
                                time = System.currentTimeMillis().toString()
                            )

                            Log.d("PAYMENT", "Saving payment: id=$paymentId, patient=$currentUserId, doctor=$doctorId")

                            val saved = createPayment(payment)
                            if (saved != null) {
                                Log.d("PAYMENT", "✅ Payment saved! ID=${saved.id}")
                            } else {
                                Log.e("PAYMENT", "❌ Failed to save payment")
                            }

                            navController.navigate(
                                "chat_with_expiry/${conversation.id}/${conversation.doctorId}/${conversation.patientId}/$chatExpiryTime"
                            )
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "chat_with_expiry/{convId}/{doctorId}/{patientId}/{expiryTime}",
            arguments = listOf(
                navArgument("convId") { type = NavType.IntType },
                navArgument("doctorId") { type = NavType.IntType },
                navArgument("patientId") { type = NavType.IntType },
                navArgument("expiryTime") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val convId = backStackEntry.arguments?.getInt("convId") ?: 0
            val doctorId = backStackEntry.arguments?.getInt("doctorId") ?: 0
            val patientId = backStackEntry.arguments?.getInt("patientId") ?: 0
            val expiryTime = backStackEntry.arguments?.getLong("expiryTime") ?: 0L

            var displayName by remember { mutableStateOf("Loading...") }

            LaunchedEffect(doctorId, patientId, currentUserRole) {
                coroutineScope.launch {
                    Log.d("NAME_LOOKUP", "role=$currentUserRole, doctorId=$doctorId, patientId=$patientId")
                    if (currentUserRole == "doctor") {
                        val patient = getPatientById(patientId)
                        displayName = patient?.name ?: "Patient"
                        Log.d("NAME_LOOKUP", "Doctor sees patient: ${patient?.name}")
                    } else {
                        val doctor = getDoctorById(doctorId)
                        displayName = doctor?.name ?: "Doctor"
                        Log.d("NAME_LOOKUP", "Patient sees doctor: ${doctor?.name}")
                    }
                }
            }

            var initialMessages by remember { mutableStateOf<List<Message>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            var selectedPatientId by remember { mutableStateOf<Int?>(null) }

            fun handleNewMessage(message: Message, convId: Int, currentUserId: Int) {
                val isFromOther = message.senderId != currentUserId
                val isMuted = convId.toString() in mutedConversations
                if (isFromOther && !isMuted) {
                    Notification.showSafely(
                        context = context,
                        title = "New message from ${message.senderName}",
                        message = message.content.take(40)
                    )
                }
            }

            LaunchedEffect(convId) {
                try {
                    initialMessages = getMessagesByConversation(convId)
                    val latestMessage = initialMessages.lastOrNull()
                    if (latestMessage != null) {
                        handleNewMessage(latestMessage, convId, currentUserId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                ChatScreen(
                    chatName = displayName,
                    userId = currentUserId,
                    userRole = currentUserRole,
                    conversationId = convId,
                    doctorId = doctorId,
                    patientId = patientId,
                    chatExpiryTime = expiryTime,
                    initialMessages = initialMessages,
                    onBack = { navController.popBackStack() },
                    onSendMessage = { message ->
                        coroutineScope.launch { sendMessage(message) }
                    },
                    onDeleteMessage = { message ->
                        GlobalScope.launch { deleteMessage(message.id) }
                    },
                    onAvatarClick = { pid -> selectedPatientId = pid },
                    isMuted = convId.toString() in mutedConversations,
                    onMuteChanged = { newState ->
                        if (newState) mutedConversations.add(convId.toString())
                        else mutedConversations.remove(convId.toString())
                    },
                    onClearAllMessages = {
                        GlobalScope.launch { clearMessagesByConversation(convId) }
                    }
                )
            }
        }

        composable("doctor") {
            currentUserRole = "doctor"
            // ⚠️ SET THIS FROM DOCTOR LOGIN — NOT HARDCODED!
            // currentUserId = 2  // ❌ REMOVED — should come from login!
            DoctorDashboardScreen(
                onPatientClick = { pId, conv ->
                    navController.navigate("adherence_statistics/$pId")
                },
                onListClick = { navController.navigate("patient_list") },
                onChatClick = {
                    currentUserRole = "doctor"
                    navController.navigate("chat_list")
                },
                onStatisticClick = {
                    navController.navigate("revenue_statistic/$currentUserId")
                },
                onSettingClick = {},
                onScanClick = {},
                onProfileClick = {}
            )
        }

        composable("revenue_statistic/{doctorId}") {
            val doctorId = it.arguments?.getString("doctorId") ?: ""
            RevenueStatisticScreen(doctorId = doctorId, onBack = { navController.popBackStack() })
        }


        composable("admin") {
            AdminDashboardScreen(
                onBack = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onHospitalsClick = { navController.navigate("admin_hospitals") },
                onDoctorsClick = { navController.navigate("admin_doctors") },
                onSettingsClick = { navController.navigate("admin_settings") }
            )
        }

        composable("admin_settings") {
            AdminSettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    loggedInAdminUsername = ""
                    currentUserId = 0
                    currentUserRole = "patient"
                    CurrentSession.clearSession(context)
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                adminUsername = loggedInAdminUsername
            )
        }

        composable("admin_hospitals") {
            AdminHospitalScreen(vm = vm_admin, onBack = { navController.popBackStack() })
        }

        composable("admin_doctors") {
            AdminDoctorScreen(
                vm = vm_admin,
                onBack = { navController.popBackStack() },
                onAddDoctor = {navController.navigate("admin_add_doctor")}
            )
        }

        composable("admin_add_doctor") {
            AdminAddDoctorScreen(
                vm = vm_admin,
                onBack = { navController.popBackStack() },
                onDoctorAdded = { navController.popBackStack() }
            )
        }

        composable("family_mode") {
            FamilyModeScreen(
                vm = vm_family,
                currentUserId = currentUserId,
                currentUserName = currentUserName,
                currentUserPhone = currentUserPhone,
                onBack = { navController.popBackStack() },
                onAddCaregiverClick = { navController.navigate("add_caregiver") },
                onSetPhoneClick = { navController.navigate("set_patient_phone") },
            )
        }

        composable("add_caregiver") {
            AddCaregiverScreen(
                vm = vm_family,
                currentUserId = currentUserId,
                currentUserName = currentUserName,
                currentUserPhone = currentUserPhone,
                onBack = { navController.popBackStack() },
                onAdded = { navController.popBackStack() }
            )
        }

        composable("set_patient_phone") {
            SetPatientPhoneScreen(
                vm = vm_family,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

    }
}

