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

class CachePageItemsUseCase @Inject constructor(
    private val pageCacheRepository: PageCacheRepository,
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke() {
        withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            val gridItems = gridRepository.gridItems.first().filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = userData.homeSettings.rows,
                    columns = userData.homeSettings.columns,
                ) && gridItem.associate == Associate.Grid
            }

            val gridItemsByPage = gridItems.groupBy { gridItem -> gridItem.page }

            val pageItems = (0 until userData.homeSettings.pageCount).map { page ->
                PageItem(
                    id = page,
                    gridItems = gridItemsByPage[page] ?: emptyList(),
                )
            }

            pageCacheRepository.insertPageItems(pageItems)
        }
    }
}