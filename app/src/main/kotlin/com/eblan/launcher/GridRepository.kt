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

        val gridItemsWithUpdateGridItem = currentGridItems.toMutableList().apply {
            val newGridItem = gridItem.copy(page = page)
            set(oldGridItemIndex, newGridItem)
        }

        _gridItemsFlow.emit(
            solveConflicts(
                page = page,
                gridItems = gridItemsWithUpdateGridItem,
                movingItem = gridItem,
            )
        )
    }

    private fun getItemDimensions(cells: List<GridCell>): Pair<Int, Int> {
        val uniqueRows = cells.map { it.row }.distinct().count()
        val uniqueCols = cells.map { it.column }.distinct().count()
        return uniqueRows to uniqueCols
    }

    private fun findAvailableRegion(
        occupied: Set<GridCell>, requiredRows: Int, requiredCols: Int, gridSize: Int = 4
    ): List<GridCell>? {
        for (startRow in 0 until gridSize) {
            for (startCol in 0 until gridSize) {
                if (startRow + requiredRows > gridSize || startCol + requiredCols > gridSize) continue

                val candidate = mutableListOf<GridCell>()
                var fits = true
                for (r in startRow until startRow + requiredRows) {
                    for (c in startCol until startCol + requiredCols) {
                        val cell = GridCell(r, c)
                        if (cell in occupied) {
                            fits = false
                            break
                        }
                        candidate.add(cell)
                    }
                    if (!fits) break
                }
                if (fits) return candidate
            }
        }
        return null
    }

    private fun solveConflicts(
        page: Int, gridItems: List<GridItem>, movingItem: GridItem, gridSize: Int = 4
    ): List<GridItem> {
        val occupiedCells = mutableSetOf<GridCell>()
        val resolvedItems = mutableListOf<GridItem>()

        // **Step 1: Lock the moving item (it never moves)**
        if (movingItem.page == page) {
            // If the moving item is already on the target page, mark its cells as occupied
            occupiedCells.addAll(movingItem.cells)
            resolvedItems.add(movingItem)
        } else {
            // If the moving item is being moved to this page, add it to resolvedItems and mark its cells as occupied
            resolvedItems.add(movingItem.copy(page = page))
            occupiedCells.addAll(movingItem.cells)
        }

        // **Step 2: Process other items (move only if they conflict)**
        gridItems.filter { it.page == page && it.id != movingItem.id }.forEach { item ->
            val conflictsWithMovingItem = item.cells.any { it in movingItem.cells }

            if (conflictsWithMovingItem) {
                // **Find a new space since it overlaps with the moving item**
                val (rows, cols) = getItemDimensions(item.cells)
                val newRegion = findAvailableRegion(occupiedCells, rows, cols, gridSize)

                if (newRegion != null) {
                    // **Move the conflicting item to the new space**
                    occupiedCells.addAll(newRegion)
                    resolvedItems.add(item.copy(cells = newRegion))
                } else {
                    // **No available space, keep it in place**
                    occupiedCells.addAll(item.cells)
                    resolvedItems.add(item)
                }
            } else {
                // **No conflict, keep the item in place**
                occupiedCells.addAll(item.cells)
                resolvedItems.add(item)
            }
        }

        // Items on other pages remain unchanged.
        val otherPagesItems = gridItems.filter { it.page != page }
        return otherPagesItems + resolvedItems
    }
}