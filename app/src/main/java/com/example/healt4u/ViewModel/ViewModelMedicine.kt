package com.example.healt4u.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Storage.deleteMedicine
import com.example.healt4u.Storage.getAllMedicines
import com.example.healt4u.Storage.getNextMedicineId
import com.example.healt4u.Storage.insertSingleMedicine
import com.example.healt4u.Storage.updateMedicineQuantity
import com.example.healt4u.model.Medicine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewModelMedicine : ViewModel() {


    private val _medicines =
        MutableStateFlow<List<Medicine>>(
            emptyList()
        )

    val medicines: StateFlow<List<Medicine>> =
        _medicines




    fun loadMedicines() {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            val result =
                getAllMedicines()

            _medicines.value = result
        }
    }


    fun add_m( //currently juz for supabase after will change to local and then supabase for cloud pattern
        medicine: Medicine
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            val success =
                insertSingleMedicine(medicine)


            if (success) {
                _medicines.update { medicines -> medicines + medicine  }
            } else {

            }
        }
    }



    fun remove_m(
        medicine: Medicine
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            val medicineId =
                medicine.id


            val success =
                deleteMedicine(medicineId)


            if (success) {

                _medicines.update { medicines -> medicines - medicine  }

            }
        }
    }



    fun updateQuantity(
        medicine: Medicine,
        newQuantity: Int
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            val success =
                updateMedicineQuantity(
                    id = medicine.id,
                    newQuantity = newQuantity
                )


            if (success) {

                _medicines.update { list ->

                    list.map {

                        if (
                            it.id ==
                            medicine.id
                        ) {

                            it.copy(
                                quantityLeft =
                                    newQuantity
                            )

                        } else {

                            it
                        }
                    }
                }
            }
        }
    }


    private val _input_med_name =
        MutableStateFlow("")

    val input_med_name:
            StateFlow<String> =
        _input_med_name


    private val _input_category =
        MutableStateFlow("")

    val input_category:
            StateFlow<String> =
        _input_category


    private val _input_dosage =
        MutableStateFlow(0)

    val input_dosage:
            StateFlow<Int> =
        _input_dosage


    private val _input_quantity =
        MutableStateFlow(0)

    val input_quantity:
            StateFlow<Int> =
        _input_quantity


    private val _input_remark =
        MutableStateFlow("")

    val input_remark:
            StateFlow<String> =
        _input_remark


    private val _input_ExpiredDate =
        MutableStateFlow(
            System.currentTimeMillis()
        )

    val input_ExpiredDate:
            StateFlow<Long> =
        _input_ExpiredDate


    private val _input_afterEat =
        MutableStateFlow(true)

    val input_afterEat:
            StateFlow<Boolean> =
        _input_afterEat


    private val _input_priority =
        MutableStateFlow(0f)

    val input_priority:
            StateFlow<Float> =
        _input_priority


    fun on_Med_Name_Change(
        value: String
    ) {
        _input_med_name.value = value
    }


    fun on_Category_Change(
        value: String
    ) {
        _input_category.value = value
    }


    fun on_Dos_Change(
        value: Int
    ) {
        _input_dosage.value = value
    }


    fun on_Quantity_Change(
        value: Int
    ) {
        _input_quantity.value = value
    }


    fun on_Remark_Change(
        value: String
    ) {
        _input_remark.value = value
    }


    fun on_ExpiredDate_Change(
        value: Long
    ) {
        _input_ExpiredDate.value = value
    }


    fun on_AfterEat_Change(
        value: Boolean
    ) {
        _input_afterEat.value = value
    }


    fun on_Priority_Change(
        value: Float
    ) {
        _input_priority.value = value
    }

    fun addMedicneForm() {

        viewModelScope.launch(
            Dispatchers.IO
        ) {


            val name =
                _input_med_name.value.trim()

            val category =
                _input_category.value
                    .trim()

            val dosage =
                _input_dosage.value

            val quantity =
                _input_quantity.value

            val priority =
                _input_priority.value


            if (name.isEmpty()) {



                return@launch
            }


            if (dosage <= 0) {


                return@launch
            }


            if (quantity < 0) {



                return@launch
            }


            if (
                priority < 0f ||
                priority > 5f
            ) {

                return@launch
            }

            val nextId =
                getNextMedicineId()

            val medicine =
                Medicine(

                    id = nextId,

                    name_medicine =
                        name,

                    category =
                        category.ifEmpty {
                            "General"
                        },

                    dosage =
                        dosage,

                    quantity =
                        quantity,

                    quantityLeft =
                        quantity,

                    remark =
                        _input_remark.value,

                    expiredDate =
                        _input_ExpiredDate.value,

                    afterEat =
                        _input_afterEat.value,

                    createDate =
                        System.currentTimeMillis(),

                    priority =
                        priority,

                    ic =
                        "1"
                )

            val success =
                insertSingleMedicine(
                    medicine
                )


            if (success) {

                val updatedList =
                    getAllMedicines()

                _medicines.value =
                    updatedList



                clearForm()

            } else {
            }
        }
    }



    private fun clearForm() {

        _input_med_name.value = ""

        _input_category.value = ""

        _input_dosage.value = 0

        _input_quantity.value = 0

        _input_remark.value = ""

        _input_ExpiredDate.value =
            System.currentTimeMillis()

        _input_afterEat.value = true

        _input_priority.value = 0f
    }



    fun updateList(
        newList: List<Medicine>
    ) {

        _medicines.value =
            newList
    }
}