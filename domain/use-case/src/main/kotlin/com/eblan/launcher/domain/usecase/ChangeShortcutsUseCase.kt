package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChangeShortcutsUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        packageName: String,
        launcherAppsShortcutInfos: List<LauncherAppsShortcutInfo>,
    ) {
        withContext(defaultDispatcher) {
            val eblanApplicationInfos =
                eblanApplicationInfoRepository.eblanApplicationInfos.first()

            val oldEblanShortcutInfos =
                eblanShortcutInfoRepository.eblanShortcutInfos.first().filter { eblanShortcutInfo ->
                    eblanShortcutInfo.packageName == packageName
                }

            val newEblanShortcutInfos =
                launcherAppsShortcutInfos.mapNotNull { launcherAppsShortcutInfo ->
                    val eblanApplicationInfo =
                        eblanApplicationInfos.find { eblanApplicationInfo ->
                            eblanApplicationInfo.packageName == packageName
                        }

                    if (eblanApplicationInfo != null) {
                        val icon = fileManager.writeFileBytes(
                            directory = fileManager.getDirectory(FileManager.SHORTCUTS_DIR),
                            name = launcherAppsShortcutInfo.shortcutId,
                            byteArray = launcherAppsShortcutInfo.icon,
                        )

                        EblanShortcutInfo(
                            shortcutId = launcherAppsShortcutInfo.shortcutId,
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
                        directory = fileManager.getDirectory(FileManager.SHORTCUTS_DIR),
                        name = eblanShortcutInfo.shortcutId,
                    )
                }
            }
        }
    }
}