package com.example.healt4u.nav

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healt4u.ViewModel.NPRAMedicineViewModel
import com.example.healt4u.ViewModel.ViewModelMedicine
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.Medicine.AddMedicineScreen
import com.example.healt4u.screen.Medicine.MedicineListScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController(),vm_med : ViewModelMedicine = viewModel()) {
        LaunchedEffect(Unit) {
        vm_med.add_m(
            Medicine(
                id = 1,
                name_medicine = "Test Medicine ${System.currentTimeMillis() / 1000}",
                category = "Test Category",
                dosage = 100,
                quantity = 50,
                remark = "Test insert at ${System.currentTimeMillis()}",
                afterEat = true,
                priority = 1.0f,
                ic = "1"
            )
        )
    }

        NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        composable("list") {
            MedicineListScreen (vm_med,  { navController.navigate("add") }
            )
        }

        composable("add") {
            AddMedicineScreen(
                vm=vm_med,
                onAddClick = {vm_med.addMedicneForm()
                    navController.popBackStack() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppNavGraph() {
    AppNavGraph()

}