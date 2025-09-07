package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItemCache
import com.eblan.launcher.domain.model.GridItemCacheType
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetGridItemsCacheUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(): Flow<GridItemCache> {
        return combine(
            userDataRepository.userData,
            gridCacheRepository.gridItemsCache,
            gridCacheRepository.gridItemCacheType,
        ) { userData, gridItems, gridItemCacheType ->
            when (gridItemCacheType) {
                GridItemCacheType.Grid -> {
                    val gridItemsWithinBounds = gridItems.filter { gridItem ->
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

                    GridItemCache(
                        gridItemsCacheByPage = gridItemsWithinBounds,
                        dockGridItemsCache = dockGridItemsWithinBounds,
                    )
                }

                GridItemCacheType.Folder -> {
                    val gridItemsWithinBounds = gridItems.filter { gridItem ->
                        isGridItemSpanWithinBounds(
                            gridItem = gridItem,
                            rows = userData.homeSettings.folderRows,
                            columns = userData.homeSettings.folderColumns,
                        ) && gridItem.associate == Associate.Grid
                    }.groupBy { gridItem -> gridItem.page }

                    GridItemCache(
                        gridItemsCacheByPage = gridItemsWithinBounds,
                        dockGridItemsCache = emptyList(),
                    )
                }
            }
        }
    }
}