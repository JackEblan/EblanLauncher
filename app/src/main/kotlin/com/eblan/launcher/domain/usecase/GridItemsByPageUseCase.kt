package com.eblan.launcher.domain.usecase

import com.eblan.launcher.GridItem
import com.eblan.launcher.GridRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class GridItemsByPageUseCase(private val gridRepository: GridRepository) {
    operator fun invoke(): Flow<Map<Int, List<GridItem>>> {
        return gridRepository.gridItems.onStart { gridRepository.insertGridItems() }
            .map { gridItems ->
                gridItems.groupBy { gridItem -> gridItem.page }
            }.flowOn(Dispatchers.Default)
    }
}