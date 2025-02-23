package com.eblan.launcher.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eblan.launcher.domain.common.qualifier.ApplicationScope
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.framework.filemanager.FileManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var eblanApplicationInfoRepository: EblanApplicationInfoRepository

    @Inject
    lateinit var packageManagerWrapper: PackageManagerWrapper

    @Inject
    lateinit var fileManager: FileManager

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context?, intent: Intent?) {
        appScope.launch {
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
    }
}