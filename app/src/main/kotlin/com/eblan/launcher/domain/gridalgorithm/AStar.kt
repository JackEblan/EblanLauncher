package com.eblan.launcher.domain.gridalgorithm

import com.eblan.launcher.GridCell
import com.eblan.launcher.GridItem
import java.util.PriorityQueue

class AStar {
    operator fun invoke(
        page: Int,
        gridItems: List<GridItem>,
        movingGridItem: GridItem,
        gridRows: Int = 4,
        gridCols: Int = 4
    ): List<GridItem> {
        val grid = Array(gridRows) { BooleanArray(gridCols) }
        val resolvedItems = mutableListOf<GridItem>()

        // Step 1: Lock the moving item.
        resolvedItems.add(movingGridItem)
        movingGridItem.cells.forEach { cell ->
            if (cell.row in 0 until gridRows && cell.column in 0 until gridCols) grid[cell.row][cell.column] =
                true
            else println("Warning: moving item cell out of bounds: ${cell.row}, ${cell.column}")
        }
        val movingCells = movingGridItem.cells.toSet()

        // Step 2: Process other items on the same page.
        gridItems.filter { it.page == page && it.id != movingGridItem.id }
            .sortedBy { it.id } // ensure stable ordering
            .forEach { item ->
                // Check conflict: only if the item's cells intersect with the moving item's cells.
                if (item.cells.any { it in movingCells }) {
                    val (reqRows, reqCols) = getItemDimensions(item.cells)
                    // Instead of starting from the moving item’s position,
                    // start from the conflicting item’s original first cell.
                    val startR = item.cells.first().row
                    val startC = item.cells.first().column

                    val newRegion = findAvailableRegion(grid, reqRows, reqCols, startR, startC)
                    if (newRegion != null) {
                        newRegion.forEach { cell ->
                            if (cell.row in 0 until gridRows && cell.column in 0 until gridCols) grid[cell.row][cell.column] =
                                true
                        }
                        resolvedItems.add(item.copy(cells = newRegion))
                    } else {
                        // If no free region is found, leave the item in place.
                        item.cells.forEach { cell ->
                            if (cell.row in 0 until gridRows && cell.column in 0 until gridCols) grid[cell.row][cell.column] =
                                true
                        }
                        resolvedItems.add(item)
                    }
                } else {
                    // Item doesn't conflict with the moving item.
                    item.cells.forEach { cell ->
                        if (cell.row in 0 until gridRows && cell.column in 0 until gridCols) grid[cell.row][cell.column] =
                            true
                    }
                    resolvedItems.add(item)
                }
            }

        // Step 3: Return resolved items along with items from other pages unchanged.
        return resolvedItems + gridItems.filter { it.page != page }
    }

    // A node for A* search, representing a cell and its associated costs.
    private data class Node(val row: Int, val col: Int, val g: Int, val h: Int) {
        val f: Int get() = g + h
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
        val queue = PriorityQueue<Node>(compareBy { it.f })
        val visited = mutableSetOf<Pair<Int, Int>>()

        queue.add(Node(startRow, startCol, 0, 0))
        visited.add(startRow to startCol)

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: break
            val r = current.row
            val c = current.col

            // Check if region starting at (r, c) fits inside the grid.
            if (r + requiredRows <= rows && c + requiredCols <= cols) {
                var fits = true
                val candidate = mutableListOf<GridCell>()
                for (i in r until r + requiredRows) {
                    for (j in c until c + requiredCols) {
                        if (grid[i][j]) {
                            fits = false
                            break
                        }
                        candidate.add(GridCell(i, j))
                    }
                    if (!fits) break
                }
                if (fits) return candidate
            }

            // Expand neighbors: Up, Down, Left, Right.
            listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1).forEach { (dr, dc) ->
                val nr = r + dr
                val nc = c + dc
                if (nr in 0 until rows && nc in 0 until cols && (nr to nc) !in visited) {
                    visited.add(nr to nc)
                    val newG = current.g + 1
                    val newH = kotlin.math.abs(nr - startRow) + kotlin.math.abs(nc - startCol)
                    queue.add(Node(nr, nc, newG, newH))
                }
            }
        }
        return null
    }

    private fun getItemDimensions(cells: List<GridCell>): Pair<Int, Int> {
        val uniqueRows = cells.map { it.row }.distinct().count()
        val uniqueCols = cells.map { it.column }.distinct().count()
        return uniqueRows to uniqueCols
    }
}