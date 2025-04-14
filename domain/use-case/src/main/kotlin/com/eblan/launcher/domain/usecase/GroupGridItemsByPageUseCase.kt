package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.GridItemsByPage
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GroupGridItemsByPageUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(): Flow<GridItemsByPage> {
        return combine(
            gridRepository.gridItems, userDataRepository.userData,
        ) { gridItems, userData ->
            val gridItemsSpanWithinBounds = gridItems.filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = userDataRepository.userData.first().rows,
                    columns = userDataRepository.userData.first().columns,
                )
            }.groupBy { gridItem -> gridItem.page }

            GridItemsByPage(
                userData = userData,
                gridItems = gridItemsSpanWithinBounds,
            )
        }.flowOn(Dispatchers.Default)
    }
}