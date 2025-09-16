package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.EblanIconPackInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UpdateIconPackUseCase @Inject constructor(
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val iconPackManager: IconPackManager,
    private val fileManager: FileManager,
    private val eblanIconPackInfoRepository: EblanIconPackInfoRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(iconPackPackageName: String) {
        withContext(defaultDispatcher) {
            val eblanApplicationInfo =
                eblanApplicationInfoRepository.getEblanApplicationInfo(packageName = iconPackPackageName)

            if (eblanApplicationInfo != null) {
                val appFilter =
                    iconPackManager.parseAppFilter(iconPackPackageName = iconPackPackageName)

                val iconPackDirectory = File(
                    fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
                    iconPackPackageName
                ).apply { if (!exists()) mkdirs() }

                val installedPackageNames = launcherAppsWrapper.getActivityList()
                    .mapNotNull { eblanLauncherActivityInfo ->
                        val entry = appFilter.entries.find { (component, _) ->
                            component.contains(eblanLauncherActivityInfo.packageName)
                        } ?: return@mapNotNull null

                        val byteArray = iconPackManager.loadByteArrayFromIconPack(
                            packageName = iconPackPackageName,
                            drawableName = entry.value,
                        ) ?: return@mapNotNull null

                        fileManager.getAndUpdateFilePath(
                            directory = iconPackDirectory,
                            name = eblanLauncherActivityInfo.packageName,
                            byteArray = byteArray
                        )

                        eblanLauncherActivityInfo.packageName
                    }
                    .toSet()

                eblanIconPackInfoRepository.upsertEblanIconPackInfo(
                    eblanIconPackInfo = EblanIconPackInfo(
                        packageName = eblanApplicationInfo.packageName,
                        icon = eblanApplicationInfo.icon,
                        label = eblanApplicationInfo.label
                    )
                )

                iconPackDirectory.listFiles()
                    ?.filter { it.isFile && it.name !in installedPackageNames }
                    ?.forEach { it.delete() }
            }
        }
    }
}