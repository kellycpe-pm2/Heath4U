package com.example.healt4u.ViewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Storage.upsertFamilyAlertCloud
import com.example.healt4u.Storage.upsertFamilyAlertsCloud
import com.example.healt4u.data.local.loadFamilyAlerts
import com.example.healt4u.data.local.loadReminderLogsForDate
import com.example.healt4u.data.local.saveFamilyAlerts
import com.example.healt4u.model.CaregiverProfile
import com.example.healt4u.model.FamilyAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FamilyModeViewModel(
    private val application: Application
) : AndroidViewModel(application) {

    private val _caregivers = MutableStateFlow<List<CaregiverProfile>>(emptyList())
    val caregivers: StateFlow<List<CaregiverProfile>> = _caregivers

    private val _alerts = MutableStateFlow<List<FamilyAlert>>(emptyList())
    val alerts: StateFlow<List<FamilyAlert>> = _alerts

    private val _patientPhone = MutableStateFlow("")
    val patientPhone: StateFlow<String> = _patientPhone

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun todayDate(): String = dateFormat.format(Calendar.getInstance().time)

    fun loadPatientPhone(context: Context) {
        val prefs = context.getSharedPreferences("family_mode_prefs", Context.MODE_PRIVATE)
        _patientPhone.value = prefs.getString("patient_phone", "") ?: ""
    }

    fun savePatientPhone(context: Context, phone: String) {
        val prefs = context.getSharedPreferences("family_mode_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("patient_phone", phone).apply()
        _patientPhone.value = phone
    }

    fun refreshCaregivers(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = java.io.File(context.filesDir, "caregivers.json")
            if (!file.exists()) return@launch
            try {
                val list = kotlinx.serialization.json.Json.decodeFromString<List<CaregiverProfile>>(file.readText())
                _caregivers.value = list
            } catch (_: Exception) { }
        }
    }

    fun addCaregiver(context: Context, name: String, phone: String, relationship: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = "cg_${System.currentTimeMillis()}"
            val caregiver = CaregiverProfile(
                id = id,
                name = name,
                phone = phone,
                relationship = relationship,
                patientPhone = _patientPhone.value,
                patientId = "p001"
            )
            val updated = _caregivers.value + caregiver
            _caregivers.value = updated
            val json = kotlinx.serialization.json.Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
            val jsonString = json.encodeToString(updated)
            context.openFileOutput("caregivers.json", Context.MODE_PRIVATE).use { it.write(jsonString.toByteArray()) }
        }
    }

    fun removeCaregiver(context: Context, caregiverId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = _caregivers.value.filter { it.id != caregiverId }
            _caregivers.value = updated
            val json = kotlinx.serialization.json.Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
            val jsonString = json.encodeToString(updated)
            context.openFileOutput("caregivers.json", Context.MODE_PRIVATE).use { it.write(jsonString.toByteArray()) }
        }
    }

    fun loadAlerts(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = java.io.File(context.filesDir, "family_alerts.json")
            if (!file.exists()) return@launch
            try {
                val list = kotlinx.serialization.json.Json.decodeFromString<List<FamilyAlert>>(file.readText())
                _alerts.value = list
            } catch (_: Exception) { }
        }
    }

    fun checkOverdueAndCreateAlerts(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val date = todayDate()
                val now = Calendar.getInstance()
                val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                val graceMinutes = 15

                val logs = loadReminderLogsForDate(context, date)
                val existingAlerts = loadFamilyAlerts(context).filter { it.date == date }
                val existingAlertKeys = existingAlerts.map { "${it.medicineName}_${it.scheduledTime}" }.toSet()

                val caregivers = _caregivers.value
                if (caregivers.isEmpty()) {
                    _isLoading.value = false
                    return@launch
                }

                val newAlerts = mutableListOf<FamilyAlert>()
                for (log in logs) {
                    if (log.status != "PENDING") continue
                    val parts = log.time.split(":")
                    val slotMinutes = (parts.getOrNull(0)?.toIntOrNull() ?: 8) * 60 + (parts.getOrNull(1)?.toIntOrNull() ?: 0)
                    if (nowMinutes - slotMinutes <= graceMinutes) continue

                    val key = "${log.medicineName}_${log.time}"
                    if (key in existingAlertKeys) continue

                    for (cg in caregivers) {
                        val alertId = "fa_${log.medicineId}_${date}_${log.time}_${cg.id}"
                        newAlerts.add(
                            FamilyAlert(
                                id = alertId,
                                medicineName = log.medicineName,
                                scheduledTime = log.time,
                                date = date,
                                patientPhone = _patientPhone.value,
                                status = "PENDING",
                                caregiverName = cg.name,
                                caregiverPhone = cg.phone
                            )
                        )
                    }
                }

                if (newAlerts.isNotEmpty()) {
                    val allAlerts = _alerts.value + newAlerts
                    _alerts.value = allAlerts
                    saveFamilyAlerts(context, allAlerts)
                    upsertFamilyAlertsCloud(newAlerts)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAlertCalled(context: Context, alert: FamilyAlert) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = alert.copy(status = "CALLED")
            val all = _alerts.value.map { if (it.id == updated.id) updated else it }
            _alerts.value = all
            saveFamilyAlerts(context, all)
            upsertFamilyAlertCloud(updated)
        }
    }

    fun resolveAlert(context: Context, alert: FamilyAlert) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = alert.copy(
                status = "RESOLVED",
                resolvedAt = System.currentTimeMillis()
            )
            val all = _alerts.value.map { if (it.id == updated.id) updated else it }
            _alerts.value = all
            saveFamilyAlerts(context, all)
            upsertFamilyAlertCloud(updated)
        }
    }
}
