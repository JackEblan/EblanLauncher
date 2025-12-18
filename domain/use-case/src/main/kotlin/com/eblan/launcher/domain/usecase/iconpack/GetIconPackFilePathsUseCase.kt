package com.eblan.launcher.domain.usecase.iconpack

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class GetIconPackFilePathsUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val fileManager: FileManager,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<Map<String, String>> {
        return combine(
            userDataRepository.userData,
            eblanApplicationInfoRepository.eblanApplicationInfos,
        ) { userData, eblaApplicationInfos ->
            val iconPacksDirectory = fileManager.getFilesDirectory(
                FileManager.Companion.ICON_PACKS_DIR,
            )

            val iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName

            val iconPackDirectory = File(
                iconPacksDirectory,
                iconPackInfoPackageName,
            )

            if (iconPackInfoPackageName.isNotEmpty()) {
                eblaApplicationInfos.mapNotNull { eblanApplicationInfo ->
                    val iconPackFile = File(
                        iconPackDirectory,
                        eblanApplicationInfo.componentName.replace(
                            "/",
                            "-",
                        ),
                    )

                    if (iconPackFile.exists()) {
                        eblanApplicationInfo.componentName to iconPackFile.absolutePath
                    } else {
                        null
                    }
                }.toMap()
            } else {
                emptyMap()
            }
        }.flowOn(ioDispatcher)
    }
}