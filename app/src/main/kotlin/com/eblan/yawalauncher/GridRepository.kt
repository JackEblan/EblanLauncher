package com.eblan.yawalauncher

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class GridRepository {
    private val _gridItemsFlow = MutableSharedFlow<Map<Int, List<GridItem>>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val currentGridItems get() = _gridItemsFlow.replayCache.firstOrNull() ?: emptyMap()

    val gridItems = _gridItemsFlow.asSharedFlow()

    suspend fun insertGridItems() {
        val gridItems = mapOf(
            0 to listOf(
                GridItem(
                    page = 0, cells = listOf(
                        GridCell(row = 0, column = 0)
                    )
                )
            ), 1 to listOf(
                GridItem(
                    page = 1, cells = listOf(
                        GridCell(row = 1, column = 1)
                    )
                )
            )
        )
        _gridItemsFlow.emit(gridItems)
    }

    suspend fun updateGridItem(page: Int, index: Int, gridItem: GridItem) {
        val gridItemsByPage = currentGridItems[page]

        if (gridItemsByPage != null) {
            val deleteOldGridItem = gridItemsByPage - gridItemsByPage[index]

            val addNewGridItem = deleteOldGridItem + gridItem

            _gridItemsFlow.emit(currentGridItems.plus(page to addNewGridItem))
        }
    }
}