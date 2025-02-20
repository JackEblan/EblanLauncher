package com.eblan.launcher.data.repository

import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class DefaultGridRepository @Inject constructor() : GridRepository {
    private val _gridItemsFlow = MutableSharedFlow<List<GridItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val gridItems = _gridItemsFlow.asSharedFlow()

    override suspend fun insertGridItems() {
        val gridItems = listOf(
            GridItem(
                page = 0, id = 0,
                cells = listOf(
                    GridCell(row = 0, column = 3),
                ),
            ),
            GridItem(
                page = 0, id = 1,
                cells = listOf(
                    GridCell(row = 1, column = 1),
                ),
            ),
            GridItem(
                page = 0, id = 2,
                cells = listOf(
                    GridCell(row = 0, column = 0),
                ),
            ),
        )

        _gridItemsFlow.emit(gridItems)
    }

    override suspend fun updateGridItems(gridItems: List<GridItem>) {
        _gridItemsFlow.emit(gridItems)
    }
}