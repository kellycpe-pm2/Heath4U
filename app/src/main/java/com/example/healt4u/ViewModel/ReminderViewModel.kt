package com.example.healt4u.ViewModel

import android.Manifest
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Storage.upsertReminderLog
import com.example.healt4u.data.local.upsertReminderLogLocal
import com.example.healt4u.model.MedicineAlert
import com.example.healt4u.model.ReminderLog
import com.example.healt4u.notification.ReminderEngine
import com.example.healt4u.notification.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val application: Application
) : AndroidViewModel(application) {

    private val _todaySchedule = MutableStateFlow<List<ReminderLog>>(emptyList())
    val todaySchedule: StateFlow<List<ReminderLog>> = _todaySchedule

    private val _medicineAlerts = MutableStateFlow<List<MedicineAlert>>(emptyList())
    val medicineAlerts: StateFlow<List<MedicineAlert>> = _medicineAlerts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun todayDate(): String = ReminderEngine.todayDate()

    // Delegates the actual schedule-building, alarm-booking, and alert-checking
    // to ReminderEngine — the same engine DailyRefreshReceiver/BootReceiver use
    // in the background, so "today's schedule" is identical whether or not the
    // user has opened the app today.
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun loadTodaySchedule(context: Context, patientId: Int = 0, date: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = ReminderEngine.refresh(context, date, patientId)
                _todaySchedule.value = result.schedule
                _medicineAlerts.value = result.alerts
            } catch (e: Exception) {
                Log.e("APPT_DEBUG", "Error loading schedule", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markStatus(context: Context, log: ReminderLog, status: String) {
        val updated = log.copy(status = status)
        _todaySchedule.value = _todaySchedule.value.map { if (it.id == updated.id) updated else it }
        if (updated.medicineId != -1) {
            ReminderScheduler.cancelAlarm(context, updated)
        }

        viewModelScope.launch(Dispatchers.IO) {
            upsertReminderLogLocal(context, updated)
            upsertReminderLog(updated)
        }
    }

    fun markTaken(context: Context, log: ReminderLog) = markStatus(context, log, "TAKEN")
    fun markMissed(context: Context, log: ReminderLog) = markStatus(context, log, "MISSED")

    fun adherenceCount(): Pair<Int, Int> {
        val medicineLogs = _todaySchedule.value.filter { it.medicineId != -1 }
        val taken = medicineLogs.count { it.status == "TAKEN" }
        return taken to medicineLogs.size
    }

    // Excludes appointments — a missed appointment isn't a "missed dose"
    fun missedLogs(): List<ReminderLog> = _todaySchedule.value.filter { it.status == "MISSED" && it.medicineId != -1 }

    //appointment reminder
    fun addAppointmentReminder(
        hospitalName: String,
        doctorName: String,
        date: String,
        time: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val reminderLog = ReminderLog(
                id = "appt_${date}_${time}_${doctorName.hashCode()}",
                patientId = 0,
                medicineId = -1,
                medicineName = "Appointment: $doctorName",
                date = date,
                time = time,
                status = "PENDING",
                type = "APPOINTMENT"
            )

            upsertReminderLogLocal(getApplication(), reminderLog)
            upsertReminderLog(reminderLog)
        }
    }
}
