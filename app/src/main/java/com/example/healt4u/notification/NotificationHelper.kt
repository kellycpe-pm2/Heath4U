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
import com.example.healt4u.notification.Notification.CHANNEL_ID

// Two channels: one for "time to take your medicine" alarms, one for the
// less urgent expiry/low-stock heads-up. Kept separate so a user could mute
// stock alerts without losing dose reminders.
object NotificationHelper {

    const val CHANNEL_DOSE = "health4u_dose_reminders"
    const val CHANNEL_STOCK = "health4u_stock_alerts"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)

        val doseChannel = NotificationChannel(
            CHANNEL_DOSE,
            "Medication reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts you when it's time to take a medicine"
        }

        val stockChannel = NotificationChannel(
            CHANNEL_STOCK,
            "Stock & expiry alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Warns about medicines running low or expiring soon"
        }

        manager?.createNotificationChannel(doseChannel)
        manager?.createNotificationChannel(stockChannel)
    }

    private fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showDoseReminder(context: Context, notificationId: Int, medicineName: String, time: String) {
        if (!hasPermission(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_DOSE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time to take $medicineName")
            .setContentText("Scheduled for $time — tap to open Health4U")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(notificationId, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showStockAlert(context: Context, notificationId: Int, title: String, message: String) {
        if (!hasPermission(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_STOCK)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(notificationId, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showMissedDoseAlert(context: Context, notificationId: Int, medicineName: String, scheduledTime: String) {
        if (!hasPermission(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_DOSE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Your patient miss a dose")
            .setContentText("$medicineName was scheduled for $scheduledTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(notificationId, notification)
    }

    // Add this inside NotificationHelper object
    fun showChatMessage(context: Context, senderName: String, message: String) {
        createChannels(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Message from $senderName")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
