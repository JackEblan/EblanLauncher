package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateEblanShortcutInfosUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val fileManager: FileManager,
) {
    suspend operator fun invoke() {
        if (!launcherAppsWrapper.hasShortcutHostPermission) {
            return
        }

        withContext(Dispatchers.Default) {
            val oldEblanApplicationInfos =
                eblanApplicationInfoRepository.eblanApplicationInfos.first()

            val oldEblanShortcutInfos = eblanShortcutInfoRepository.eblanShortcutInfos.first()

            val newEblanShortcutInfos =
                launcherAppsWrapper.getShortcuts()?.mapNotNull { launcherAppsShortcutInfo ->
                    val eblanApplicationInfo =
                        oldEblanApplicationInfos.find { eblanApplicationInfo ->
                            eblanApplicationInfo.packageName == launcherAppsShortcutInfo.packageName
                        }

                    if (eblanApplicationInfo != null) {
                        val icon = fileManager.writeFileBytes(
                            directory = fileManager.shortcutsDirectory,
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

            if (newEblanShortcutInfos != null && oldEblanShortcutInfos != newEblanShortcutInfos) {
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
                        directory = fileManager.shortcutsDirectory,
                        name = eblanShortcutInfo.id,
                    )
                }
            }
        }
    }
}