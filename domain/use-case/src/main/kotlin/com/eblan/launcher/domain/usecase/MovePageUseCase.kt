package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MovePageUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
) {
    suspend operator fun invoke(from: Int, to: Int): Int {
        return withContext(Dispatchers.Default) {
            val gridItems = gridCacheRepository.gridCacheItems.first()
                .filter { it.associate == Associate.Grid }

            val movePageMovingGridItems =
                gridItems.filter { gridItem -> gridItem.page == from }
                    .map { gridItem -> gridItem.copy(page = to) }

            val movePageOtherGridItems = gridItems.filter { it.page != from }.map { gridItem ->
                when {
                    from < to && gridItem.page in (from + 1)..to ->
                        gridItem.copy(page = gridItem.page - 1)

                    from > to && gridItem.page in to until from ->
                        gridItem.copy(page = gridItem.page + 1)

                    else -> gridItem
                }
            }

            gridCacheRepository.upsertGridItems(gridItems = movePageMovingGridItems + movePageOtherGridItems)

            to
        }
    }
}