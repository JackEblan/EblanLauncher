package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ChangeShortcutsUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
) {
    suspend operator fun invoke(
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