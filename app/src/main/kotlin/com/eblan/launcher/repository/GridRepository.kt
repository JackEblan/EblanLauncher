package com.eblan.launcher.repository

import com.eblan.launcher.domain.model.EdgeState
import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItem
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class GridRepository {
    private val _gridItemsFlow = MutableSharedFlow<List<GridItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val gridItems = _gridItemsFlow.asSharedFlow()

    suspend fun insertGridItems() {
        val gridItems = listOf(
            GridItem(
                page = 0, id = 0,
                cells = listOf(
                    GridCell(row = 0, column = 3)
                ),
                edgeState = EdgeState.None,
            ), GridItem(
                page = 0, id = 1,
                cells = listOf(
                    GridCell(row = 1, column = 1)
                ),
                edgeState = EdgeState.None,
            ), GridItem(
                page = 0, id = 2,
                cells = listOf(
                    GridCell(row = 0, column = 0)
                ),
                edgeState = EdgeState.None,
            )
        )

        _gridItemsFlow.emit(gridItems)
    }

    suspend fun updateGridItems(gridItems: List<GridItem>) {
        _gridItemsFlow.emit(gridItems)
    }
}