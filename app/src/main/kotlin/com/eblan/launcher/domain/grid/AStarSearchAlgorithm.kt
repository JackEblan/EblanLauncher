package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItem
import java.util.PriorityQueue
import kotlin.math.abs

/**
 * Resolves layout conflicts among grid items on a specified page when a grid item is moved.
 *
 * Updates the positions of grid items to prevent overlapping when the [movingGridItem]
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
 * For each node, it checks if a rectangular region of size [requiredRows] x [requiredCols] is free either up, down, left or right.
 * Upon finding a valid region, produce a list of [GridCell] objects
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
    grid: Array<BooleanArray>, requiredRows: Int, requiredCols: Int, startRow: Int, startCol: Int,
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
                val newH = abs(nr - startRow) + abs(nc - startCol)
                queue.add(Node(nr, nc, newG, newH))
            }
        }
    }
    return null
}