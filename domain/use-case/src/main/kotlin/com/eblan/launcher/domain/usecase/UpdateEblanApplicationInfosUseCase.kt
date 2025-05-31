package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateEblanApplicationInfosUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val fileManager: FileManager,
    private val gridRepository: GridRepository,
) {
    suspend operator fun invoke() {
        val oldEblanApplicationInfos = eblanApplicationInfoRepository.eblanApplicationInfos.first()

        val newEblanApplicationInfos =
            packageManagerWrapper.queryIntentActivities().map { packageManagerApplicationInfo ->
                val icon = packageManagerApplicationInfo.icon?.let { currentIcon ->
                    fileManager.writeFileBytes(
                        directory = fileManager.iconsDirectory,
                        name = packageManagerApplicationInfo.packageName,
                        byteArray = currentIcon,
                    )
                }

                EblanApplicationInfo(
                    packageName = packageManagerApplicationInfo.packageName,
                    icon = icon,
                    label = packageManagerApplicationInfo.label,
                )
            }

        if (oldEblanApplicationInfos != newEblanApplicationInfos) {
            val eblanApplicationInfosToDelete =
                oldEblanApplicationInfos - newEblanApplicationInfos.toSet()

            eblanApplicationInfoRepository.upsertEblanApplicationInfos(eblanApplicationInfos = newEblanApplicationInfos)

            eblanApplicationInfoRepository.deleteEblanApplicationInfos(eblanApplicationInfos = eblanApplicationInfosToDelete)

            val dataIds = eblanApplicationInfosToDelete.onEach { eblanApplicationInfo ->
                fileManager.deleteFile(
                    directory = fileManager.iconsDirectory,
                    name = eblanApplicationInfo.packageName,
                )
            }.map { eblanApplicationInfo ->
                eblanApplicationInfo.packageName
            }

            gridRepository.deleteGridItemByDataIds(dataIds = dataIds)
        }
    }
}