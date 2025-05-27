package com.eblan.launcher.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.eblan.launcher.broadcastreceiver.PackageBroadcastReceiver
import com.eblan.launcher.domain.common.qualifier.ApplicationScope
import com.eblan.launcher.domain.usecase.UpdateEblanApplicationInfosUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApplicationInfoService : Service() {
    @Inject
    lateinit var updateEblanApplicationInfosUseCase: UpdateEblanApplicationInfosUseCase

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    private lateinit var serviceJob: Job

    private lateinit var packageBroadcastReceiver: PackageBroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        packageBroadcastReceiver = PackageBroadcastReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }

        registerReceiver(packageBroadcastReceiver, intentFilter)

        appScope.launch {
            serviceJob = launch {
                updateEblanApplicationInfosUseCase()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(packageBroadcastReceiver)

        serviceJob.cancel()
    }
}