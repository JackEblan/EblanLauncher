package com.eblan.launcher.domain.grid

import com.eblan.launcher.GridCell
import com.eblan.launcher.GridItem
import java.util.PriorityQueue

fun gridAlgorithmUsingAStar(
    page: Int,
    gridItems: List<GridItem>,
    movingGridItem: GridItem,
    gridRows: Int,
    gridCols: Int,
): List<GridItem> {
    val grid = Array(gridRows) { BooleanArray(gridCols) }
    val resolvedItems = mutableListOf<GridItem>()

    resolvedItems.add(movingGridItem)
    movingGridItem.cells.forEach { cell ->
        grid[cell.row][cell.column] = true
    }

    val samePageItems = gridItems.filter { it.page == page && it.id != movingGridItem.id }

    val (nonConflicting, conflicting) = samePageItems.partition { item ->
        item.cells.none { it in movingGridItem.cells }
    }

    nonConflicting.forEach { item ->
        item.cells.forEach { cell ->
            grid[cell.row][cell.column] = true
        }
        resolvedItems.add(item)
    }

    conflicting.forEach { item ->
        val (reqRows, reqCols) = getItemDimensions(item.cells)
        val startR = item.cells.first().row
        val startC = item.cells.first().column

        val newRegion = findAvailableRegion(grid, reqRows, reqCols, startR, startC)
        if (newRegion != null) {
            newRegion.forEach { cell ->
                grid[cell.row][cell.column] = true
            }
            resolvedItems.add(item.copy(cells = newRegion))
        } else {
            item.cells.forEach { cell ->
                grid[cell.row][cell.column] = true
            }
            resolvedItems.add(item)
        }
    }

    return resolvedItems + gridItems.filter { it.page != page }
}

private data class Node(val row: Int, val col: Int, val g: Int, val h: Int) {
    val f: Int get() = g + h
}

private fun findAvailableRegion(
    grid: Array<BooleanArray>, requiredRows: Int, requiredCols: Int, startRow: Int, startCol: Int
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