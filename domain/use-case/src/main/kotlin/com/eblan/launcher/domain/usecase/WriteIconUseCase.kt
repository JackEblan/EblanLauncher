package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import javax.inject.Inject

class WriteIconUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val fileManager: FileManager,
) {
    suspend operator fun invoke() {
        val eblanApplicationInfos =
            packageManagerWrapper.queryIntentActivities().map { packageManagerApplicationInfo ->
                val icon = fileManager.writeIconBytes(
                    name = packageManagerApplicationInfo.packageName,
                    icon = packageManagerApplicationInfo.icon,
                )

                EblanApplicationInfo(
                    packageName = packageManagerApplicationInfo.packageName,
                    icon = icon,
                    label = packageManagerApplicationInfo.label,
                )
            }

        eblanApplicationInfoRepository.upsertEblanApplicationInfos(eblanApplicationInfos = eblanApplicationInfos)
    }
}