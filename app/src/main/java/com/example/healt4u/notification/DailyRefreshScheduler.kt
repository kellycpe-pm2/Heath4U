package com.example.healt4u.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

// Books a single daily wake-up (06:00) that runs DailyRefreshReceiver. Uses a
// self-rescheduling pattern — the receiver books tomorrow's alarm itself when
// it finishes — rather than AlarmManager.setRepeating, which drifts over time
// and can be skipped entirely under Doze.
object DailyRefreshScheduler {

    private const val REFRESH_HOUR = 6
    private const val REQUEST_CODE = 9001

    fun scheduleNextRefresh(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, REFRESH_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, DailyRefreshReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent)
    }
}
