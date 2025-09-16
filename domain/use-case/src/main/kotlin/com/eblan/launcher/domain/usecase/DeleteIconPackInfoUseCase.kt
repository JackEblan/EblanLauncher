package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DeleteIconPackInfoUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val userDataRepository: UserDataRepository,
    private val eblanIconPackInfoRepository: EblanIconPackInfoRepository,
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(iconPackInfoPackageName: String) {
        withContext(ioDispatcher) {
            val eblanIconPackInfo =
                eblanIconPackInfoRepository.getEblanIconPackInfo(packageName = iconPackInfoPackageName)

            if (eblanIconPackInfo != null) {
                eblanIconPackInfoRepository.deleteEblanIconPackInfo(eblanIconPackInfo = eblanIconPackInfo)

                val iconPackInfoPackageName =
                    userDataRepository.userData.first().generalSettings.iconPackInfoPackageName

                val iconPacksDirectory = File(
                    fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
                    iconPackInfoPackageName,
                )

                if (iconPacksDirectory.isDirectory && iconPacksDirectory.exists()) {
                    iconPacksDirectory.deleteRecursively()
                }
            }
        }
    }
}