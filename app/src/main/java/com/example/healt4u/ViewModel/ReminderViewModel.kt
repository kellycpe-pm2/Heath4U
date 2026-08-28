package com.example.healt4u.ViewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healt4u.Storage.getReminderLogsForDate
import com.example.healt4u.Storage.upsertReminderLog
import com.example.healt4u.Storage.upsertReminderLogs
import com.example.healt4u.data.local.load_Medicines
import com.example.healt4u.data.local.loadReminderLogsForDate
import com.example.healt4u.data.local.upsertReminderLogLocal
import com.example.healt4u.data.local.upsertReminderLogsLocal
import com.example.healt4u.model.Medicine
import com.example.healt4u.model.ReminderLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReminderViewModel(
    private val application: Application
) : AndroidViewModel(application) {

    private val _todaySchedule = MutableStateFlow<List<ReminderLog>>(emptyList())
    val todaySchedule: StateFlow<List<ReminderLog>> = _todaySchedule

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun todayDate(): String = dateFormat.format(Calendar.getInstance().time)

    // Builds today's schedule from every saved medicine (reminder_time + times_per_day),
    // then overlays any status already recorded for today so marks survive app restarts.
    fun loadTodaySchedule(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val date = todayDate()

                val medicines = load_Medicines(context)
                val generated = generateSlotsFor(medicines, date)

                val savedLocal = loadReminderLogsForDate(context, date)

                // Merge medicine reminders + appointments
                val appointments = savedLocal.filter { it.medicineId == -1 }
                var merged = mergeGeneratedWithSaved(generated, savedLocal)
                var allItems = (merged + appointments).distinctBy { it.id }

                allItems = flagOverdueAsMissed(allItems.sortedBy { it.time })
                _todaySchedule.value = allItems

                // Cloud sync
                val savedCloud = getReminderLogsForDate(date)
                if (savedCloud.isNotEmpty()) {
                    merged = mergeGeneratedWithSaved(generated, savedCloud)
                    val cloudAppointments = savedCloud.filter { it.medicineId == -1 }
                    allItems = (merged + cloudAppointments).distinctBy { it.id }
                    allItems = flagOverdueAsMissed(allItems.sortedBy { it.time })
                    _todaySchedule.value = allItems
                    upsertReminderLogsLocal(context, allItems)
                }
            } catch (e: Exception) {
                android.util.Log.e("APPT_DEBUG", "Error loading schedule", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateSlotsFor(medicines: List<Medicine>, date: String): List<ReminderLog> {
        val slots = mutableListOf<ReminderLog>()
        for (med in medicines) {
            val timesPerDay = (med.timesPerDay ?: 1).coerceIn(1, 6)
            val startTime = med.reminderTime ?: "08:00"
            val startMinutes = timeStringToMinutes(startTime)
            val intervalMinutes = (24 * 60) / timesPerDay

            for (slot in 0 until timesPerDay) {
                val minutesOfDay = (startMinutes + slot * intervalMinutes) % (24 * 60)
                val timeLabel = minutesToTimeString(minutesOfDay)
                slots.add(
                    ReminderLog(
                        id = "${med.id}_${date}_$slot",
                        medicineId = med.id,
                        medicineName = med.name_medicine,
                        date = date,
                        time = timeLabel,
                        status = "PENDING"
                    )
                )
            }
        }
        return slots.sortedBy { it.time }
    }

    private fun mergeGeneratedWithSaved(
        generated: List<ReminderLog>,
        saved: List<ReminderLog>
    ): List<ReminderLog> {
        val savedById = saved.associateBy { it.id }
        return generated.map { savedById[it.id] ?: it }
    }

    // Anything still PENDING more than 30 minutes past its slot time is auto-flagged
    // MISSED — this is what drives the family "missed-dose alert" on the dashboard.
    private fun flagOverdueAsMissed(logs: List<ReminderLog>): List<ReminderLog> {
        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val graceMinutes = 30

        return logs.map { log ->
            if (log.status == "PENDING") {
                val slotMinutes = timeStringToMinutes(log.time)
                if (nowMinutes - slotMinutes > graceMinutes) {
                    log.copy(status = "MISSED")
                } else log
            } else log
        }
    }

    fun markStatus(context: Context, log: ReminderLog, status: String) {
        val updated = log.copy(status = status)
        _todaySchedule.value = _todaySchedule.value.map { if (it.id == updated.id) updated else it }

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

    fun missedLogs(): List<ReminderLog> = _todaySchedule.value.filter { it.status == "MISSED" }

    private fun timeStringToMinutes(time: String): Int {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }

    private fun minutesToTimeString(totalMinutes: Int): String {
        val h = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        return "%02d:%02d".format(h, m)
    }

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
                medicineId = -1,
                medicineName = "Appointment: $doctorName",
                date = date,
                time = time,
                status = "PENDING"
            )

            upsertReminderLogLocal(getApplication(), reminderLog)
            upsertReminderLog(reminderLog)
        }
    }
}
