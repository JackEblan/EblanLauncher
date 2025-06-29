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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GroupGridItemsByPageUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(): Flow<GridItemsByPage> {
        val gridItemsFlow = gridCacheRepository.isCache.flatMapLatest { isCache ->
            if (isCache) {
                gridCacheRepository.gridCacheItems
            } else {
                gridRepository.gridItems
            }
        }
        
        return combine(
            userDataRepository.userData,
            gridItemsFlow,
        ) { userData, gridItems ->
            val gridItemsSpanWithinBounds = gridItems.filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = userData.homeSettings.rows,
                    columns = userData.homeSettings.columns,
                ) && gridItem.associate == Associate.Grid
            }.groupBy { gridItem -> gridItem.page }

            val dockGridItemsWithinBounds = gridItems.filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = userData.homeSettings.dockRows,
                    columns = userData.homeSettings.dockColumns,
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