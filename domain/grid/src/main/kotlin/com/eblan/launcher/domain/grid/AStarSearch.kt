package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import java.util.PriorityQueue
import kotlin.math.abs

/**
 * Resolves layout conflicts among grid items on a specified page when a grid item is moved.
 *
 * Updates the positions of grid items to prevent overlapping when the [movingGridItem]
 * is placed onto the grid. It operates on a per‑page basis, marking occupied cells in a
 * boolean grid based on each item’s starting position and span. Non‑conflicting items (those
 * that do not overlap with the moving item) are added directly. For conflicting items,
 * the algorithm uses an A* search strategy to find a new free position that fits the item’s spans.
 * If a free region cannot be found for any conflicting item, the function returns `null`.
 *
 * @param gridItems A list of all grid items.
 * @param movingGridItem The grid item being moved, which may cause conflicts.
 * @param rows The total number of rows in the grid.
 * @param columns The total number of columns in the grid.
 * @return A list of grid items with updated positions if successful, or `null` if any conflict cannot be resolved.
 */
fun resolveConflicts(
    gridItems: List<GridItem>,
    movingGridItem: GridItem,
    rows: Int,
    columns: Int,
): List<GridItem>? {
    // Create a grid to track occupied cells; true indicates an occupied cell.
    val grid = Array(rows) { BooleanArray(columns) }
    val resolvedItems = mutableListOf<GridItem>()

    // Mark the moving grid item’s region as occupied.
    markRegion(
        grid = grid,
        startRow = movingGridItem.startRow,
        startCol = movingGridItem.startColumn,
        rowSpan = movingGridItem.rowSpan,
        colSpan = movingGridItem.columnSpan,
    )
    resolvedItems.add(movingGridItem)

    // Partition items into non-conflicting and conflicting groups based on rectangle overlap.
    val (conflicting, nonConflicting) = gridItems.filter { gridItem ->
        isGridItemSpanWithinBounds(
            gridItem = gridItem,
            rows = rows,
            columns = columns,
        ) && gridItem.id != movingGridItem.id && gridItem.page == movingGridItem.page
    }.partition { gridItem ->
        rectanglesOverlap(movingGridItem = movingGridItem, gridItem = gridItem)
    }

    // Process non-conflicting items: mark their regions and add them as-is.
    nonConflicting.forEach { item ->
        markRegion(grid, item.startRow, item.startColumn, item.rowSpan, item.columnSpan)
        resolvedItems.add(item)
    }

    // Process conflicting items: reposition them using the A* search algorithm.
    conflicting.forEach { gridItem ->
        // Use the current position as the starting point for the search.
        val newPosition = aStarSearchAlgorithm(
            grid = grid,
            requiredRows = gridItem.rowSpan,
            requiredCols = gridItem.columnSpan,
            startRow = gridItem.startRow,
            startCol = gridItem.startColumn,
        )
        if (newPosition != null) {
            val (newStartRow, newStartColumn) = newPosition
            markRegion(
                grid = grid,
                startRow = newStartRow,
                startCol = newStartColumn,
                rowSpan = gridItem.rowSpan,
                colSpan = gridItem.columnSpan,
            )
            resolvedItems.add(gridItem.copy(startRow = newStartRow, startColumn = newStartColumn))
        } else {
            // If no valid region is found, conflict resolution fails.
            return null
        }
    }

    return resolvedItems
}

/**
 * Marks a rectangular region in the grid as occupied.
 *
 * @param grid The grid represented as a 2D boolean array.
 * @param startRow The starting row index.
 * @param startCol The starting column index.
 * @param rowSpan The number of rows the region spans.
 * @param colSpan The number of columns the region spans.
 */
private fun markRegion(
    grid: Array<BooleanArray>,
    startRow: Int,
    startCol: Int,
    rowSpan: Int,
    colSpan: Int,
) {
    for (r in startRow until startRow + rowSpan) {
        for (c in startCol until startCol + colSpan) {
            grid[r][c] = true
        }
    }
}

/**
 * Data class representing a node used in the A* search algorithm.
 *
 * @property row The row position of the node.
 * @property col The column position of the node.
 * @property g The cost from the starting position to this node.
 * @property h The heuristic estimate (using Manhattan distance) from this node to the starting position.
 * @property f The total cost (g + h) used for node prioritization.
 */
private data class Node(val row: Int, val col: Int, val g: Int, val h: Int) {
    val f: Int get() = g + h
}

/**
 * Uses the A* search algorithm to locate a free region in the grid that can fit an item
 * with the specified spans.
 *
 * Starting from the provided [startRow] and [startCol], the algorithm explores neighboring cells
 * (up, down, left, and right) using a priority queue. For each candidate top-left position, it checks if a
 * rectangular region of size [requiredRows] x [requiredCols] is free. Upon finding a valid region, it returns
 * a Pair of integers representing the top-left cell of that region.
 *
 * @param grid The current grid as a 2D boolean array where `true` denotes an occupied cell.
 * @param requiredRows The number of rows required by the grid item.
 * @param requiredCols The number of columns required by the grid item.
 * @param startRow The starting row for the search.
 * @param startCol The starting column for the search.
 * @return A Pair representing the top-left coordinate of a free region that fits the required dimensions, or `null` if none is found.
 */
private fun aStarSearchAlgorithm(
    grid: Array<BooleanArray>, requiredRows: Int, requiredCols: Int, startRow: Int, startCol: Int,
): Pair<Int, Int>? {
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

        // Check if a region starting at (r, c) fits inside the grid.
        if (r + requiredRows <= rows && c + requiredCols <= cols) {
            var fits = true
            for (i in r until r + requiredRows) {
                for (j in c until c + requiredCols) {
                    if (grid[i][j]) {
                        fits = false
                        break
                    }
                }
                if (!fits) break
            }
            if (fits) return r to c
        }

        // Expand neighbors: Up, Down, Left, Right.
        listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1).forEach { (dr, dc) ->
            val nr = r + dr
            val nc = c + dc
            if (nr in 0 until rows && nc in 0 until cols && (nr to nc) !in visited) {
                visited.add(nr to nc)
                val newG = current.g + 1
                val newH = abs(nr - startRow) + abs(nc - startCol)
                queue.add(Node(nr, nc, newG, newH))
            }
        }
    }
    return null
}
