package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdatePageItemsUseCase @Inject constructor(
    private val gridRepository: GridRepository,
) {
    suspend operator fun invoke(pageItems: List<PageItem>) {
        withContext(Dispatchers.Default) {
            val gridItems = pageItems.mapIndexed { index, pageItem ->
                pageItem.gridItems.map { gridItem ->
                    gridItem.copy(page = index)
                }
            }.flatten()

            gridRepository.upsertGridItems(gridItems = gridItems)
        }
    }
}