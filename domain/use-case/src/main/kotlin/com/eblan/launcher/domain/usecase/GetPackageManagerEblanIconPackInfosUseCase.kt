package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanIconPackInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import javax.inject.Inject

class GetPackageManagerEblanIconPackInfosUseCase @Inject constructor(
    private val packageManagerWrapper: PackageManagerWrapper,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
) {
    suspend operator fun invoke(): List<EblanIconPackInfo> {
        return packageManagerWrapper.getIconPackInfoByPackageNames().mapNotNull { packageName ->
            eblanApplicationInfoRepository.getEblanApplicationInfo(packageName = packageName)
                ?.let { eblanApplicationInfo ->
                    EblanIconPackInfo(
                        packageName = eblanApplicationInfo.packageName,
                        icon = eblanApplicationInfo.icon,
                        label = eblanApplicationInfo.label
                    )
                }
        }
    }
}