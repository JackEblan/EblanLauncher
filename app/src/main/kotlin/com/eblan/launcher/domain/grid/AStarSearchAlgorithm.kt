package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItem
import java.util.PriorityQueue
import kotlin.math.abs

/**
 * Resolves layout conflicts among grid items on a specified page when a grid item is moved.
 *
 * This function updates the positions of grid items to prevent overlapping when the [movingGridItem]
 * is placed onto the grid. It operates on a per‑page basis, first marking the occupied cells in a
 * boolean grid. Non‑conflicting items (i.e. those that do not overlap with the moving item) are added
 * directly. For conflicting items, the algorithm uses an A* search strategy to find a new, free rectangular
 * region that fits the grid item’s dimensions. If a free region cannot be found for any conflicting item,
 * the function returns `null` to indicate that the conflict resolution failed.
 *
 * @param page The page number on which to resolve conflicts.
 * @param gridItems A list of all grid items.
 * @param movingGridItem The grid item being moved, which may cause conflicts.
 * @param rows The total number of rows in the grid.
 * @param columns The total number of columns in the grid.
 * @return A list of grid items with updated positions if successful, or `null` if any conflict cannot be resolved.
 */
fun resolveConflicts(
    page: Int,
    gridItems: List<GridItem>,
    movingGridItem: GridItem,
    rows: Int,
    columns: Int,
): List<GridItem>? {
    // Create a grid to track occupied cells; true indicates an occupied cell.
    val grid = Array(rows) { BooleanArray(columns) }
    val resolvedItems = mutableListOf<GridItem>()

    // Add the moving grid item to the result and mark its cells as occupied.
    resolvedItems.add(movingGridItem)
    movingGridItem.cells.forEach { cell ->
        grid[cell.row][cell.column] = true
    }

    // Filter grid items on the same page (excluding the moving item).
    val samePageItems = gridItems.filter { it.page == page && it.id != movingGridItem.id }

    // Partition items into non-conflicting and conflicting groups.
    val (nonConflicting, conflicting) = samePageItems.partition { item ->
        item.cells.none { it in movingGridItem.cells }
    }

    // Process non-conflicting items: mark their cells and add them as-is.
    nonConflicting.forEach { item ->
        item.cells.forEach { cell ->
            grid[cell.row][cell.column] = true
        }
        resolvedItems.add(item)
    }

    // Process conflicting items: attempt to reposition them using the A* search algorithm.
    conflicting.forEach { item ->
        // Retrieve the dimensions (required rows and columns) of the grid item.
        // Assumes the existence of a helper function 'getGridItemDimensions' that calculates these.
        val (reqRows, reqCols) = getGridItemDimensions(item.cells)
        // Use the top-left cell of the item as the starting point for the search.
        val startR = item.cells.first().row
        val startC = item.cells.first().column

        // Attempt to find a free region for this item.
        val newRegion = aStarSearchAlgorithm(grid, reqRows, reqCols, startR, startC)
        if (newRegion != null) {
            // Mark the newly allocated cells as occupied.
            newRegion.forEach { cell ->
                grid[cell.row][cell.column] = true
            }
            // Add the item with its new cell positions.
            resolvedItems.add(item.copy(cells = newRegion))
        } else {
            // If no valid region can be found, conflict resolution fails.
            return null
        }
    }

    // Return the updated grid items, including items from pages other than the specified one.
    return resolvedItems + gridItems.filter { it.page != page }
}

/**
 * Data class representing a node used in the A* search algorithm.
 *
 * @property row The row position of the node.
 * @property col The column position of the node.
 * @property g The cost from the starting position to this node.
 * @property h The heuristic estimate (using Chebyshev distance) from this node to the initial starting position.
 * @property f The total cost (g + h) used for node prioritization.
 */
private data class Node(val row: Int, val col: Int, val g: Int, val h: Int) {
    val f: Int get() = g + h
}

/**
 * Uses the A* search algorithm to locate a free rectangular region in the grid that can fit an item
 * with the specified dimensions.
 *
 * Starting from the provided [startRow] and [startCol], the algorithm explores neighboring cells
 * (including diagonal moves) by maintaining a priority queue of [Node] objects, sorted by their total cost [Node.f].
 * For each node, it checks if a rectangular region of size [requiredRows] x [requiredCols] is free using
 * [isRegionFree]. Upon finding a valid region, [generateRegion] is used to produce a list of [GridCell] objects
 * representing that region.
 *
 * @param grid The current grid represented as a 2D boolean array where `true` denotes an occupied cell.
 * @param requiredRows The number of rows required by the grid item.
 * @param requiredCols The number of columns required by the grid item.
 * @param startRow The starting row position for the search.
 * @param startCol The starting column position for the search.
 * @return A list of [GridCell] representing a free region that fits the required dimensions, or `null` if none is found.
 */
private fun aStarSearchAlgorithm(
    grid: Array<BooleanArray>, requiredRows: Int, requiredCols: Int, startRow: Int, startCol: Int
): List<GridCell>? {
    // Priority queue to select the node with the lowest total cost (f = g + h).
    val queue = PriorityQueue<Node>(compareBy { it.f })
    // Map to store the cost so far for each cell.
    val costSoFar = mutableMapOf<Pair<Int, Int>, Int>()
    queue.add(Node(startRow, startCol, 0, 0))
    costSoFar[startRow to startCol] = 0

    while (queue.isNotEmpty()) {
        val current = queue.poll() ?: break

        // Check if the rectangular region starting at the current node is free.
        if (isRegionFree(current.row, current.col, requiredRows, requiredCols, grid)) {
            return generateRegion(current.row, current.col, requiredRows, requiredCols)
        }

        // Explore all neighboring cells (including diagonals).
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue  // Skip the current cell.
                val nr = current.row + dr
                val nc = current.col + dc
                // Skip if the neighbor is outside the grid boundaries.
                if (nr !in grid.indices || nc !in 0 until grid[0].size) continue

                val newG = current.g + 1
                // The heuristic uses the maximum of row and column differences (Chebyshev distance).
                val newH = maxOf(abs(nr - startRow), abs(nc - startCol))

                // Only consider this neighbor if a cheaper path to it is found.
                if (newG < costSoFar.getOrDefault(nr to nc, Int.MAX_VALUE)) {
                    costSoFar[nr to nc] = newG
                    queue.add(Node(nr, nc, newG, newH))
                }
            }
        }
    }
    // Return null if no suitable free region is found.
    return null
}

/**
 * Checks if a rectangular region in the grid is completely free (i.e., none of its cells are occupied).
 *
 * The region is defined by the top‑left corner ([startRow], [startCol]) and extends for [reqRows] rows
 * and [reqCols] columns. If the region exceeds grid boundaries or if any cell in the region is already
 * occupied (true), the region is not free.
 *
 * @param startRow The top‑left row index of the region.
 * @param startCol The top‑left column index of the region.
 * @param reqRows The number of rows in the region.
 * @param reqCols The number of columns in the region.
 * @param grid The grid represented as a 2D boolean array.
 * @return `true` if the entire region is free; `false` otherwise.
 */
private fun isRegionFree(
    startRow: Int, startCol: Int, reqRows: Int, reqCols: Int, grid: Array<BooleanArray>
): Boolean {
    // Ensure the region fits within the grid.
    if (startRow + reqRows > grid.size || startCol + reqCols > grid[0].size) return false

    // Check each cell in the region.
    for (r in startRow until startRow + reqRows) {
        for (c in startCol until startCol + reqCols) {
            if (grid[r][c]) return false
        }
    }
    return true
}

/**
 * Generates a list of [GridCell] objects representing a rectangular region starting at the specified cell.
 *
 * The region begins at ([startRow], [startCol]) and spans [rows] rows and [cols] columns.
 *
 * @param startRow The starting row index of the region.
 * @param startCol The starting column index of the region.
 * @param rows The number of rows in the region.
 * @param cols The number of columns in the region.
 * @return A list of [GridCell] objects covering the specified rectangular region.
 */
private fun generateRegion(
    startRow: Int, startCol: Int, rows: Int, cols: Int
): List<GridCell> {
    return (startRow until startRow + rows).flatMap { r ->
        (startCol until startCol + cols).map { c -> GridCell(r, c) }
    }
}
