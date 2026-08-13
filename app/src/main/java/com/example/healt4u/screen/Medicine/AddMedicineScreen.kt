package com.example.healt4u.screen.Medicine
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.componentUI.TextFieldInput
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.screen.componentUI.dropDownMenu
import com.example.healt4u.ViewModel.ViewModelMedicine

fun fetchCategories(categories : List<Pair<Char, String>>,id : Char) : String{
    return (categories.find{it.first== id})?.second ?: ""

}




@Composable
fun AddMedicineScreen(){
    var med_name by remember {mutableStateOf("")}
    var med_category by remember {mutableStateOf("")}

    val categories : List <Pair<Char, String>> = listOf(
        'A'.to("Controlled medicines"),
        'B'.to("Natural Products with Therapeutic Claim"),
        'X'.to("Non-scheduled Poisons"),
        'N'.to("Health Supplements"),
        'H'.to("Veterinary Products")
    )
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

                TextFieldInput("Medicine Name",{med_name=it},"Medicine Name",Modifier.fillMaxWidth(),true, singleLine = true)

                dropDownMenu(Modifier.fillMaxWidth(),categories.map { it.second } ,"Category",{newValue ->med_category = newValue
                })

            }

        }
    )

}

@Preview (showBackground = true)
@Composable
fun add(){
    AddMedicineScreen()
}