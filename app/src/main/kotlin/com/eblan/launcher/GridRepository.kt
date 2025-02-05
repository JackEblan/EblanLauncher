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
                page = 0, id = 1, cells = listOf(
                    GridCell(row = 1, column = 1)
                )
            ), GridItem(
                page = 0, id = 2, cells = listOf(
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
            val newGridItem = gridItem.copy(page = page)
            set(oldGridItemIndex, newGridItem)
        }

        _gridItemsFlow.emit(
            resolveGridConflicts(
                page = page,
                gridItems = gridItemsWithUpdateGridItem,
            )
        )
    }

    private fun resolveGridConflicts(page: Int, gridItems: List<GridItem>): List<GridItem> {
        val occupiedCells = mutableSetOf<GridCell>()
        val updatedItems = mutableListOf<GridItem>()

        // Filter items for the target page
        val itemsOnPage = gridItems.filter { it.page == page }

        for (item in itemsOnPage) {
            val conflictingCells = item.cells.filter { it in occupiedCells }

            if (conflictingCells.isNotEmpty()) {
                // Calculate the item's original dimensions
                val (rows, cols) = getItemDimensions(item.cells)
                // Find a new region matching the item's size
                val newRegion = findAvailableRegion(occupiedCells, rows, cols)

                newRegion?.let {
                    occupiedCells.addAll(it)
                    updatedItems.add(item.copy(cells = it))
                } ?: run {
                    // Handle no available space (e.g., keep original, even with conflict)
                    occupiedCells.addAll(item.cells)
                    updatedItems.add(item)
                }
            } else {
                occupiedCells.addAll(item.cells)
                updatedItems.add(item)
            }
        }

        // Combine updated items with items from other pages
        return gridItems.filter { it.page != page } + updatedItems
    }

    // Helper to determine item dimensions (rows x cols)
    private fun getItemDimensions(cells: List<GridCell>): Pair<Int, Int> {
        val minRow = cells.minOf { it.row }
        val maxRow = cells.maxOf { it.row }
        val minCol = cells.minOf { it.column }
        val maxCol = cells.maxOf { it.column }
        return (maxRow - minRow + 1) to (maxCol - minCol + 1)
    }

    // Finds the top-left corner of the first available region of size rows x cols
    private fun findAvailableRegion(
        occupied: Set<GridCell>, requiredRows: Int, requiredCols: Int
    ): List<GridCell>? {
        for (startRow in 0 until 4) {
            for (startCol in 0 until 4) {
                // Check if the region fits within the 4x4 grid
                if (startRow + requiredRows > 4 || startCol + requiredCols > 4) continue

                val cells = mutableListOf<GridCell>()
                var isAvailable = true
                for (r in startRow until startRow + requiredRows) {
                    for (c in startCol until startCol + requiredCols) {
                        val cell = GridCell(r, c)
                        if (cell in occupied) {
                            isAvailable = false
                            break
                        }
                        cells.add(cell)
                    }
                    if (!isAvailable) break
                }
                if (isAvailable) return cells
            }
        }
        return null // No space found
    }
}