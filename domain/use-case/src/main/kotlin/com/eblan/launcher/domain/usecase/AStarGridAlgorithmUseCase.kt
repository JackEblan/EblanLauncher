package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resolveConflicts
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AStarGridAlgorithmUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(movingGridItem: GridItem) {
        withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            val gridItems = gridRepository.gridItems.first().filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = userData.rows,
                    columns = userData.columns,
                ) && gridItem.id != movingGridItem.id && gridItem.page == movingGridItem.page
            }

            val resolvedConflictsGridItems = resolveConflicts(
                gridItems = gridItems,
                movingGridItem = movingGridItem,
                rows = userData.rows,
                columns = userData.columns,
            )

            if (resolvedConflictsGridItems != null) {
                gridRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
            }
        }
    }
}