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
            resolveConflictsForPage(
                page = page,
                gridItems = gridItemsWithUpdateGridItem,
            )
        )
    }

    private fun getItemDimensions(cells: List<GridCell>): Pair<Int, Int> {
        val uniqueRows = cells.map { it.row }.distinct().count()
        val uniqueCols = cells.map { it.column }.distinct().count()
        return uniqueRows to uniqueCols
    }

    private fun findAvailableRegion(
        occupied: Set<GridCell>,
        requiredRows: Int,
        requiredCols: Int,
        gridSize: Int = 4
    ): List<GridCell>? {
        for (startRow in 0 until gridSize) {
            for (startCol in 0 until gridSize) {
                // Check if the region would fit within the grid boundaries.
                if (startRow + requiredRows > gridSize || startCol + requiredCols > gridSize) continue

                val candidate = mutableListOf<GridCell>()
                var fits = true
                // Build a candidate region.
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

    /**
     * Resolves cell conflicts for all grid items on the given page.
     *
     * @param page The target page.
     * @param gridItems The complete list of grid items.
     * @param gridSize The grid size (default is 4 for a 4x4 grid).
     * @param selectedItem The item that was moved. Its cells are locked in and will not be changed.
     * @return A new list of grid items with conflicts resolved on the target page.
     */
    private fun resolveConflictsForPage(
        page: Int,
        gridItems: List<GridItem>,
        gridSize: Int = 4,
        selectedItem: GridItem? = null
    ): List<GridItem> {
        // Set to track occupied cells on the page.
        val occupiedCells = mutableSetOf<GridCell>()
        // List to accumulate the resolved items for the target page.
        val resolvedItems = mutableListOf<GridItem>()

        // 1. Lock the selected (moved) item if provided.
        selectedItem?.let { selItem ->
            if (selItem.page == page) {
                occupiedCells.addAll(selItem.cells)
                resolvedItems.add(selItem)
            }
        }

        // 2. Process all other items on the target page, excluding the selected item.
        gridItems.filter {
            it.page == page && (selectedItem == null || it.id != selectedItem.id)
        }.forEach { item ->
            // Check if any of the item's cells conflict with already occupied ones.
            if (item.cells.any { it in occupiedCells }) {
                // Determine the item's dimensions.
                val (rows, cols) = getItemDimensions(item.cells)
                // Try to find an available region.
                val newRegion = findAvailableRegion(occupiedCells, rows, cols, gridSize)
                if (newRegion != null) {
                    // Use the new region and mark these cells as occupied.
                    occupiedCells.addAll(newRegion)
                    resolvedItems.add(item.copy(cells = newRegion))
                } else {
                    // If no region is available, leave the item as-is.
                    occupiedCells.addAll(item.cells)
                    resolvedItems.add(item)
                }
            } else {
                // No conflict – mark the cells as occupied.
                occupiedCells.addAll(item.cells)
                resolvedItems.add(item)
            }
        }

        // 3. Items on other pages remain unchanged.
        val otherPagesItems = gridItems.filter { it.page != page }
        return otherPagesItems + resolvedItems
    }
}