package com.eblan.launcher.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.eblan.launcher.broadcastreceiver.PackageBroadcastReceiver
import com.eblan.launcher.domain.common.qualifier.ApplicationScope
import com.eblan.launcher.domain.usecase.WriteIconUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApplicationInfoService : Service() {
    @Inject
    lateinit var writeIconUseCase: WriteIconUseCase

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
        }

        registerReceiver(packageBroadcastReceiver, intentFilter)

        serviceJob = appScope.launch {
            writeIconUseCase()
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