package com.example.healt4u.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healt4u.ViewModel.NPRAMedicineViewModel
import com.example.healt4u.ViewModel.ViewModelMedicine
import com.example.healt4u.screen.Medicine.AddMedicineScreen
import com.example.healt4u.screen.Medicine.MedicineListScreen

@Composable
fun AppNavGraph(navHost: NavHostController = rememberNavController(),vm: ViewModelMedicine = viewModel(),vm_NPRA: NPRAMedicineViewModel = viewModel()) {


    NavHost(
        navController = navHost,
        startDestination = "MedicineList"
    ) {
        composable("MedicineList") {
            MedicineListScreen {
                navHost.navigate("AddMedicine")
            }
        }

        composable("AddMedicine") {
            AddMedicineScreen(
                vm,
                onAddClick = { navHost.popBackStack() }
            )
        }
    }
}

@Preview(showBackground = true, name = "Medicine List Preview")
@Composable
fun PreviewMedicineListScreen() {

    AppNavGraph()
}