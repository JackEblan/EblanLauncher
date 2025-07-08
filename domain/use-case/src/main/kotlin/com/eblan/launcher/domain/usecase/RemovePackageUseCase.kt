package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.AppWidgetManagerDomainWrapper
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import javax.inject.Inject

class RemovePackageUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val appWidgetManagerDomainWrapper: AppWidgetManagerDomainWrapper,
) {
    suspend operator fun invoke(packageName: String) {
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
                    directory = fileManager.widgetsDirectory,
                    name = appWidgetManagerAppWidgetProviderInfo.className,
                )
            }
    }
}