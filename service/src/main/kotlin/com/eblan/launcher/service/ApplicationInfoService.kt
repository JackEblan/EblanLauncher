package com.eblan.launcher.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.eblan.launcher.domain.common.qualifier.ApplicationScope
import com.eblan.launcher.domain.framework.AppWidgetManagerDomainWrapper
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.LauncherAppsEvent
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.usecase.UpdateEblanAppWidgetProviderInfosUseCase
import com.eblan.launcher.domain.usecase.UpdateEblanApplicationInfosUseCase
import com.eblan.launcher.domain.usecase.UpdateEblanShortcutInfosUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApplicationInfoService : Service() {
    @Inject
    lateinit var updateEblanApplicationInfosUseCase: UpdateEblanApplicationInfosUseCase

    @Inject
    lateinit var updateEblanAppWidgetProviderInfosUseCase: UpdateEblanAppWidgetProviderInfosUseCase

    @Inject
    lateinit var updateEblanShortcutInfosUseCase: UpdateEblanShortcutInfosUseCase

    @Inject
    lateinit var launcherAppsWrapper: LauncherAppsWrapper

    @Inject
    lateinit var packageManagerWrapper: PackageManagerWrapper

    @Inject
    lateinit var eblanApplicationInfoRepository: EblanApplicationInfoRepository

    @Inject
    lateinit var eblanShortcutInfoRepository: EblanShortcutInfoRepository

    @Inject
    lateinit var fileManager: FileManager

    @Inject
    lateinit var appWidgetManagerDomainWrapper: AppWidgetManagerDomainWrapper

    @Inject
    lateinit var eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    private var launcherAppsEventJob: Job? = null

    private var updateJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        appScope.launch {
            launcherAppsEventJob = launch {
                launcherAppsWrapper.launcherAppsEvent.collectLatest { launcherAppsEvent ->
                    when (launcherAppsEvent) {
                        is LauncherAppsEvent.PackageAdded -> {
                            packageAdded(packageName = launcherAppsEvent.packageName)
                        }

                        is LauncherAppsEvent.PackageChanged -> {
                            packageChanged(packageName = launcherAppsEvent.packageName)
                        }

                        is LauncherAppsEvent.PackageRemoved -> {
                            packageRemoved(packageName = launcherAppsEvent.packageName)
                        }

                        is LauncherAppsEvent.ShortcutsChanged -> {
                            upsertEblanShortcutInfos(
                                packageName = launcherAppsEvent.packageName,
                                launcherAppsShortcutInfos = launcherAppsEvent.launcherAppsShortcutInfos,
                            )
                        }
                    }
                }
            }

            updateJob = launch {
                updateEblanApplicationInfosUseCase()

                updateEblanAppWidgetProviderInfosUseCase()

                updateEblanShortcutInfosUseCase()
            }
        }


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        launcherAppsEventJob?.cancel()

        updateJob?.cancel()
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

        appWidgetManagerDomainWrapper.getInstalledProviders()
            .filter { appWidgetManagerAppWidgetProviderInfo ->
                appWidgetManagerAppWidgetProviderInfo.packageName == packageName
            }.forEach { appWidgetManagerAppWidgetProviderInfo ->
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

        val eblanAppWidgetProviderInfos = appWidgetManagerDomainWrapper.getInstalledProviders()
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
                    configure = appWidgetManagerAppWidgetProviderInfo.configure,
                    packageName = appWidgetManagerAppWidgetProviderInfo.packageName,
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

    private suspend fun upsertEblanShortcutInfos(
        packageName: String,
        launcherAppsShortcutInfos: List<LauncherAppsShortcutInfo>,
    ) {
        val eblanApplicationInfos =
            eblanApplicationInfoRepository.eblanApplicationInfos.first()

        val oldEblanShortcutInfos = eblanShortcutInfoRepository.eblanShortcutInfos.first()

        val newEblanShortcutInfos =
            launcherAppsShortcutInfos.mapNotNull { launcherAppsShortcutInfo ->
                val eblanApplicationInfo =
                    eblanApplicationInfos.find { eblanApplicationInfo ->
                        eblanApplicationInfo.packageName == packageName
                    }

                if (eblanApplicationInfo != null) {
                    val icon = fileManager.writeFileBytes(
                        directory = fileManager.previewsDirectory,
                        name = launcherAppsShortcutInfo.id,
                        byteArray = launcherAppsShortcutInfo.icon,
                    )

                    EblanShortcutInfo(
                        id = launcherAppsShortcutInfo.id,
                        packageName = launcherAppsShortcutInfo.packageName,
                        shortLabel = launcherAppsShortcutInfo.shortLabel,
                        longLabel = launcherAppsShortcutInfo.longLabel,
                        eblanApplicationInfo = eblanApplicationInfo,
                        icon = icon,
                    )
                } else {
                    null
                }
            }

        if (oldEblanShortcutInfos != newEblanShortcutInfos) {
            val eblanShortcutInfosToDelete =
                oldEblanShortcutInfos - newEblanShortcutInfos.toSet()

            eblanShortcutInfoRepository.upsertEblanShortcutInfos(
                eblanShortcutInfos = newEblanShortcutInfos,
            )

            eblanShortcutInfoRepository.deleteEblanShortcutInfos(
                eblanShortcutInfos = eblanShortcutInfosToDelete,
            )

            eblanShortcutInfosToDelete.forEach { eblanShortcutInfo ->
                fileManager.deleteFile(
                    directory = fileManager.previewsDirectory,
                    name = eblanShortcutInfo.id,
                )
            }
        }
    }
}