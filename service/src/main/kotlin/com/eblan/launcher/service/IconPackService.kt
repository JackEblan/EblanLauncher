package com.eblan.launcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.usecase.UpdateIconPackUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IconPackService : Service() {
    @Inject
    lateinit var updateIconPackUseCase: UpdateIconPackUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var updateIconPackJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val iconPackPackageName = intent?.getStringExtra(IconPackManager.ICON_PACK_PACKAGE_NAME)

        if (iconPackPackageName != null) {
            ServiceCompat.startForeground(
                this,
                1,
                createNotification(),

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                } else {
                    0
                },
            )

            updateIconPackJob?.cancel()

            serviceScope.launch {
                updateIconPackJob = launch {
                    updateIconPackUseCase(iconPackPackageName = iconPackPackageName)

                    stopForeground(STOP_FOREGROUND_REMOVE)

                    stopSelf()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
    }

    fun createNotification(): Notification {
        val channelId = "Eblan Launcher"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Eblan Launcher Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_cached_24)
            .setContentTitle("Eblan Launcher")
            .setContentText("Importing icon pack to cache, This may take some time")
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()
    }
}