package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GridItemsByPageUseCase @Inject constructor(
    private val gridRepository: GridRepository,
) {
    operator fun invoke(): Flow<Map<Int, List<GridItem>>> {
        return gridRepository.gridItems.map { gridItems ->
            gridItems.groupBy { gridItem -> gridItem.page }
        }.flowOn(Dispatchers.Default)
    }
}