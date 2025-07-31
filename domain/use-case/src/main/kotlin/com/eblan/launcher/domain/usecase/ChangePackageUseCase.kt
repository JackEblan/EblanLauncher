package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ChangePackageUseCase @Inject constructor(
    private val packageManagerWrapper: PackageManagerWrapper,
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
) {
    suspend operator fun invoke(packageName: String) {
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

        val eblanApplicationInfo = EblanApplicationInfo(
            componentName = componentName,
            packageName = packageName,
            icon = icon,
            label = label,
        )

        eblanApplicationInfoRepository.upsertEblanApplicationInfo(
            eblanApplicationInfo = eblanApplicationInfo,
        )

        val oldEblanAppWidgetProviderInfos =
            eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos.first()

        val newEblanAppWidgetProviderInfos = appWidgetManagerWrapper.getInstalledProviders()
            .filter { appWidgetManagerAppWidgetProviderInfo ->
                appWidgetManagerAppWidgetProviderInfo.packageName == packageName
            }.map { appWidgetManagerAppWidgetProviderInfo ->
                val preview = appWidgetManagerAppWidgetProviderInfo.preview?.let { currentPreview ->
                    fileManager.writeFileBytes(
                        directory = fileManager.widgetsDirectory,
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

        if (oldEblanAppWidgetProviderInfos != newEblanAppWidgetProviderInfos) {
            val eblanAppWidgetProviderInfosToDelete =
                oldEblanAppWidgetProviderInfos - newEblanAppWidgetProviderInfos.toSet()

            eblanAppWidgetProviderInfoRepository.upsertEblanAppWidgetProviderInfos(
                eblanAppWidgetProviderInfos = newEblanAppWidgetProviderInfos,
            )

            eblanAppWidgetProviderInfoRepository.deleteEblanAppWidgetProviderInfos(
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfosToDelete,
            )

            eblanAppWidgetProviderInfosToDelete.forEach { eblanAppWidgetProviderInfo ->
                fileManager.deleteFile(
                    directory = fileManager.widgetsDirectory,
                    name = eblanAppWidgetProviderInfo.className,
                )
            }
        }
    }
}