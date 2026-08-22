package com.example.healt4u.screen.Medicine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healt4u.R
import com.example.healt4u.data.MedicineData
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.componentUI.DatePickerPopupOnClick
import com.example.healt4u.screen.componentUI.NumericStepper
import com.example.healt4u.screen.componentUI.TextFieldInput
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.screen.componentUI.TimingButton
import com.example.healt4u.screen.componentUI.button
import com.example.healt4u.screen.componentUI.dropDownMenu
import com.example.healt4u.screen.componentUI.slider

@Composable
fun EditMedicineScreen(
    medicine: Medicine,
    onEdit: (Medicine) -> Unit,
    onBack: () -> Unit
) {

    var medName by remember {
        mutableStateOf(
            medicine.name_medicine
        )
    }

    var medCategory by remember {
        mutableStateOf(
            medicine.category
        )
    }

    var medDosage by remember {
        mutableStateOf(
            medicine.dosage
        )
    }

    var medQuantity by remember {
        mutableStateOf(
            medicine.quantity
        )
    }

    var medExpiredDate by remember {
        mutableStateOf(
            medicine.expiredDate
        )
    }

    var medIsBeforeEating by remember {
        mutableStateOf(
            medicine.afterEat ?: true
        )
    }

    var medPriority by remember {
        mutableStateOf(
            medicine.priority ?: 1f
        )
    }

    var medRemark by remember {
        mutableStateOf(
            medicine.remark ?: ""
        )
    }


    colorTheme {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(horizontal = 50.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "Edit Medicine",
                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                Modifier.height(25.dp)
            )


            TextFieldInput(
                medName,

                { value ->
                    medName = value
                },

                "Medicine Name",

                Modifier.fillMaxWidth(),

                false,

                singleLine = true
            )


            Spacer(
                Modifier.height(10.dp)
            )


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {

                dropDownMenu(

                    Modifier.fillMaxSize(),

                    medCategory,

                    MedicineData.categories.map {
                        it.second
                    },

                    "Category",

                    { value ->
                        medCategory = value
                    }
                )
            }


            NumericStepper(

                1,
                10000,
                medDosage,
                50,

                { value ->
                    medDosage = value
                }
            )


            NumericStepper(

                0,
                100,
                medQuantity,
                1,

                { value ->
                    medQuantity = value
                }
            )


            DatePickerPopupOnClick(

                modifier =
                    Modifier.fillMaxWidth(),

                "Expired Date",

                value =
                    medExpiredDate,

                { value ->
                    medExpiredDate = value
                }
            )


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                TimingButton(

                    text = "Before Eating",

                    isSelected =
                        medIsBeforeEating,

                    id =
                        R.drawable.eatbefore,

                    onClick = {
                        medIsBeforeEating = true
                    }
                )


                Spacer(
                    Modifier.width(16.dp)
                )


                TimingButton(

                    text = "After Eating",

                    isSelected =
                        !medIsBeforeEating,

                    id =
                        R.drawable.eatafter,

                    onClick = {
                        medIsBeforeEating = false
                    }
                )
            }


            slider(

                medPriority,

                { value ->
                    medPriority = value
                }
            )


            TextFieldInput(

                medRemark,

                { value ->
                    medRemark = value
                },

                "Remark",

                Modifier
                    .fillMaxWidth()
                    .padding(15.dp),

                false,

                singleLine = false
            )


            button(

                modifier =
                    Modifier.fillMaxWidth(),

                text = "Submit",

                onClick = {

                    val updatedMedicine =
                        Medicine(

                            id =
                                medicine.id,

                            name_medicine =
                                medName,

                            category =
                                medCategory,

                            dosage =
                                medDosage,

                            quantity =
                                medQuantity,

                            quantityLeft =
                                medicine.quantityLeft,

                            remark =
                                medRemark,

                            expiredDate =
                                medExpiredDate,

                            afterEat =
                                medIsBeforeEating,

                            createDate =
                                medicine.createDate,

                            priority =
                                medPriority,

                            ic =
                                medicine.ic
                        )


                    onEdit(
                        updatedMedicine
                    )
                }
            )



            button(

                modifier =
                    Modifier.fillMaxWidth(),

                text = "Reset Changes",

                onClick = {

                    medName =
                        medicine.name_medicine

                    medCategory =
                        medicine.category

                    medDosage =
                        medicine.dosage

                    medQuantity =
                        medicine.quantity

                    medExpiredDate =
                        medicine.expiredDate

                    medIsBeforeEating =
                        medicine.afterEat ?: true

                    medPriority =
                        medicine.priority ?: 1f

                    medRemark =
                        medicine.remark ?: ""
                }
            )



            button(

                modifier =
                    Modifier.fillMaxWidth(),

                text = "Cancel",

                onClick = onBack
            )
        }
    }
}