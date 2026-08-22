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
import com.example.healt4u.ViewModel.ViewModelMedicine
import com.example.healt4u.data.local.loadMedicines
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.Medicine.AddMedicineScreen
import com.example.healt4u.screen.Medicine.EditMedicineScreen
import com.example.healt4u.screen.Medicine.MedicineDetailScreen
import com.example.healt4u.screen.Medicine.MedicineListScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    vm_med: ViewModelMedicine = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        loadMedicines(context)

    }

    val medicines by vm_med.medicines.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        // ========== List Screen ==========
        composable("list") {
            MedicineListScreen(
                vm = vm_med,
                onAddClick = { navController.navigate("add") },
                onDel = { medicine ->
                    vm_med.deleteMedicineLocal(medicine)  // Local only

                },
                onEdit = { medicine ->
                    navController.navigate("edit/${medicine?.id ?: -1}")

                },
                onClickRow = { medicine ->
                    navController.navigate("viewMedicine/${medicine?.id ?: -1}")
                },
                onCloudSync = {
                    vm_med.syncWithServer()
                },
                onUploadToCloud = {
                    vm_med.uploadToServer()
                }
            )
        }

        // ========== Add Screen ==========
        composable("add") {
            AddMedicineScreen(
                vm = vm_med,
                onAddClick = {
                    vm_med.addMedicineForm(context)  // Local only
                    navController.popBackStack()
                }
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

            if (medicine != null) {
                EditMedicineScreen(
                    medicine = medicine,
                    onEdit = { updatedMedicine ->
                        vm_med.updateMedicineLocal(updatedMedicine)  // Local only

                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                navController.popBackStack()
            }
        }

        // ========== Detail Screen ==========
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
