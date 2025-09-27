package com.eblan.launcher.framework.notificationmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultNotificationManagerWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    AndroidNotificationManagerWrapper {
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun createNotificationChannel(
        channelId: String,
        name: String,
        importance: Int
    ) {
        val channel = NotificationChannel(
            channelId,
            "Eblan Launcher Service",
            NotificationManager.IMPORTANCE_DEFAULT,
        )

        notificationManager.createNotificationChannel(channel)
    }
}