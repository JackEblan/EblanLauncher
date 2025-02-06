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
                    GridCell(row = 0, column = 1)
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

        val newGridItem = gridItem.copy(page = page)

        val gridItemsWithUpdateGridItem = currentGridItems.toMutableList().apply {
            set(oldGridItemIndex, newGridItem)
        }

        _gridItemsFlow.emit(
            solveConflicts(
                page = page,
                gridItems = gridItemsWithUpdateGridItem,
                movingItem = newGridItem,
            )
        )
    }

    private fun getItemDimensions(cells: List<GridCell>): Pair<Int, Int> {
        val uniqueRows = cells.map { it.row }.distinct().count()
        val uniqueCols = cells.map { it.column }.distinct().count()
        return uniqueRows to uniqueCols
    }

    private fun findAvailableRegion(
        grid: Array<BooleanArray>, requiredRows: Int, requiredCols: Int
    ): List<GridCell>? {
        val rows = grid.size
        val cols = grid[0].size

        for (startRow in 0 until rows) {
            for (startCol in 0 until cols) {
                if (startRow + requiredRows > rows || startCol + requiredCols > cols) continue

                val candidate = mutableListOf<GridCell>()
                var fits = true

                for (r in startRow until startRow + requiredRows) {
                    for (c in startCol until startCol + requiredCols) {
                        if (grid[r][c]) {
                            fits = false
                            break
                        }
                        candidate.add(GridCell(r, c))
                    }
                    if (!fits) break
                }
                if (fits) return candidate
            }
        }
        return null
    }

    private fun solveConflicts(
        page: Int,
        gridItems: List<GridItem>,
        movingItem: GridItem,
        gridRows: Int = 4,
        gridCols: Int = 4
    ): List<GridItem> {
        val grid = Array(gridRows) { BooleanArray(gridCols) }
        val resolvedItems = mutableListOf<GridItem>()

        // **Step 1: Lock the moving item**
        resolvedItems.add(movingItem)
        movingItem.cells.forEach { grid[it.row][it.column] = true }

        // **Step 2: Process other items, move only if needed**
        gridItems.filter { it.page == page && it.id != movingItem.id }.forEach { item ->
            if (item.cells.any { grid[it.row][it.column] }) {
                val (rows, cols) = getItemDimensions(item.cells)
                val newRegion = findAvailableRegion(grid, rows, cols)

                if (newRegion != null) {
                    newRegion.forEach { grid[it.row][it.column] = true }
                    resolvedItems.add(item.copy(cells = newRegion))
                } else {
                    item.cells.forEach { grid[it.row][it.column] = true }
                    resolvedItems.add(item)
                }
            } else {
                item.cells.forEach { grid[it.row][it.column] = true }
                resolvedItems.add(item)
            }
        }

        return resolvedItems + gridItems.filter { it.page != page }
    }
}