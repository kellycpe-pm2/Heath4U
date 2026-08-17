package com.example.healt4u.nav

import android.annotation.SuppressLint
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
fun AppNavGraph(navController: NavHostController = rememberNavController(),vm_med : ViewModelMedicine = viewModel()) {
    NavHost(
        navController = navController,
        startDestination = "MedicineList"
    ) {
        composable("MedicineList") {
            MedicineListScreen (oAddnClick =
                { navController.navigate("AddMedicine") }
            )
        }

        composable("AddMedicine") {
            AddMedicineScreen(
                vm=vm_med,
                onAddClick = { navController.popBackStack() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppNavGraph() {
    AppNavGraph()

}