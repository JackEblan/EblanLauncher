package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetEblanApplicationInfosByLabelUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(label: String): Flow<List<EblanApplicationInfo>> {
        return eblanApplicationInfoRepository.eblanApplicationInfos.map { eblanApplicationInfos ->
            eblanApplicationInfos
                .filter { eblanApplicationInfo ->
                    val eblanApplicationInfoLabel = eblanApplicationInfo.label

                    eblanApplicationInfoLabel != null && eblanApplicationInfoLabel.contains(
                        other = label,
                        ignoreCase = true,
                    )
                }
                .sortedBy { eblanApplicationInfo -> eblanApplicationInfo.label }
        }.flowOn(defaultDispatcher)
    }
}