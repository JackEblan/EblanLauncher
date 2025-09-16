package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UpdateIconPackInfoUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val iconPackManager: IconPackManager,
    private val userDataRepository: UserDataRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(packageName: String) {
        withContext(defaultDispatcher) {
            val iconPackInfoPackageName =
                userDataRepository.userData.first().generalSettings.iconPackInfoPackageName

            val iconPackDirectory = File(
                fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
                iconPackInfoPackageName
            ).apply { if (!exists()) mkdirs() }

            val appFilter =
                iconPackManager.parseAppFilter(iconPackInfoPackageName = iconPackInfoPackageName)

            val entry = appFilter.entries.find { (component, _) ->
                component.contains(packageName)
            } ?: return@withContext

            val byteArray = iconPackManager.loadByteArrayFromIconPack(
                packageName = iconPackInfoPackageName,
                drawableName = entry.value,
            ) ?: return@withContext

            fileManager.getAndUpdateFilePath(
                directory = iconPackDirectory,
                name = packageName,
                byteArray = byteArray,
            )
        }
    }
}