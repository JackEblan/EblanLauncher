package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.AppWidgetHostDomainWrapper
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteWidgetGridItemUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val appWidgetHostDomainWrapper: AppWidgetHostDomainWrapper,
) {
    suspend operator fun invoke(id: Int) {
        val gridItem = gridCacheRepository.gridCacheItems.first().find { it.id == id }

        val data = gridItem?.data

        if (data is GridItemData.Widget) {
            appWidgetHostDomainWrapper.deleteAppWidgetId(appWidgetId = data.appWidgetId)

            gridCacheRepository.deleteGridItem(gridItem)
        }
    }
}