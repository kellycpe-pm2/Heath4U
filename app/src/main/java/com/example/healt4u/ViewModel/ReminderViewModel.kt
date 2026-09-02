package com.example.healt4u.ViewModel

import android.Manifest
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
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
import com.example.healt4u.model.MedicineAlert
import com.example.healt4u.model.ReminderLog
import com.example.healt4u.notification.NotificationHelper
import com.example.healt4u.notification.ReminderScheduler
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

    private val _medicineAlerts = MutableStateFlow<List<MedicineAlert>>(emptyList())
    val medicineAlerts: StateFlow<List<MedicineAlert>> = _medicineAlerts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Prevents re-notifying for the same alert every time the dashboard reloads
    // in the same app session (e.g. navigating back and forth).
    private val notifiedAlertKeys = mutableSetOf<String>()
    private val scheduledAlarmIds = mutableSetOf<String>()

    private val EXPIRY_WARNING_DAYS = 7
    private val LOW_STOCK_THRESHOLD = 5

    // Doses are spaced across waking hours only (07:00-22:00), not the full
    // 24-hour clock, so a medicine taken 5x/day doesn't get scheduled at 3 AM.
    private val WAKING_START_MINUTES = 7 * 60
    private val WAKING_END_MINUTES = 22 * 60

    fun todayDate(): String = dateFormat.format(Calendar.getInstance().time)

    // Builds today's schedule from every saved medicine (reminder_time + times_per_day),
    // then overlays any status already recorded for today so marks survive app restarts.
    // In ReminderViewModel.kt
    fun loadTodaySchedule(
        context: Context,
        patientId: Int = 0,
        selectedDate: String = todayDate()  // Default = today, but OVERRIDE-able
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                NotificationHelper.createChannels(context)

                // USE THE PASSED DATE, NOT FORCE TODAY
                val date = selectedDate  // ← CHANGED

                val medicines = load_Medicines(context)
                val generated = generateSlotsFor(medicines, date, patientId)

                val savedLocal = loadReminderLogsForDate(context, date)
                val appointments = savedLocal.filter { it.medicineId == -1 || it.medicineId == null }
                var merged = mergeGeneratedWithSaved(generated, savedLocal)
                var allItems = (merged + appointments).distinctBy { it.id }

                allItems = flagOverdueAsMissed(allItems.sortedBy { it.time },date)
                _todaySchedule.value = allItems

                // Cloud sync
                val savedCloud = getReminderLogsForDate(date)
                if (savedCloud.isNotEmpty()) {
                    merged = mergeGeneratedWithSaved(generated, savedCloud)
                    val cloudAppointments = savedCloud.filter { it.medicineId == -1 || it.medicineId == null }
                    allItems = (merged + cloudAppointments).distinctBy { it.id }
                    allItems = flagOverdueAsMissed(allItems.sortedBy { it.time },date)
                    _todaySchedule.value = allItems
                    upsertReminderLogsLocal(context, allItems)
                }

                scheduleAlarmsForPendingDoses(context, allItems.filter { it.medicineId != -1 })
                checkMedicineAlerts(context, medicines)
            } catch (e: Exception) {
                Log.e("APPT_DEBUG", "Error loading schedule", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Schedules a notification for every dose that's still ahead of us today.
    // Alarms are keyed by log.id, so re-running this on every dashboard load is
    // safe — AlarmManager just replaces the pending alarm for the same id.
    private fun scheduleAlarmsForPendingDoses(context: Context, logs: List<ReminderLog>) {
        for (log in logs) {
            if (log.status == "PENDING" && log.id !in scheduledAlarmIds) {
                ReminderScheduler.scheduleAlarm(context, log)
                scheduledAlarmIds.add(log.id)
            }
        }
    }

    // 7 days before expiry, or stock at/under the low-stock threshold — posts a
    // notification once per app session per medicine, and always refreshes the
    // banner list shown on Dashboard/Schedule.
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun checkMedicineAlerts(context: Context, medicines: List<Medicine>) {
        val now = System.currentTimeMillis()
        val warningWindowMillis = EXPIRY_WARNING_DAYS * 24L * 60 * 60 * 1000
        val alerts = mutableListOf<MedicineAlert>()

        for (med in medicines) {
            val expiredDate = med.expiredDate
            if (expiredDate != null) {
                val msUntilExpiry = expiredDate - now
                if (msUntilExpiry in 0..warningWindowMillis) {
                    val daysLeft = (msUntilExpiry / (24 * 60 * 60 * 1000)).toInt()
                    alerts.add(
                        MedicineAlert(
                            medicineId = med.id,
                            medicineName = med.name_medicine,
                            kind = MedicineAlert.Kind.EXPIRING_SOON,
                            message = "${med.name_medicine} expires in $daysLeft day${if (daysLeft != 1) "s" else ""}"
                        )
                    )
                } else if (msUntilExpiry < 0) {
                    alerts.add(
                        MedicineAlert(
                            medicineId = med.id,
                            medicineName = med.name_medicine,
                            kind = MedicineAlert.Kind.EXPIRING_SOON,
                            message = "${med.name_medicine} has expired"
                        )
                    )
                }
            }

            val quantityLeft = med.quantityLeft ?: med.quantity
            if (quantityLeft <= LOW_STOCK_THRESHOLD) {
                alerts.add(
                    MedicineAlert(
                        medicineId = med.id,
                        medicineName = med.name_medicine,
                        kind = MedicineAlert.Kind.LOW_STOCK,
                        message = "${med.name_medicine} is running low ($quantityLeft left)"
                    )
                )
            }
        }

        _medicineAlerts.value = alerts

        for (alert in alerts) {
            val key = "${alert.medicineId}_${alert.kind}_${todayDate()}"
            if (key !in notifiedAlertKeys) {
                notifiedAlertKeys.add(key)
                val title = if (alert.kind == MedicineAlert.Kind.EXPIRING_SOON) "Medicine expiring soon" else "Medicine running low"
                NotificationHelper.showStockAlert(context, key.hashCode(), title, alert.message)
            }
        }
    }

    private fun generateSlotsFor(medicines: List<Medicine>, date: String, patientId: Int): List<ReminderLog> {
        val slots = mutableListOf<ReminderLog>()
        for (med in medicines) {
            val timesPerDay = (med.timesPerDay ?: 1).coerceIn(1, 6)
            val startTime = med.reminderTime ?: "08:00"
            val startMinutes = timeStringToMinutes(startTime).coerceIn(WAKING_START_MINUTES, WAKING_END_MINUTES)

            // Spread the remaining doses evenly between the chosen start time and
            // the end of waking hours, e.g. 08:00 start + 5x/day -> roughly every
            // 2h45m up to 22:00, instead of wrapping into the middle of the night.
            val windowMinutes = (WAKING_END_MINUTES - startMinutes).coerceAtLeast(0)
            val intervalMinutes = if (timesPerDay > 1) windowMinutes / (timesPerDay - 1) else 0

            for (slot in 0 until timesPerDay) {
                val minutesOfDay = (startMinutes + slot * intervalMinutes).coerceAtMost(WAKING_END_MINUTES)
                val timeLabel = minutesToTimeString(minutesOfDay)
                slots.add(
                    ReminderLog(
                        id = "${med.id}_${date}_$slot",
                        medicineId = med.id,
                        medicineName = med.name_medicine,
                        date = date,
                        time = timeLabel,
                        status = "PENDING",
                        type = "MEDICINE",
                        patientId = patientId
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
    private fun flagOverdueAsMissed(logs: List<ReminderLog>, scheduleDate: String): List<ReminderLog> {
        val todayDate = todayDate()
        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val graceMinutes = 30

        return logs.map { log ->
            if (log.status == "PENDING") {
                // ONLY check time if viewing TODAY
                if (scheduleDate != todayDate) {
                    // PAST date → MISSED, FUTURE date → STAY PENDING
                    if (scheduleDate < todayDate) {
                        log.copy(status = "MISSED")
                    } else {
                        log // FUTURE → keep PENDING
                    }
                } else {
                    // SAME DAY → check if time passed
                    val slotMinutes = timeStringToMinutes(log.time)
                    if (nowMinutes - slotMinutes > graceMinutes) {
                        log.copy(status = "MISSED")
                    } else log
                }
            } else log
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
                status = "PENDING",
                type = "APPOINTMENT",
                patientId = 0
            )

            upsertReminderLogLocal(getApplication(), reminderLog)
            upsertReminderLog(reminderLog)
        }
    }
}
