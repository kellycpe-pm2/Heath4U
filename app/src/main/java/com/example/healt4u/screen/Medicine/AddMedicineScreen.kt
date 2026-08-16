package com.example.healt4u.screen.Medicine
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healt4u.R
import com.example.healt4u.screen.componentUI.TextFieldInput
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.screen.componentUI.dropDownMenu
import com.example.healt4u.ViewModel.ViewModelMedicine
import com.example.healt4u.data.MedicineData
import com.example.healt4u.screen.componentUI.DatePickerPopupOnClick
import com.example.healt4u.screen.componentUI.NumericStepper
import com.example.healt4u.screen.componentUI.TimingButton
import com.example.healt4u.screen.componentUI.button
import com.example.healt4u.screen.componentUI.slider

fun fetchCategories(categories : List<Pair<Char, String>>,id : Char) : String{
    return (categories.find{it.first== id})?.second ?: ""

}




@Composable
fun AddMedicineScreen(vm :ViewModelMedicine = viewModel()){

    val medicinesList by vm.medicines.collectAsStateWithLifecycle()
    val med_name by vm.input_med_name.collectAsStateWithLifecycle()
    val med_category by vm.input_category.collectAsStateWithLifecycle()



    colorTheme(
        {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Medicine",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(25.dp))

                TextFieldInput(
                    med_name,
                    vm::on_Med_Name_Change,
                    "Medicine Name",
                    Modifier.fillMaxWidth(),
                    false,
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().height(70.dp)
                ) {
                    dropDownMenu(
                        Modifier.fillMaxSize(),
                        med_category,
                        MedicineData.categories.map { it.second },
                        "Category",
                        vm::on_Category_Change
                    )

                }


                NumericStepper(0, 10000, 50, 50, vm::on_Dos_Change)

                NumericStepper(0, 100, 1, 1, vm::on_Quantity_Change)

                DatePickerPopupOnClick(modifier = Modifier.fillMaxWidth(), "Expired Date")

                var isBeforeEating by remember { mutableStateOf(true) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // BUTTON 1: Before Eating
                    TimingButton(
                        text = "Before Eating",
                        isSelected = isBeforeEating,
                        id = R.drawable.eatbefore,
                        onClick = { isBeforeEating = true },
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    TimingButton(
                        text = "After Eating",
                        isSelected = !isBeforeEating,
                        id = R.drawable.eatafter,
                        onClick = { isBeforeEating = false }
                    )


                }
                slider()
                TextFieldInput(
                    med_name,
                    vm::on_Med_Name_Change,
                    "Remark",
                    Modifier.fillMaxWidth().padding(15.dp),
                    false,
                    singleLine = false
                )
                button(modifier = Modifier.fillMaxWidth(), text = "Submit", onClick = vm::addMedicneForm)

            }
        }

    )
            }




@Preview (showBackground = true)
@Composable
fun add(){
    AddMedicineScreen()
}