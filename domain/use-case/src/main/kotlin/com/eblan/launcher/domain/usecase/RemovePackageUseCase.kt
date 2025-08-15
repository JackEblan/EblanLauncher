package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemovePackageUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(packageName: String) {
        withContext(defaultDispatcher) {
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
                }.forEach { appWidgetManagerAppWidgetProviderInfo ->
                    fileManager.deleteFile(
                        directory = fileManager.widgetsDirectory,
                        name = appWidgetManagerAppWidgetProviderInfo.className,
                    )
                }
        }
    }
}