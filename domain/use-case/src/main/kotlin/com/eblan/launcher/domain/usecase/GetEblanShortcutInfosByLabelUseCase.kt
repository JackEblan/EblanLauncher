package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetEblanShortcutInfosByLabelUseCase @Inject constructor(
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(label: String): Flow<Map<EblanApplicationInfo, List<EblanShortcutInfo>>> {
        return eblanShortcutInfoRepository.eblanShortcutInfos.map { eblanShortcutInfos ->
            eblanShortcutInfos
                .filter { eblanShortcutInfo ->
                    val eblanApplicationInfoLabel =
                        eblanShortcutInfo.eblanApplicationInfo.label

                    eblanApplicationInfoLabel != null && eblanApplicationInfoLabel.contains(
                        other = label,
                        ignoreCase = true,
                    )
                }
                .sortedBy { eblanShortcutInfo ->
                    eblanShortcutInfo.eblanApplicationInfo.label
                }.groupBy { eblanShortcutInfo ->
                    eblanShortcutInfo.eblanApplicationInfo
                }
        }.flowOn(defaultDispatcher)
    }
}