package com.example.healt4u.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healt4u.ViewModel.ViewModelMedicine
import com.example.healt4u.screen.Medicine.AddMedicineScreen
import com.example.healt4u.screen.Medicine.MedicineListScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController(),vm_med : ViewModelMedicine = viewModel()) {
    vm_med.loadMedicines()
    val medicines  by vm_med.medicines.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        composable("list") {
            MedicineListScreen (vm_med,
                oAddnClick ={ navController.navigate("add") },
                onDel = {med->

            }, onEdit = {med->

            }, onClickRow = {med->
                navController.navigate("viewMedicine/${med?.id?:-1}")
            } )
        }

        composable("add") {
            AddMedicineScreen(
                vm=vm_med,
                onAddClick = {vm_med.addMedicneForm()
                    navController.popBackStack() }
            )
        }

        composable("viewMedicine/{id}",listOf(navArgument("id"){ NavType.IntType})){id->
            var id1 = id.arguments?.getInt("id")?:-1

        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppNavGraph() {
    AppNavGraph()

}