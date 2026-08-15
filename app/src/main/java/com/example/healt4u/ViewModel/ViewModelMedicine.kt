package com.example.healt4u.ViewModel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import com.example.healt4u.model.Medicine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ViewModelMedicine : ViewModel(){
    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines : StateFlow<List<Medicine>> = _medicines

    fun add_m(m: Medicine){
        _medicines.update { current -> current + m  }
    }

    fun remove_m (m :Medicine){
        _medicines.update {current -> current - m}
    }
    private val _input_med_name = MutableStateFlow("")
    val input_med_name : StateFlow<String> = _input_med_name
    private val _input_category= MutableStateFlow("")
    val input_category : StateFlow<String> = _input_category


    private val _input_dosage= MutableStateFlow(0)
    val input_dosage : StateFlow<Int> = _input_dosage

    private val _input_quantity= MutableStateFlow(0)
    val input_quantity : StateFlow<Int> = _input_quantity


    private val _input_remark= MutableStateFlow("")
    val input_remark : StateFlow<String> = _input_remark

    private val _input_ExpiredDate = MutableStateFlow(System.currentTimeMillis())
    val input_ExpiredDate : StateFlow <Long> = _input_ExpiredDate

    private val _input_afterEat= MutableStateFlow(true)
    val input_afterEat : StateFlow<Boolean> = _input_afterEat

    private val _input_priority= MutableStateFlow(0)
    val input_priority : StateFlow<Int> = _input_priority

    fun on_Med_Name_Change (m_name :String){ _input_med_name.value = m_name }
    fun on_Category_Change (m_category :String){ _input_category.value = m_category }
    fun on_Dos_Change (m_Dos :Int){ _input_dosage.value = m_Dos }
    fun on_Quantity_Change (m_quantity :Int){ _input_quantity.value = m_quantity }
    fun on_Remark_Change (m_remark :String){ _input_remark.value = m_remark }
    fun on_ExpiredDate_Change (m_ExpiredDate :Long){ _input_ExpiredDate.value = m_ExpiredDate }
    fun on_AfterEat_Change (m_afterEat :Boolean){ _input_afterEat.value = m_afterEat }
    fun on_Priority_Change (m_priority :Int){ _input_priority.value = m_priority }

    private var nextId = 0

    fun addMedicneForm (){
        val med_name = _input_med_name.value.trim()
        val dos = _input_dosage.value
        val quantity = _input_quantity.value

        if (med_name.isEmpty() && dos ==0 && quantity == 0  ){
            return
        }
        add_m(Medicine(id = nextId++, name_medicine = med_name, category = _input_category.value, dosage = dos, quantity = quantity, remark = _input_remark.value, expiredDate = _input_ExpiredDate.value, afterEat = _input_afterEat.value, create_Date =System.currentTimeMillis() , priority = _input_priority.value))
        _input_med_name.value=""
        _input_dosage.value =0
        _input_quantity.value =0
        _input_category.value=""
        _input_remark.value =""
        _input_ExpiredDate.value=0
        _input_priority.value =0
    }



}