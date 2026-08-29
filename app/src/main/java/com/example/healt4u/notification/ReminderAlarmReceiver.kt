package com.example.healt4u.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Registered in AndroidManifest.xml. AlarmManager wakes the app up (even if
// killed) at each dose's scheduled time and this posts the notification.
class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME) ?: "your medicine"
        val time = intent.getStringExtra(EXTRA_TIME) ?: ""
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        NotificationHelper.createChannels(context)
        NotificationHelper.showDoseReminder(context, notificationId, medicineName, time)
    }

    companion object {
        const val EXTRA_MEDICINE_NAME = "medicine_name"
        const val EXTRA_TIME = "time"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
