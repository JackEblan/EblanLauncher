package com.eblan.launcher.framework.notificationmanager

import android.os.Build
import androidx.annotation.RequiresApi

interface AndroidNotificationManagerWrapper {

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(
        channelId: String,
        name: String,
        importance: Int
    )

    companion object {
        const val CHANNEL_ID = "Eblan Launcher"
    }
}