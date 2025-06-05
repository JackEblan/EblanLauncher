package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItemsByPage
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GroupGridItemsByPageUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(): Flow<GridItemsByPage> {
        return combine(
            userDataRepository.userData,
            gridCacheRepository.isCache,
            gridRepository.gridItems,
            gridCacheRepository.gridCacheItems,
        ) { userData, isCache, gridItems, gridCacheItems ->
            val currentGridItems = if (isCache) {
                gridCacheItems
            } else {
                gridItems
            }

            val gridItemsSpanWithinBounds = currentGridItems.filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = userData.rows,
                    columns = userData.columns,
                ) && gridItem.associate == Associate.Grid
            }.groupBy { gridItem -> gridItem.page }

            val dockGridItemsWithinBounds = currentGridItems.filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = userData.dockRows,
                    columns = userData.dockColumns,
                ) && gridItem.associate == Associate.Dock
            }

            GridItemsByPage(
                userData = userData,
                gridItems = gridItemsSpanWithinBounds,
                dockGridItems = dockGridItemsWithinBounds,
            )
        }.flowOn(Dispatchers.Default)
    }
}