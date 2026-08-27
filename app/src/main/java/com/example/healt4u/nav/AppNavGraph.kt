package com.example.healt4u.nav

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healt4u.Storage.getMessagesByConversation
import com.example.healt4u.ViewModel.AdminManagementViewModel
import com.example.healt4u.ViewModel.FamilyModeViewModel
import com.example.healt4u.ViewModel.HospitalViewModel
import com.example.healt4u.ViewModel.ReminderViewModel
import com.example.healt4u.ViewModel.ViewModelMedicine
import com.example.healt4u.screen.Admin.AdminDashboardScreen
import com.example.healt4u.screen.Admin.AdminDoctorScreen
import com.example.healt4u.screen.Admin.AdminHospitalScreen
import com.example.healt4u.screen.Admin.AdminLoginScreen
import com.example.healt4u.screen.Admin.AdminSettingsScreen
import com.example.healt4u.screen.Admin.AdminSubscriptionScreen
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
import com.example.healt4u.screen.Medicine.AddMedicineScreen
import com.example.healt4u.screen.Medicine.EditMedicineScreen
import com.example.healt4u.screen.Medicine.MedicineDetailScreen
import com.example.healt4u.screen.Medicine.MedicineListScreen
import com.example.healt4u.screen.ScanScreen.ManualInputDialog
import com.example.healt4u.screen.ScanScreen.ScannerScreen


@androidx.camera.core.ExperimentalGetImage
@OptIn(ExperimentalGetImage::class)
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    vm_med: ViewModelMedicine = viewModel(),
    vm_reminder: ReminderViewModel = viewModel(),
    vm_admin: AdminManagementViewModel = viewModel(),
    vm_hospital: HospitalViewModel = viewModel(),
    vm_family: FamilyModeViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm_med.loadFromLocal(context)
    }

    val medicines by vm_med.medicines.collectAsStateWithLifecycle()
    val success by vm_med.success.collectAsStateWithLifecycle()
    var showManualDialog by remember { mutableStateOf(false) }

    // reset
    vm_med.clearSuccessState()
    vm_med.clearError()
    vm_med.clearValidationErrors()
    vm_med.clearSuccess()

    NavHost(
        navController = navController,
        startDestination = "scan"
    ) {
        composable("login") {
            AdminLoginScreen(
                onAdminLoginSuccess = {
                    navController.navigate("admin") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onPatientLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
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
                onFamilyModeClick = { navController.navigate("family_mode") }
            )
        }

        composable("schedule") {
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
            // Handle navigation on success
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

            // Handle navigation on success
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
                        // Navigation handled by LaunchedEffect above
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
                onBarcodeScanned = { barcode: String ->
                },
                onManualInput = {
                    showManualDialog = true
                },
                onFlashToggle = { isOn: Boolean -> },
                onGalleryPick = {},
                onBackClick = {  },
                context = context
            )

            if (showManualDialog) {
                ManualInputDialog(
                    onDismiss = { showManualDialog = false },
                    onSearch = { malNumber: String ->
                        showManualDialog = false
                    }
                )
            }
        }

        composable("chat_list") {
            ChatListScreen(
                patientId = "p001",
                onConversationClick = { conversation ->
                    navController.navigate("chat/${conversation.id}")
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
                navArgument("hospitalId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val hospitalId = backStackEntry.arguments?.getInt("hospitalId") ?: 0
            val hospital = com.example.healt4u.data.HospitalData.getHospitalById(hospitalId)

            if (hospital != null) {
                DoctorListScreen(
                    hospital = hospital,
                    onDoctorSelected = { doctor ->
                        val conversationId = "${doctor.id}_p001"
                        navController.navigate("chat/$conversationId")
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(
            route = "chat/{conversationId}",
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val doctorId = conversationId.split("_").firstOrNull()?.toIntOrNull() ?: 0
            val doctor = com.example.healt4u.data.HospitalData.getDoctorById(doctorId)

            val initialMessages = getMessagesByConversation(context, conversationId)

            ChatScreen(
                chatName = doctor?.name ?: "Doctor",
                userId = "p001",
                initialMessages = initialMessages,
                onBack = { navController.popBackStack() },
                onSendMessage = { message ->
                    com.example.healt4u.Storage.sendMessage(context, message)
                }
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
                onSubscriptionClick = { navController.navigate("admin_subscription") },
                onSettingsClick = { navController.navigate("admin_settings") }
            )
        }

        composable("admin_settings") {
            AdminSettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("admin_hospitals") {
            AdminHospitalScreen(vm = vm_admin, onBack = { navController.popBackStack() })
        }

        composable("admin_doctors") {
            AdminDoctorScreen(vm = vm_admin, onBack = { navController.popBackStack() })
        }

        composable("admin_subscription") {
            AdminSubscriptionScreen(onBack = { navController.popBackStack() })
        }

        composable("family_mode") {
            FamilyModeScreen(
                vm = vm_family,
                onBack = { navController.popBackStack() },
                onAddCaregiverClick = { navController.navigate("add_caregiver") },
                onSetPhoneClick = { navController.navigate("set_patient_phone") }
            )
        }

        composable("add_caregiver") {
            AddCaregiverScreen(
                vm = vm_family,
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
                onBack = { navController.popBackStack() }
            )
        }
    }
}