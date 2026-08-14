package com.example.healt4u.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healt4u.ViewModel.NPRAMedicineViewModel
import com.example.healt4u.ViewModel.ViewModelMedicine
import com.example.healt4u.screen.Medicine.AddMedicineScreen

@Composable
fun AppNavGraph(navHost : NavHostController = rememberNavController()){
    val vm : ViewModelMedicine = viewModel()
    val vm_NPRA : NPRAMedicineViewModel = viewModel()
    NavHost (
        navController = navHost,
        startDestination = " Medicine Management"
    ){
        composable("Medicine Management"){
            AddMedicineScreen(vm)
        }

    }
}