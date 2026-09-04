package com.example.healt4u.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.healt4u.R
import com.example.healt4u.Session.CurrentSession
import com.example.healt4u.Storage.getReminderLogsForDate
import com.example.healt4u.data.local.load_Medicines
import com.example.healt4u.data.local.loadReminderLogsForDate
import com.example.healt4u.data.local.upsertReminderLogsLocal
import com.example.healt4u.model.Medicine
import com.example.healt4u.model.MedicineAlert
import com.example.healt4u.model.ReminderLog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Headless "build today's schedule + book alarms + check alerts" logic for
// whichever patient is currently logged in (CurrentSession.patientId).
// DailyRefreshReceiver and BootReceiver call this directly — with no UI and
// no ViewModel — so dose/stock notifications keep firing on days the user
// never opens the app. Mirrors ReminderViewModel's own schedule-building
// logic; kept as a separate, self-contained copy on purpose so this can run
// standalone from a BroadcastReceiver without depending on the ViewModel.
object ReminderEngine {

    data class RefreshResult(
        val schedule: List<ReminderLog>,
        val alerts: List<MedicineAlert>
    )

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Per-process dedupe — fine since each background run is short-lived and
    // each day gets a fresh window anyway.
    private val notifiedAlertKeys = mutableSetOf<String>()
    private val scheduledAlarmIds = mutableSetOf<String>()

    private const val EXPIRY_WARNING_DAYS = 7
    private const val LOW_STOCK_THRESHOLD = 5
    private const val WAKING_START_MINUTES = 7 * 60
    private const val WAKING_END_MINUTES = 22 * 60

    fun todayDate(): String = dateFormat.format(Calendar.getInstance().time)

    // Only ever refreshes TODAY for the currently logged-in patient — that's
    // the only case where booking alarms/notifications makes sense in the
    // background.
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    suspend fun refresh(context: Context): RefreshResult {
        val patientId = CurrentSession.patientId
        NotificationHelper.createChannels(context)

        val date = todayDate()
        val medicines = load_Medicines(context)
        val generated = generateSlotsFor(medicines, date, patientId)

        val savedLocal = loadReminderLogsForDate(context, date)
        val appointments = savedLocal.filter { it.medicineId == -1 }
        var merged = mergeGeneratedWithSaved(generated, savedLocal)
        var allItems = (merged + appointments).distinctBy { it.id }
        allItems = flagOverdueAsMissed(allItems.sortedBy { it.time })

        val savedCloud = getReminderLogsForDate(date, patientId)
        if (savedCloud.isNotEmpty()) {
            merged = mergeGeneratedWithSaved(generated, savedCloud)
            val cloudAppointments = savedCloud.filter { it.medicineId == -1 }
            allItems = (merged + cloudAppointments).distinctBy { it.id }
            allItems = flagOverdueAsMissed(allItems.sortedBy { it.time })
            upsertReminderLogsLocal(context, allItems)
        }

        scheduleAlarmsForPendingDoses(context, allItems.filter { it.medicineId != -1 })
        val alerts = checkMedicineAlerts(context, medicines)

        return RefreshResult(allItems, alerts)
    }

    private fun scheduleAlarmsForPendingDoses(context: Context, logs: List<ReminderLog>) {
        for (log in logs) {
            if (log.status == "PENDING" && log.id !in scheduledAlarmIds) {
                ReminderScheduler.scheduleAlarm(context, log)
                scheduledAlarmIds.add(log.id)
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun checkMedicineAlerts(context: Context, medicines: List<Medicine>): List<MedicineAlert> {
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

        for (alert in alerts) {
            val key = "${alert.medicineId}_${alert.kind}_${todayDate()}"
            if (key !in notifiedAlertKeys) {
                notifiedAlertKeys.add(key)
                val title = if (alert.kind == MedicineAlert.Kind.EXPIRING_SOON) "Medicine expiring soon" else "Medicine running low"
                NotificationHelper.showStockAlert(context, key.hashCode(), title, alert.message)
            }
        }

        return alerts
    }

    private fun generateSlotsFor(medicines: List<Medicine>, date: String, patientId: Int): List<ReminderLog> {
        val slots = mutableListOf<ReminderLog>()
        for (med in medicines) {
            val timesPerDay = (med.timesPerDay ?: 1).coerceIn(1, 6)
            val startTime = med.reminderTime ?: "08:00"
            val startMinutes = timeStringToMinutes(startTime).coerceIn(WAKING_START_MINUTES, WAKING_END_MINUTES)

            val windowMinutes = (WAKING_END_MINUTES - startMinutes).coerceAtLeast(0)
            val intervalMinutes = if (timesPerDay > 1) windowMinutes / (timesPerDay - 1) else 0

            for (slot in 0 until timesPerDay) {
                val minutesOfDay = (startMinutes + slot * intervalMinutes).coerceAtMost(WAKING_END_MINUTES)
                val timeLabel = minutesToTimeString(minutesOfDay)
                slots.add(
                    ReminderLog(
                        id = "${patientId}_${med.id}_${date}_$slot",
                        patientId = patientId,
                        medicineId = med.id,
                        medicineName = med.name_medicine,
                        date = date,
                        time = timeLabel,
                        status = "PENDING",
                        type = "MEDICINE"
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

    // Background refresh only ever runs for "today", so this is the simpler
    // same-day-only version (no past/future date branching needed here).
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
}

object Notification {
    const val CHANNEL_ID = "chat_messages"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New chat message notifications"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun showSafely(context: Context, title: String, message: String) {
        if (!hasPermission(context)) return
        try {
            show(context, title, message)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun hasPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun show(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}