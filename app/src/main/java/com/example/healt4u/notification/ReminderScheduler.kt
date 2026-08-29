package com.example.healt4u.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.healt4u.model.ReminderLog
import java.util.Calendar

// Uses AlarmManager.setAndAllowWhileIdle — inexact but Doze-aware, and unlike
// setExactAndAllowWhileIdle it needs no SCHEDULE_EXACT_ALARM permission, so
// there's nothing extra for the user to grant in system settings before a demo.
object ReminderScheduler {

    fun scheduleAlarm(context: Context, log: ReminderLog) {
        val triggerAtMillis = todayAtTimeMillis(log.time) ?: return
        if (triggerAtMillis <= System.currentTimeMillis()) return // already passed, don't schedule

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildPendingIntent(context, log)

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun cancelAlarm(context: Context, log: ReminderLog) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(buildPendingIntent(context, log))
    }

    private fun buildPendingIntent(context: Context, log: ReminderLog): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_MEDICINE_NAME, log.medicineName)
            putExtra(ReminderAlarmReceiver.EXTRA_TIME, log.time)
            putExtra(ReminderAlarmReceiver.EXTRA_NOTIFICATION_ID, log.id.hashCode())
        }
        return PendingIntent.getBroadcast(
            context,
            log.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun todayAtTimeMillis(time: String): Long? {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return null

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
