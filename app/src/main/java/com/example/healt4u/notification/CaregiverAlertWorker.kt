package com.example.healt4u.notification

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.healt4u.Storage.getFamilyAlertsForCaregiver

class CaregiverAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val caregiverUserId = inputData.getInt(KEY_CAREGIVER_ID, 0)
        if (caregiverUserId == 0) return Result.success()

        val alerts = getFamilyAlertsForCaregiver(caregiverUserId)
            .filter { it.status == "PENDING" }
        val preferences = applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val notifiedIds = preferences.getStringSet(NOTIFIED_IDS, emptySet()).orEmpty().toMutableSet()

        for (alert in alerts) {
            if (alert.id in notifiedIds) continue
            NotificationHelper.showMissedDoseAlert(
                applicationContext,
                alert.id.hashCode(),
                alert.medicineName,
                alert.scheduledTime
            )
            notifiedIds.add(alert.id)
        }

        preferences.edit().putStringSet(NOTIFIED_IDS, notifiedIds).apply()
        return Result.success()
    }

    companion object {
        const val KEY_CAREGIVER_ID = "caregiver_user_id"
        private const val PREFERENCES = "caregiver_alert_notifications"
        private const val NOTIFIED_IDS = "notified_alert_ids"
    }
}
