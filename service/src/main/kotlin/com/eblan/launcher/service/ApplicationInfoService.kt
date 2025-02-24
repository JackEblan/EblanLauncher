package com.eblan.launcher.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.eblan.launcher.broadcastreceiver.PackageBroadcastReceiver
import com.eblan.launcher.domain.common.qualifier.ApplicationScope
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.framework.filemanager.FileManager
import com.eblan.launcher.framework.packagemanager.PackageManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApplicationInfoService : Service() {
    @Inject
    lateinit var eblanApplicationInfoRepository: EblanApplicationInfoRepository

    @Inject
    lateinit var packageManagerWrapper: PackageManagerWrapper

    @Inject
    lateinit var fileManager: FileManager

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
            val eblanApplicationInfos =
                packageManagerWrapper.queryIntentActivities().map { packageManagerApplicationInfo ->
                    val icon = fileManager.writeIconBytes(
                        name = packageManagerApplicationInfo.packageName,
                        newIcon = packageManagerWrapper.getApplicationIcon(
                            packageName = packageManagerApplicationInfo.packageName,
                        ),
                    )

                    EblanApplicationInfo(
                        packageName = packageManagerApplicationInfo.packageName,
                        icon = icon,
                        label = packageManagerApplicationInfo.label,
                    )
                }

            eblanApplicationInfoRepository.upsertEblanApplicationInfos(eblanApplicationInfos = eblanApplicationInfos)
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