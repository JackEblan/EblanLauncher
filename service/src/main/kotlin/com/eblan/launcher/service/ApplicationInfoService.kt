package com.eblan.launcher.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.eblan.launcher.domain.common.qualifier.ApplicationScope
import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.LauncherAppsEvent
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.usecase.UpdateEblanAppWidgetProviderInfosUseCase
import com.eblan.launcher.domain.usecase.UpdateEblanApplicationInfosUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApplicationInfoService : Service() {
    @Inject
    lateinit var updateEblanApplicationInfosUseCase: UpdateEblanApplicationInfosUseCase

    @Inject
    lateinit var updateEblanAppWidgetProviderInfosUseCase: UpdateEblanAppWidgetProviderInfosUseCase

    @Inject
    lateinit var launcherAppsWrapper: LauncherAppsWrapper

    @Inject
    lateinit var packageManagerWrapper: PackageManagerWrapper

    @Inject
    lateinit var eblanApplicationInfoRepository: EblanApplicationInfoRepository

    @Inject
    lateinit var fileManager: FileManager

    @Inject
    lateinit var appWidgetManagerWrapper: AppWidgetManagerWrapper

    @Inject
    lateinit var eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    private lateinit var launcherAppsEventJob: Job

    private lateinit var updateJob: Job

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        appScope.launch {
            launcherAppsEventJob = launch {
                launcherAppsWrapper.launcherAppsEvent.collectLatest { launcherAppEvent ->
                    when (launcherAppEvent) {
                        is LauncherAppsEvent.PackageAdded -> {
                            packageAdded(packageName = launcherAppEvent.packageName)
                        }

                        is LauncherAppsEvent.PackageChanged -> {
                            packageChanged(packageName = launcherAppEvent.packageName)

                        }

                        is LauncherAppsEvent.PackageRemoved -> {
                            packageRemoved(packageName = launcherAppEvent.packageName)
                        }
                    }
                }
            }

            updateJob = launch {
                updateEblanApplicationInfosUseCase()

                updateEblanAppWidgetProviderInfosUseCase()
            }
        }


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        launcherAppsEventJob.cancel()

        updateJob.cancel()
    }

    private suspend fun packageAdded(packageName: String) {
        val componentName = packageManagerWrapper.getComponentName(packageName = packageName)

        val iconByteArray = packageManagerWrapper.getApplicationIcon(packageName = packageName)

        val icon = iconByteArray?.let { currentIconByteArray ->
            fileManager.writeFileBytes(
                directory = fileManager.iconsDirectory,
                name = packageName,
                byteArray = currentIconByteArray,
            )
        }

        val label = packageManagerWrapper.getApplicationLabel(packageName = packageName)

        upsertEblanApplicationInfo(
            componentName = componentName,
            packageName = packageName, icon = icon, label = label,
        )
    }

    private suspend fun packageRemoved(packageName: String) {
        eblanApplicationInfoRepository.deleteEblanApplicationInfoByPackageName(
            packageName = packageName,
        )

        fileManager.deleteFile(
            directory = fileManager.iconsDirectory,
            name = packageName,
        )

        appWidgetManagerWrapper.getInstalledProviders()
            .filter { appWidgetManagerAppWidgetProviderInfo ->
                appWidgetManagerAppWidgetProviderInfo.packageName == packageName
            }.onEach { appWidgetManagerAppWidgetProviderInfo ->
                fileManager.deleteFile(
                    directory = fileManager.previewsDirectory,
                    name = appWidgetManagerAppWidgetProviderInfo.className,
                )
            }
    }

    private suspend fun packageChanged(packageName: String) {
        val componentName = packageManagerWrapper.getComponentName(packageName = packageName)

        val iconByteArray = packageManagerWrapper.getApplicationIcon(packageName = packageName)

        val icon = iconByteArray?.let { currentIconByteArray ->
            fileManager.writeFileBytes(
                directory = fileManager.iconsDirectory,
                name = packageName,
                byteArray = currentIconByteArray,
            )
        }

        val label = packageManagerWrapper.getApplicationLabel(packageName = packageName)

        upsertEblanApplicationInfo(
            componentName = componentName,
            packageName = packageName,
            icon = icon,
            label = label,
        )
    }

    private suspend fun upsertEblanApplicationInfo(
        componentName: String?,
        packageName: String,
        icon: String?,
        label: String?,
    ) {
        val eblanApplicationInfo = EblanApplicationInfo(
            componentName = componentName,
            packageName = packageName,
            icon = icon,
            label = label,
        )

        eblanApplicationInfoRepository.upsertEblanApplicationInfo(
            eblanApplicationInfo = eblanApplicationInfo,
        )

        val eblanAppWidgetProviderInfos = appWidgetManagerWrapper.getInstalledProviders()
            .filter { appWidgetManagerAppWidgetProviderInfo ->
                appWidgetManagerAppWidgetProviderInfo.packageName == packageName
            }.map { appWidgetManagerAppWidgetProviderInfo ->
                val preview = appWidgetManagerAppWidgetProviderInfo.preview?.let { currentPreview ->
                    fileManager.writeFileBytes(
                        directory = fileManager.previewsDirectory,
                        name = appWidgetManagerAppWidgetProviderInfo.className,
                        byteArray = currentPreview,
                    )
                }

                EblanAppWidgetProviderInfo(
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
            }

        eblanAppWidgetProviderInfoRepository.upsertEblanAppWidgetProviderInfos(
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
        )
    }
}