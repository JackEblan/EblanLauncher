package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.EblanApplicationComponent
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetEblanApplicationComponentUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
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

            val pageCount = if (launcherAppsWrapper.hasShortcutHostPermission) {
                3
            } else {
                2
            }

            EblanApplicationComponent(
                eblanApplicationInfos = sortedEblanApplicationInfos,
                eblanAppWidgetProviderInfos = groupedEblanAppWidgetProviderInfos,
                eblanShortcutInfos = eblanShortcutInfos,
                pageCount = pageCount,
            )
        }
    }
}