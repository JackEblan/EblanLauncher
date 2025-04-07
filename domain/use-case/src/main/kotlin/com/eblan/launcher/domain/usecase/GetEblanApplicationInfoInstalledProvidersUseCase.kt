package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetEblanApplicationInfoInstalledProvidersUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
) {
    suspend operator fun invoke(installedProviderPackageNames: List<String>): List<EblanApplicationInfo> {
        return withContext(Dispatchers.Default) {
            eblanApplicationInfoRepository.eblanApplicationInfos.first()
                .filter { eblanApplicationInfo ->
                    eblanApplicationInfo.packageName in installedProviderPackageNames
                }
        }
    }
}