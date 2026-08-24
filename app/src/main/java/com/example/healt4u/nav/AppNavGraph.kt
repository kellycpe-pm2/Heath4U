// com/example/healt4u/nav/AppNavGraph.kt
package com.example.healt4u.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healt4u.ViewModel.ReminderViewModel
import com.example.healt4u.ViewModel.ViewModelMedicine
import com.example.healt4u.screen.Dashboard.HomeDashboardScreen
import com.example.healt4u.screen.Dashboard.ScheduleListScreen
import com.example.healt4u.screen.Medicine.AddMedicineScreen
import com.example.healt4u.screen.Medicine.EditMedicineScreen
import com.example.healt4u.screen.Medicine.MedicineDetailScreen
import com.example.healt4u.screen.Medicine.MedicineListScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    vm_med: ViewModelMedicine = viewModel(),
    vm_reminder: ReminderViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm_med.loadFromLocal(context)
    }

    val medicines by vm_med.medicines.collectAsStateWithLifecycle()
    val success by vm_med.success.collectAsStateWithLifecycle()


    //reset
    vm_med.clearSuccessState()
    vm_med.clearError()
    vm_med.clearValidationErrors()
    vm_med.clearSuccess()
    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            HomeDashboardScreen(
                vm = vm_reminder,
                onMedicineClick = { navController.navigate("list") },
                onScheduleClick = { navController.navigate("schedule") }
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
                onBack= { navController.popBackStack()}
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
    }
}