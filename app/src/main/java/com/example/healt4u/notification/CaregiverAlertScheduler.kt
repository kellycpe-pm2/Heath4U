package com.example.healt4u.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object CaregiverAlertScheduler {
    private const val WORK_NAME = "caregiver_missed_dose_alerts"
    private const val IMMEDIATE_WORK_NAME = "$WORK_NAME-now"

    fun start(context: Context, caregiverUserId: Int) {
        if (caregiverUserId == 0) return

        val immediateRequest = OneTimeWorkRequestBuilder<CaregiverAlertWorker>()
            .setInputData(
                Data.Builder()
                    .putInt(CaregiverAlertWorker.KEY_CAREGIVER_ID, caregiverUserId)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            immediateRequest
        )

        val request = PeriodicWorkRequestBuilder<CaregiverAlertWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                Data.Builder()
                    .putInt(CaregiverAlertWorker.KEY_CAREGIVER_ID, caregiverUserId)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun runNow(context: Context, caregiverUserId: Int) {
        if (caregiverUserId == 0) return
        val request = OneTimeWorkRequestBuilder<CaregiverAlertWorker>()
            .setInputData(
                Data.Builder()
                    .putInt(CaregiverAlertWorker.KEY_CAREGIVER_ID, caregiverUserId)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
