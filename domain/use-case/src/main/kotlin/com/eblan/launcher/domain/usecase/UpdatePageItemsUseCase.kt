package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.PageCacheRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdatePageItemsUseCase @Inject constructor(
    private val pageCacheRepository: PageCacheRepository,
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke() {
        withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            val pageItems = gridRepository.gridItems.first().filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = userData.homeSettings.rows,
                    columns = userData.homeSettings.columns,
                ) && gridItem.associate == Associate.Grid
            }.groupBy { gridItem -> gridItem.page }.map { (page, gridItems) ->
                PageItem(id = page, gridItems = gridItems)
            }.sortedBy { pageItem -> pageItem.id }

            pageCacheRepository.insertPageItems(pageItems = pageItems)
        }
    }
}