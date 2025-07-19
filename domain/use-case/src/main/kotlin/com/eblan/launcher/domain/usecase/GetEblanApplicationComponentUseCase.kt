package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.EblanApplicationComponent
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetEblanApplicationComponentUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
) {
    operator fun invoke(): Flow<EblanApplicationComponent> {
        return combine(
            eblanApplicationInfoRepository.eblanApplicationInfos,
            eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos,
            eblanShortcutInfoRepository.eblanShortcutInfos,
        ) { eblanApplicationInfos, eblanAppWidgetProviderInfos, eblanShortcutInfos ->
            val sortedEblanApplicationInfos =
                eblanApplicationInfos.sortedBy { eblanApplicationInfo ->
                    eblanApplicationInfo.label
                }

            val groupedEblanAppWidgetProviderInfos =
                eblanAppWidgetProviderInfos.sortedBy { eblanAppWidgetProviderInfo ->
                    eblanAppWidgetProviderInfo.eblanApplicationInfo.label
                }.groupBy { eblanAppWidgetProviderInfo ->
                    eblanAppWidgetProviderInfo.eblanApplicationInfo
                }

            val groupedEblanShortcutInfos =
                eblanShortcutInfos.sortedBy { eblanShortcutInfo ->
                    eblanShortcutInfo.eblanApplicationInfo.label
                }.groupBy { eblanShortcutInfo ->
                    eblanShortcutInfo.eblanApplicationInfo
                }

            EblanApplicationComponent(
                eblanApplicationInfos = sortedEblanApplicationInfos,
                eblanAppWidgetProviderInfos = groupedEblanAppWidgetProviderInfos,
                eblanShortcutInfos = groupedEblanShortcutInfos,
            )
        }.flowOn(Dispatchers.Default)
    }
}