package com.eblan.launcher.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eblan.launcher.domain.common.qualifier.ApplicationScope
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageBroadcastReceiver : BroadcastReceiver() {
    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    @Inject
    lateinit var eblanApplicationInfoRepository: EblanApplicationInfoRepository

    @Inject
    lateinit var packageManagerWrapper: PackageManagerWrapper

    @Inject
    lateinit var fileManager: FileManager

    override fun onReceive(context: Context?, intent: Intent?) {
        val packageName = intent?.data?.schemeSpecificPart ?: return

        appScope.launch {
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)

                    val iconByteArray =
                        packageManagerWrapper.getApplicationIcon(packageName = packageName)

                    val icon = fileManager.writeIconBytes(
                        iconsDirectory = fileManager.iconsDirectory,
                        name = packageName,
                        icon = iconByteArray,
                    )

                    val label = packageManagerWrapper.getApplicationLabel(packageName = packageName)

                    if (!isReplacing && label != null) {
                        val eblanApplicationInfo = EblanApplicationInfo(
                            packageName = packageName,
                            icon = icon,
                            label = label,
                        )

                        eblanApplicationInfoRepository.upsertEblanApplicationInfo(
                            eblanApplicationInfo = eblanApplicationInfo,
                        )
                    }
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)

                    if (!isReplacing) {
                        eblanApplicationInfoRepository.deleteEblanApplicationInfoByPackageName(packageName = packageName)
                    }
                }

                Intent.ACTION_PACKAGE_CHANGED -> {
                    // App components (like enabled activities) changed
                }

                Intent.ACTION_PACKAGE_REPLACED -> {
                    // App updated (same package)
                }
            }
        }
    }
}