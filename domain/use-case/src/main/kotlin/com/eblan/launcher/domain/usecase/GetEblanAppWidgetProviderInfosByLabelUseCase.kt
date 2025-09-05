package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetEblanAppWidgetProviderInfosByLabelUseCase @Inject constructor(
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(label: String): Flow<Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>> {
        return eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos.map { eblanAppWidgetProviderInfos ->
            eblanAppWidgetProviderInfos
                .filter { eblanAppWidgetProviderInfo ->
                    val eblanApplicationInfoLabel =
                        eblanAppWidgetProviderInfo.eblanApplicationInfo.label

                    eblanApplicationInfoLabel != null && eblanApplicationInfoLabel.contains(
                        other = label,
                        ignoreCase = true,
                    )
                }
                .sortedBy { eblanAppWidgetProviderInfo ->
                    eblanAppWidgetProviderInfo.eblanApplicationInfo.label
                }.groupBy { eblanAppWidgetProviderInfo ->
                    eblanAppWidgetProviderInfo.eblanApplicationInfo
                }
        }.flowOn(defaultDispatcher)
    }
}