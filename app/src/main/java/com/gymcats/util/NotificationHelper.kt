package com.gymcats.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object NotificationHelper {
    const val CHANNEL_ID = "gymcats_channel"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, "GymCats", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Lembretes do GymCats" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun send(context: Context, title: String, message: String, id: Int = 1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(id, notification)
    }
}
