package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resolveConflicts
import com.eblan.launcher.domain.grid.resolveConflictsWithShift
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
    suspend operator fun invoke(gridItem: GridItem): List<GridItem>? {
        return withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            if (isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = userData.rows,
                    columns = userData.columns,
                )
            ) {
                val gridItems = gridRepository.gridItems.first()

                val resolvedConflictsGridItems = resolveConflictsWithShift(
                    gridItems = gridItems,
                    movingGridItem = gridItem,
                    rows = userData.rows,
                    columns = userData.columns,
                )

                if (resolvedConflictsGridItems != null) {
                    gridRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
                }

                resolvedConflictsGridItems
            } else {
                null
            }
        }
    }
}