package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateEblanApplicationInfosUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val fileManager: FileManager,
    private val gridRepository: GridRepository,
) {
    suspend operator fun invoke() {
        val oldEblanApplicationInfos = eblanApplicationInfoRepository.eblanApplicationInfos.first()

        val newEblanApplicationInfos =
            launcherAppsWrapper.getActivityList().map { eblanLauncherActivityInfo ->
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

            eblanApplicationInfosToDelete.onEach { eblanApplicationInfo ->
                fileManager.deleteFile(
                    directory = fileManager.iconsDirectory,
                    name = eblanApplicationInfo.packageName,
                )
            }
        }
    }
}