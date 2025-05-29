package com.eblan.launcher.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eblan.launcher.domain.common.qualifier.ApplicationScope
import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
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

    @Inject
    lateinit var appWidgetManagerWrapper: AppWidgetManagerWrapper

    @Inject
    lateinit var eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        val packageName = intent?.data?.schemeSpecificPart ?: return

        appScope.launch {
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)

                    actionPackageAdded(isReplacing = isReplacing, packageName = packageName)
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)

                    actionPackageRemoved(isReplacing = isReplacing, packageName = packageName)
                }

                Intent.ACTION_PACKAGE_CHANGED -> {
                    // App components (like enabled activities) changed
                }

                Intent.ACTION_PACKAGE_REPLACED -> {
                    actionPackageReplaced(packageName = packageName)
                }
            }
        }
    }

    private suspend fun actionPackageRemoved(isReplacing: Boolean, packageName: String) {
        if (!isReplacing) {
            eblanApplicationInfoRepository.deleteEblanApplicationInfoByPackageName(
                packageName = packageName,
            )

            appWidgetManagerWrapper.getInstalledProviders()
                .filter { appWidgetManagerAppWidgetProviderInfo ->
                    appWidgetManagerAppWidgetProviderInfo.packageName == packageName
                }.onEach { appWidgetManagerAppWidgetProviderInfo ->
                    eblanAppWidgetProviderInfoRepository.deleteEblanAppWidgetProviderInfoByClassName(
                        className = appWidgetManagerAppWidgetProviderInfo.className,
                    )
                }
        }
    }

    private suspend fun actionPackageAdded(isReplacing: Boolean, packageName: String) {
        val iconByteArray =
            packageManagerWrapper.getApplicationIcon(packageName = packageName)

        val icon = iconByteArray?.let { currentIconByteArray ->
            fileManager.writeFileBytes(
                directory = fileManager.iconsDirectory,
                name = packageName,
                byteArray = currentIconByteArray,
            )
        }

        if (!isReplacing) {
            val label = packageManagerWrapper.getApplicationLabel(packageName = packageName)

            upsertEblanApplicationInfo(packageName = packageName, icon = icon, label = label)
        }
    }

    private suspend fun actionPackageReplaced(packageName: String) {
        val iconByteArray =
            packageManagerWrapper.getApplicationIcon(packageName = packageName)

        val icon = iconByteArray?.let { currentIconByteArray ->
            fileManager.writeFileBytes(
                directory = fileManager.iconsDirectory,
                name = packageName,
                byteArray = currentIconByteArray,
            )
        }

        val label = packageManagerWrapper.getApplicationLabel(packageName = packageName)

        upsertEblanApplicationInfo(packageName = packageName, icon = icon, label = label)
    }

    private suspend fun upsertEblanApplicationInfo(
        packageName: String,
        icon: String?,
        label: String?,
    ) {
        val eblanApplicationInfo = EblanApplicationInfo(
            packageName = packageName,
            icon = icon,
            label = label,
        )

        eblanApplicationInfoRepository.upsertEblanApplicationInfo(
            eblanApplicationInfo = eblanApplicationInfo,
        )

        appWidgetManagerWrapper.getInstalledProviders()
            .filter { appWidgetManagerAppWidgetProviderInfo ->
                appWidgetManagerAppWidgetProviderInfo.packageName == packageName
            }.onEach { appWidgetManagerAppWidgetProviderInfo ->
                val preview =
                    appWidgetManagerAppWidgetProviderInfo.preview?.let { currentPreview ->
                        fileManager.writeFileBytes(
                            directory = fileManager.previewsDirectory,
                            name = appWidgetManagerAppWidgetProviderInfo.className,
                            byteArray = currentPreview,
                        )
                    }

                val eblanAppWidgetProviderInfo = EblanAppWidgetProviderInfo(
                    className = appWidgetManagerAppWidgetProviderInfo.className,
                    componentName = appWidgetManagerAppWidgetProviderInfo.componentName,
                    targetCellWidth = appWidgetManagerAppWidgetProviderInfo.targetCellWidth,
                    targetCellHeight = appWidgetManagerAppWidgetProviderInfo.targetCellHeight,
                    minWidth = appWidgetManagerAppWidgetProviderInfo.minWidth,
                    minHeight = appWidgetManagerAppWidgetProviderInfo.minHeight,
                    resizeMode = appWidgetManagerAppWidgetProviderInfo.resizeMode,
                    minResizeWidth = appWidgetManagerAppWidgetProviderInfo.minResizeWidth,
                    minResizeHeight = appWidgetManagerAppWidgetProviderInfo.minResizeHeight,
                    maxResizeWidth = appWidgetManagerAppWidgetProviderInfo.maxResizeWidth,
                    maxResizeHeight = appWidgetManagerAppWidgetProviderInfo.maxResizeHeight,
                    preview = preview,
                    eblanApplicationInfo = eblanApplicationInfo,
                )

                eblanAppWidgetProviderInfoRepository.upsertEblanAppWidgetProviderInfo(
                    eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                )
            }
    }
}