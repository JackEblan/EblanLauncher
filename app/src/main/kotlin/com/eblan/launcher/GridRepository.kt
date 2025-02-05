package com.eblan.launcher

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class GridRepository {
    private val _gridItemsFlow = MutableSharedFlow<List<GridItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val currentGridItems get() = _gridItemsFlow.replayCache.firstOrNull() ?: emptyList()

    val gridItems = _gridItemsFlow.asSharedFlow()

    suspend fun insertGridItems() {
        val gridItems = listOf(
            GridItem(
                page = 0, id = 0, cells = listOf(
                    GridCell(row = 0, column = 0)
                )
            ), GridItem(
                page = 1, id = 1, cells = listOf(
                    GridCell(row = 0, column = 0)
                )
            )
        )

        _gridItemsFlow.emit(gridItems)
    }

    suspend fun updateGridItem(page: Int, gridItem: GridItem) {
        val oldGridItem = currentGridItems.find { it.id == gridItem.id }

        val oldGridItemIndex = currentGridItems.indexOf(oldGridItem)

        val gridItemsWithUpdateGridItem = currentGridItems.toMutableList().apply {
            set(oldGridItemIndex, gridItem.copy(page = page))
        }

        _gridItemsFlow.emit(gridItemsWithUpdateGridItem)
    }
}