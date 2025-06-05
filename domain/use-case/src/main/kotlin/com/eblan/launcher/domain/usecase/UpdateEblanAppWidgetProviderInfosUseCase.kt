package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateEblanAppWidgetProviderInfosUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val fileManager: FileManager,
) {
    suspend operator fun invoke() {
        val oldEblanApplicationInfos = eblanApplicationInfoRepository.eblanApplicationInfos.first()

        val oldEblanAppWidgetProviderInfos =
            eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos.first()

        val newEblanAppWidgetProviderInfos = withContext(Dispatchers.Default) {
            appWidgetManagerWrapper.getInstalledProviders()
                .mapNotNull { appWidgetManagerAppWidgetProviderInfo ->
                    val eblanApplicationInfo =
                        oldEblanApplicationInfos.find { eblanApplicationInfo ->
                            eblanApplicationInfo.packageName == appWidgetManagerAppWidgetProviderInfo.packageName
                        }

                    if (eblanApplicationInfo != null) {
                        val preview =
                            appWidgetManagerAppWidgetProviderInfo.preview?.let { currentPreview ->
                                fileManager.writeFileBytes(
                                    directory = fileManager.previewsDirectory,
                                    name = appWidgetManagerAppWidgetProviderInfo.className,
                                    byteArray = currentPreview,
                                )
                            }

                        EblanAppWidgetProviderInfo(
                            className = appWidgetManagerAppWidgetProviderInfo.className,
                            componentName = appWidgetManagerAppWidgetProviderInfo.componentName,
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
                    } else {
                        null
                    }
                }
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

            eblanAppWidgetProviderInfosToDelete.onEach { eblanAppWidgetProviderInfo ->
                fileManager.deleteFile(
                    directory = fileManager.previewsDirectory,
                    name = eblanAppWidgetProviderInfo.className,
                )
            }
        }
    }
}