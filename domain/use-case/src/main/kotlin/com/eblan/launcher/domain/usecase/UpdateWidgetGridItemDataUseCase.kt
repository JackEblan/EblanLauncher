package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridRepository
import javax.inject.Inject

class UpdateWidgetGridItemDataUseCase @Inject constructor(
    private val gridRepository: GridRepository,
) {
    suspend operator fun invoke(gridItem: GridItem, appWidgetId: Int) {
        if (appWidgetId == -1) {
            gridRepository.deleteGridItem(gridItem = gridItem)
        } else {
            val gridItemData = gridItem.data

            if (gridItemData is GridItemData.Widget) {
                gridRepository.updateGridItemData(
                    id = gridItem.id,
                    data = gridItemData.copy(appWidgetId = appWidgetId),
                )
            }
        }
    }
}