package com.example.healt4u.nav

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.healt4u.screen.FamilyMode.CaregiverAlertScreen
import com.example.healt4u.screen.FamilyMode.FamilyModeScreen
import com.example.healt4u.screen.FamilyMode.SetPatientPhoneScreen
import com.example.healt4u.screen.Patient.PatientLoginScreen
import com.example.healt4u.screen.Medicine.AddMedicineScreen
import com.example.healt4u.screen.Medicine.EditMedicineScreen
import com.example.healt4u.screen.Medicine.MedicineDetailScreen
import com.example.healt4u.screen.Medicine.MedicineListScreen
import com.example.healt4u.screen.ScanScreen.ManualInputDialog
import com.example.healt4u.screen.ScanScreen.ScannerScreen
import com.example.healt4u.data.HospitalData.getDoctorById
import com.example.healt4u.model.Message
import kotlinx.coroutines.launch
import com.example.healt4u.Storage.getMessagesByConversation
import com.example.healt4u.Storage.getPatientById
import com.example.healt4u.Storage.sendMessage
import com.example.healt4u.ViewModel.NPRAMedicineViewModel
import com.example.healt4u.model.PatientUser
import com.example.healt4u.screen.Adherence.AdherenceStatisticScreen
import com.example.healt4u.screen.AppointmentScreen
import com.example.healt4u.screen.Dashboard.DoctorDashboardScreen
import com.example.healt4u.screen.DoctorPatientChat.Notification
import com.example.healt4u.screen.PatientListScreen
import com.example.healt4u.screen.ScanScreen.AddReminderScreen
import com.example.healt4u.screen.ScanScreen.HistoryScreen
import com.example.healt4u.screen.ScanScreen.ScanResult
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import java.util.Locale

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
    viewModel: NPRAMedicineViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentUserId by remember { mutableStateOf(2) }
    var currentUserRole by remember { mutableStateOf("patient") }
    var loggedInAdminUsername by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("") }
    var currentUserPhone by remember { mutableStateOf("") }
    val patient = remember { mutableStateOf<PatientUser?>(null) }

    LaunchedEffect(Unit) {
        vm_med.loadFromLocal(context)
    }

    LaunchedEffect(Unit) {
        Notification.createChannel(context)
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
        startDestination = "login"
    ) {
        composable("login") {
            AdminLoginScreen(
                onAdminLoginSuccess = { username ->
                    loggedInAdminUsername = username
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
                    navController.navigate("doctor")
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
                onMedicineClick = { navController.navigate("list") },
                onScheduleClick = { navController.navigate("schedule") },
                onChatClick = { navController.navigate("chat_list") },
                onFamilyModeClick = { navController.navigate("family_mode") },
                onAppointmentClick = { navController.navigate("appointment") },
                onAdherenceClick = { navController.navigate("adherence_statistics/$currentUserId") },
                onScanClick = { navController.navigate("scan") }
            )
        }

        composable("schedule") {
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                vm_reminder.loadTodaySchedule(context)
            }

            ScheduleListScreen(
                vm = vm_reminder,
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
                    viewModel.searchMedicine(scannedData)
                    navController.navigate("detail/$scannedData")
                },
                onManualInput = {
                    showManualDialog = true
                },
                onFlashToggle = {},
                onGalleryPick = {},
                onBackClick = {navController.popBackStack()}
            )

            if (showManualDialog) {
                ManualInputDialog(
                    onDismiss = { showManualDialog = false },
                    onSearch = { query ->
                        showManualDialog = false
                        viewModel.searchMedicine(query)
                        navController.navigate("detail/$query")
                    }
                )
            }
        }

        composable("history") {
            HistoryScreen(
                medicines = viewModel.medicines.collectAsState().value,
                onItemClick = { regNo ->
                    navController.navigate("detail/$regNo")
                },
                onClearHistory = {
                    // 清空历史逻辑
                }
            )
        }

        composable(
            route = "detail/{barcode}",
            arguments = listOf(navArgument("barcode") { type = NavType.StringType })
        ) { backStackEntry ->
            val barcode = backStackEntry.arguments?.getString("barcode") ?: ""
            val searchResult by viewModel.searchResult.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val errorMessage by viewModel.errorMessage.collectAsState()

            ScanResult (
                barcode = barcode,
                medicine = searchResult,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onBack = {
                    navController.popBackStack()
                    viewModel.resetSearch()
                },
                onAddToReminder = {
                }
            )
        }

        composable(
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
        }


        composable("appointment") {
            AppointmentScreen(
                onBack = { navController.popBackStack() },
                onConfirm = { hospitalName, doctorName, displayDate, time ->
                    coroutineScope.launch {
                        val inputFormat = java.text.SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                        val outputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                        val parsedDate = inputFormat.parse(displayDate)
                        val standardDate = outputFormat.format(parsedDate!!)

                        vm_reminder.addAppointmentReminder(
                            hospitalName = hospitalName,
                            doctorName = doctorName,
                            date = standardDate,
                            time = time
                        )
                    }
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
                onConversationClick = { conversation ->
                    navController.navigate(
                        "chat/${conversation.id}/${conversation.doctorId}/${conversation.patientId}"
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
            val hospital = com.example.healt4u.data.HospitalData.getHospitalById(hospitalId)

            if (hospital != null) {
                DoctorListScreen(
                    hospital = hospital,
                    onDoctorSelected = { doctor ->
                        coroutineScope.launch {
                            val conversation = createConversation(
                                doctorId = doctor.id,
                                patientId = currentUserId,
                                doctorName = doctor.name,
                                patientName = "Patient",
                                hospitalId = hospital.id,
                                hospitalName = hospital.name
                            )
                            if (conversation != null) {
                                navController.navigate(
                                    "chat/${conversation.id}/${conversation.doctorId}/${conversation.patientId}"
                                )
                            }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(
            route = "chat/{convId}/{doctorId}/{patientId}",
            arguments = listOf(
                navArgument("convId") { type = NavType.IntType },
                navArgument("doctorId") { type = NavType.IntType },
                navArgument("patientId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val convId = backStackEntry.arguments?.getInt("convId") ?: 0
            val doctorId = backStackEntry.arguments?.getInt("doctorId") ?: 0
            val patientId = backStackEntry.arguments?.getInt("patientId") ?: 0

            var patientName by remember { mutableStateOf("Patient") }
            var doctorName by remember { mutableStateOf("Doctor") }

            LaunchedEffect(doctorId, patientId) {
                val doctor = getDoctorById(doctorId)
                val patient = getPatientById(patientId)
                doctorName = doctor?.name ?: "Doctor"
                patientName = patient?.name ?: "Patient"
            }

            val chatName = if (currentUserRole == "doctor") patientName else doctorName

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

            var lastLoadedMessageId by remember { mutableStateOf<Int?>(null) }

            LaunchedEffect(convId) {
                try {
                    initialMessages = getMessagesByConversation(convId)
                    val latestMessage = initialMessages.lastOrNull()
                    if (latestMessage != null) {
                        if (latestMessage.id != lastLoadedMessageId) {
                            handleNewMessage(latestMessage, convId, currentUserId)
                        }
                        lastLoadedMessageId = latestMessage.id
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }

            LaunchedEffect(selectedPatientId) {
                val targetId = selectedPatientId ?: return@LaunchedEffect
                navController.navigate("adherence_statistics/$targetId")
                selectedPatientId = null
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                var reloadKey by remember { mutableIntStateOf(0) }

                ChatScreen(
                    chatName = chatName,
                    userId = currentUserId,
                    userRole = currentUserRole,
                    conversationId = convId,
                    doctorId = doctorId,
                    patientId = patientId,
                    initialMessages = initialMessages,
                    onBack = {
                        reloadKey++
                        navController.popBackStack()
                    },
                    onSendMessage = { message ->
                        coroutineScope.launch {
                            sendMessage(message)
                        }
                    },
                    onDeleteMessage = { message ->
                        GlobalScope.launch {
                            val success = deleteMessage(message.id)
                            if (success) {
                                Log.d("Chat", "Message deleted from cloud")
                            }
                        }
                    },
                    onAvatarClick = { pid ->
                        selectedPatientId = pid
                    },
                    isMuted = convId.toString() in mutedConversations,
                    onMuteChanged = { newState ->
                        if (newState) mutedConversations.add(convId.toString())
                        else mutedConversations.remove(convId.toString())
                    },
                    onClearAllMessages = {
                        GlobalScope.launch {
                            val success = clearMessagesByConversation(convId)
                            if (success) {
                                Log.d("Chat", "All messages cleared from cloud")
                            }
                        }
                    }
                )

                LaunchedEffect(selectedPatientId) {
                    selectedPatientId?.let { id ->
                        navController.navigate("adherence_statistics/$id")
                        selectedPatientId = null
                    }
                }
            }
        }

        composable("doctor") {
            currentUserRole = "doctor"
            DoctorDashboardScreen(
                onPatientClick = { patientId, conversation ->
                    navController.navigate("adherence_statistics/${patientId}")
                },
                onListClick = { navController.navigate("patient_list") },
                onChatClick = {
                    currentUserRole = "doctor"
                    navController.navigate("chat_list")
                },
                onStatisticClick = {
                    navController.navigate("adherence_statistics/$currentUserId")
                },
                onSettingClick = {},
                onScanClick = {},
                onProfileClick = {}
            )
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
                onBack = { navController.popBackStack() }
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
                onCaregiverAlertsClick = { navController.navigate("caregiver_alerts") }
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

        composable("caregiver_alerts") {
            CaregiverAlertScreen(
                vm = vm_family,
                currentUserId = currentUserId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

