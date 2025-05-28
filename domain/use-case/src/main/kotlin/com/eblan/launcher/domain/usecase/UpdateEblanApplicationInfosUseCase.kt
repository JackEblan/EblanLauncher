package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateEblanApplicationInfosUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val fileManager: FileManager,
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

        val newEblanApplicationInfosByPackageName = withContext(Dispatchers.Default) {
            newEblanApplicationInfos.map { eblanApplicationInfo ->
                eblanApplicationInfo.packageName
            }
        }

        if (oldEblanApplicationInfos != newEblanApplicationInfos) {
            eblanApplicationInfoRepository.upsertEblanApplicationInfos(eblanApplicationInfos = newEblanApplicationInfos)

            eblanApplicationInfoRepository.deleteEblanApplicationInfosNotInPackageNames(packageNames = newEblanApplicationInfosByPackageName)
        }
    }
}