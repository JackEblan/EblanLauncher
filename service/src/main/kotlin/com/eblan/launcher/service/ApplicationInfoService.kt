package com.eblan.launcher.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.IBinder
import androidx.core.graphics.drawable.toBitmap
import com.eblan.launcher.broadcastreceiver.PackageBroadcastReceiver
import com.eblan.launcher.domain.common.qualifier.ApplicationScope
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.framework.filemanager.FileManager
import com.eblan.launcher.framework.packagemanager.PackageManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
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
                packageManagerWrapper.queryIntentActivities().map { applicationInfo ->
                    val newIcon = withContext(Dispatchers.IO) {
                        val stream = ByteArrayOutputStream()

                        val drawable = packageManagerWrapper.getApplicationIcon(
                            packageName = applicationInfo.packageName,
                        )

                        drawable?.toBitmap()?.compress(Bitmap.CompressFormat.PNG, 100, stream)

                        stream.toByteArray()
                    }

                    val icon = fileManager.writeIconBytes(
                        name = applicationInfo.packageName,
                        newIcon = newIcon,
                    )

                    val label = applicationInfo.loadLabel(packageManager).toString()

                    EblanApplicationInfo(
                        packageName = applicationInfo.packageName,
                        icon = icon,
                        label = label,
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