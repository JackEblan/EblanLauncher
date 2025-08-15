package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.repository.PageCacheRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CachePageItemsUseCase @Inject constructor(
    private val pageCacheRepository: PageCacheRepository,
    private val userDataRepository: UserDataRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(gridItems: List<GridItem>) {
        withContext(defaultDispatcher) {
            val userData = userDataRepository.userData.first()

            val gridItemsByPage = gridItems.filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = userData.homeSettings.rows,
                    columns = userData.homeSettings.columns,
                ) && gridItem.associate == Associate.Grid
            }.groupBy { gridItem -> gridItem.page }

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