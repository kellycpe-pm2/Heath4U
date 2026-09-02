package com.example.healt4u.notification

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// AlarmManager alarms are wiped when the device reboots, so this rebuilds
// today's schedule (and re-books every dose alarm) as soon as the device
// restarts, then books the recurring daily refresh again.
class BootReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ReminderEngine.refresh(context.applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                DailyRefreshScheduler.scheduleNextRefresh(context.applicationContext)
                pendingResult.finish()
            }
        }
    }
}
