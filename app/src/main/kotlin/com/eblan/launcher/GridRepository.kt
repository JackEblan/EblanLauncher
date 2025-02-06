package com.eblan.launcher

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.PriorityQueue
import kotlin.math.abs

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
        grid: Array<BooleanArray>,
        requiredRows: Int,
        requiredCols: Int,
        startRow: Int,
        startCol: Int
    ): List<GridCell>? {
        val rows = grid.size
        val cols = grid[0].size

        // Priority queue for A* (sorts by Manhattan distance)
        val queue = PriorityQueue<Triple<Int, Int, Int>>(compareBy { it.third })
        val visited = mutableSetOf<Pair<Int, Int>>()

        queue.add(Triple(startRow, startCol, 0)) // (row, col, heuristic)
        visited.add(startRow to startCol)

        while (queue.isNotEmpty()) {
            val (r, c, _) = queue.poll() ?: break

            // Check if required space fits at (r, c)
            if (r + requiredRows <= rows && c + requiredCols <= cols) {
                val candidate = mutableListOf<GridCell>()
                var fits = true

                for (i in r until r + requiredRows) {
                    for (j in c until c + requiredCols) {
                        if (grid[i][j]) { // If occupied, discard this region
                            fits = false
                            break
                        }
                        candidate.add(GridCell(i, j))
                    }
                    if (!fits) break
                }

                if (fits) return candidate // Found the best available space
            }

            // Add neighboring cells with a heuristic (Manhattan distance)
            listOf(
                r - 1 to c, r + 1 to c,  // Up & Down
                r to c - 1, r to c + 1   // Left & Right
            ).filter { (nr, nc) -> nr in 0 until rows && nc in 0 until cols && (nr to nc) !in visited }
                .forEach { (nr, nc) ->
                    val heuristic = abs(nr - startRow) + abs(nc - startCol)
                    queue.add(Triple(nr, nc, heuristic))
                    visited.add(nr to nc)
                }
        }

        return null // No space found
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

        // Ensure we don't try to access out-of-bounds indices
        movingItem.cells.forEach { cell ->
            if (cell.row in 0 until gridRows && cell.column in 0 until gridCols) {
                grid[cell.row][cell.column] = true
            } else {
                // Handle the case where the cell is out of bounds, maybe log a warning
                println("Warning: Moving item cell out of bounds: ${cell.row}, ${cell.column}")
            }
        }

        // **Step 2: Process other items, move only if needed**
        gridItems.filter { it.page == page && it.id != movingItem.id }.forEach { item ->
            if (item.cells.any { grid[it.row][it.column] }) {
                val (rows, cols) = getItemDimensions(item.cells)
                val newRegion = findAvailableRegion(
                    grid = grid,
                    requiredRows = rows,
                    requiredCols = cols,
                    startRow = movingItem.cells.first().row,
                    startCol = movingItem.cells.first().column
                )

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