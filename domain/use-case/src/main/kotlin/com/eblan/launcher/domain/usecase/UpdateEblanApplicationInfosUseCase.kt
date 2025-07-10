package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.LauncherAppsDomainWrapper
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateEblanApplicationInfosUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val launcherAppsDomainWrapper: LauncherAppsDomainWrapper,
    private val fileManager: FileManager,
) {
    suspend operator fun invoke() {
        val oldEblanApplicationInfos = eblanApplicationInfoRepository.eblanApplicationInfos.first()

        val newEblanApplicationInfos =
            launcherAppsDomainWrapper.getActivityList().map { eblanLauncherActivityInfo ->
                val icon = eblanLauncherActivityInfo.icon?.let { currentIcon ->
                    fileManager.writeFileBytes(
                        directory = fileManager.iconsDirectory,
                        name = eblanLauncherActivityInfo.packageName,
                        byteArray = currentIcon,
                    )
                }

                EblanApplicationInfo(
                    componentName = eblanLauncherActivityInfo.componentName,
                    packageName = eblanLauncherActivityInfo.packageName,
                    icon = icon,
                    label = eblanLauncherActivityInfo.label,
                )
            }

        if (oldEblanApplicationInfos != newEblanApplicationInfos) {
            val eblanApplicationInfosToDelete =
                oldEblanApplicationInfos - newEblanApplicationInfos.toSet()

            eblanApplicationInfoRepository.upsertEblanApplicationInfos(eblanApplicationInfos = newEblanApplicationInfos)

            eblanApplicationInfoRepository.deleteEblanApplicationInfos(eblanApplicationInfos = eblanApplicationInfosToDelete)

            eblanApplicationInfosToDelete.forEach { eblanApplicationInfo ->
                fileManager.deleteFile(
                    directory = fileManager.iconsDirectory,
                    name = eblanApplicationInfo.packageName,
                )
            }
        }
    }
}