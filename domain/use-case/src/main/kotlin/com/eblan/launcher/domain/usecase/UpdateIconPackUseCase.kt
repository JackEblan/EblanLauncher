package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UpdateIconPackUseCase @Inject constructor(
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val iconPackManager: IconPackManager,
    private val fileManager: FileManager,
    private val userDataRepository: UserDataRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(iconPackPackageName: String) {
        withContext(defaultDispatcher) {
            launcherAppsWrapper.getActivityList().forEach { eblanLauncherActivityInfo ->
                val appFilter =
                    iconPackManager.parseAppFilter(iconPackPackageName = iconPackPackageName)

                val entry = appFilter.entries.find { (component, _) ->
                    component.contains(eblanLauncherActivityInfo.packageName)
                } ?: return@forEach

                val byteArray = iconPackManager.loadByteArrayFromIconPack(
                    packageName = iconPackPackageName,
                    drawableName = entry.value,
                ) ?: return@forEach

                val iconPackDirectory = File(
                    fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
                    iconPackPackageName
                ).apply { if (!exists()) mkdirs() }

                fileManager.getAndUpdateFilePath(
                    directory = iconPackDirectory,
                    name = eblanLauncherActivityInfo.packageName,
                    byteArray = byteArray
                )
            }
        }

        userDataRepository.updateIconPackPackageName(iconPackPackageName = iconPackPackageName)
    }
}