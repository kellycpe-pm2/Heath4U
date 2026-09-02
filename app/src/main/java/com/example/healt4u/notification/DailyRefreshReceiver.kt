package com.example.healt4u.notification

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Fires once a day (see DailyRefreshScheduler) even if the app is closed, so
// today's dose alarms and expiry/low-stock notifications get (re)built
// without the user needing to open the app first. Reschedules itself for the
// next day when done.
class DailyRefreshReceiver : BroadcastReceiver() {

    // POST_NOTIFICATIONS is checked at runtime inside NotificationHelper before
    // any notification is shown, so a denied permission just means this refresh
    // silently skips notifying (the alarms and schedule data still update).
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
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
