package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridRepository
import javax.inject.Inject

class UpdateWidgetGridItemDataUseCase @Inject constructor(
    private val gridRepository: GridRepository,
) {
    suspend operator fun invoke(id: String, appWidgetId: Int) {
        val gridItemData = gridRepository.getGridItem(id = id)?.data

        if (gridItemData is GridItemData.Widget) {
            gridRepository.updateGridItemData(
                id = id,
                data = gridItemData.copy(appWidgetId = appWidgetId),
            )
        }
    }
}