package com.accident.detection

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    const val CHANNEL_ID = "bt_channel"
    const val NOTIFICATION_ID = 1

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Bluetooth Messages",
                    NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun baseNotification(context: Context): Notification {
        ensureChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Accident Detection")
            .setContentText("Listening for messages…")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()
    }

    fun pushMessage(context: Context, message: String) {
        ensureChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Accident Detection")
            .setContentText(message.trim())
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.trim()))
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
