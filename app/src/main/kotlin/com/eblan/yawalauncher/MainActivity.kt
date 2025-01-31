package com.eblan.yawalauncher


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.eblan.yawalauncher.ui.theme.YawaLauncherTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YawaLauncherTheme {
                Scaffold { innerPadding ->
                    Greeting(
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .fillMaxSize(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Greeting(
    modifier: Modifier = Modifier
) {
    val items = remember {
        mutableStateListOf(
            GridItem(
                cells = listOf(
                    GridCell(0, 0),
                )
            ),
        )
    }

    var isDragging by remember { mutableStateOf(false) }

    var dragOffsetX by remember { mutableIntStateOf(-1) }

    var dragOffsetY by remember { mutableIntStateOf(-1) }

    var gridIntSize by remember { mutableStateOf(IntSize.Zero) }

    var selectedIndex by remember { mutableIntStateOf(-1) }

    val pagerState = rememberPagerState(pageCount = {
        10
    })

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        HorizontalPager(state = pagerState) { page ->
            Grid(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        gridIntSize = it
                    },
            ) {
                items.forEachIndexed { index, item ->
                    Text(text = "Hello $page", modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(onLongPress = {
                                isDragging = true
                                selectedIndex = index
                            })
                        }
                        .fillMaxSize()
                        .background(Color.Blue)
                        .gridCells(item.cells))
                }
            }
        }

        if (isDragging) {
            val boundingBox = calculateBoundingBox(
                gridCells = items[selectedIndex].cells,
                gridWidth = 4,
                gridHeight = 4,
                screenWidth = gridIntSize.width,
                screenHeight = gridIntSize.height
            )

            val coordinates = calculateCoordinates(
                gridCells = items[selectedIndex].cells,
                gridWidth = 4,
                gridHeight = 4,
                screenWidth = gridIntSize.width,
                screenHeight = gridIntSize.height
            )

            dragOffsetX = coordinates.x

            dragOffsetY = coordinates.y

            val density = LocalDensity.current

            val boundingBoxWidth = with(density) {
                boundingBox.width.toDp()
            }

            val boundingBoxHeight = with(density) {
                boundingBox.height.toDp()
            }

            var width by remember { mutableStateOf(boundingBoxWidth) }

            var height by remember { mutableStateOf(boundingBoxHeight) }

            val coroutineScope = rememberCoroutineScope()

            Box(modifier = Modifier
                .offset {
                    IntOffset(
                        x = dragOffsetX, y = dragOffsetY
                    )
                }
                .size(width = width, height = height)
                .background(Color.Green)
                .pointerInput(Unit) {
                    detectDragGestures(onDragEnd = {
                        moveGridItemWithCoordinates(
                            items = items,
                            itemIndex = selectedIndex,
                            x = dragOffsetX,
                            y = dragOffsetY,
                            gridWidth = 4,
                            gridHeight = 4,
                            screenWidth = gridIntSize.width,
                            screenHeight = gridIntSize.height,
                            boundingBoxWidth = boundingBox.width,
                            boundingBoxHeight = boundingBox.height,
                        )

                        dragOffsetX = -1
                        dragOffsetY = -1
                        isDragging = false
                    }, onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x.roundToInt()
                        dragOffsetY += dragAmount.y.roundToInt()

                        val edgeState = isAtScreenEdge(
                            x = dragOffsetX,
                            y = dragOffsetY,
                            boundingBoxWidth = boundingBox.width,
                            boundingBoxHeight = boundingBox.height,
                            screenWidth = gridIntSize.width,
                            screenHeight = gridIntSize.height,
                            margin = 0
                        )

                        if (edgeState.touchesRight) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }

                        if (edgeState.touchesLeft) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    })
                }) {
                Text(text = "Drag")

                Box(modifier = Modifier
                    .size(40.dp)
                    .background(Color.Gray)
                    .align(Alignment.BottomEnd)
                    .pointerInput(Unit) {
                        detectDragGestures(onDragEnd = {
                            val newPixelWidth = with(density) {
                                width.toPx()
                            }

                            val newPixelHeight = with(density) {
                                height.toPx()
                            }

                            resizeGridItemWithPixels(
                                items = items,
                                itemIndex = selectedIndex,
                                newPixelWidth = newPixelWidth.roundToInt(),
                                newPixelHeight = newPixelHeight.roundToInt(),
                                gridCellPixelWidth = gridIntSize.width / 4,
                                gridCellPixelHeight = gridIntSize.height / 4,
                            )

                            isDragging = false
                        }, onDrag = { change, dragAmount ->
                            change.consume()
                            width += dragAmount.x.toDp()
                            height += dragAmount.y.toDp()
                        })
                    })
            }
        }
    }
}

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = {
        content()
    }, modifier = modifier) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            val gridCells = measurable.parentData as GridCellsParentData

            val boundingBox = calculateBoundingBox(
                gridCells = gridCells.cells,
                gridWidth = 4,
                gridHeight = 4,
                screenWidth = constraints.maxWidth,
                screenHeight = constraints.maxHeight
            )

            measurable.measure(
                Constraints(
                    maxWidth = boundingBox.width, maxHeight = boundingBox.height
                )
            )
        }

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            placeables.forEach { placeable ->
                val gridCells = placeable.parentData as GridCellsParentData

                val coordinates = calculateCoordinates(
                    gridCells = gridCells.cells,
                    gridWidth = 4,
                    gridHeight = 4,
                    screenWidth = constraints.maxWidth,
                    screenHeight = constraints.maxHeight
                )

                placeable.placeRelative(x = coordinates.x, y = coordinates.y)
            }
        }
    }
}

data class GridItem(
    val selected: Boolean = false, val cells: List<GridCell> // List of grid cells the item occupies
)

data class GridCell(val row: Int, val column: Int)

// Data class representing the bounding box of an item
data class BoundingBox(
    val width: Int,  // Total width of the bounding box
    val height: Int  // Total height of the bounding box
)

// Function to calculate the bounding box for a list of grid cells
fun calculateBoundingBox(
    gridCells: List<GridCell>, // List of grid cells the item occupies
    gridWidth: Int,            // Number of columns in the grid
    gridHeight: Int,           // Number of rows in the grid
    screenWidth: Int,          // Total width of the screen
    screenHeight: Int          // Total height of the screen
): BoundingBox {
    // Calculate cell dimensions
    val cellWidth = screenWidth / gridWidth
    val cellHeight = screenHeight / gridHeight

    // Find the minimum and maximum row and column indices
    val minRow = gridCells.minOf { it.row }
    val maxRow = gridCells.maxOf { it.row }
    val minCol = gridCells.minOf { it.column }
    val maxCol = gridCells.maxOf { it.column }

    // Calculate the total width and height
    val width = (maxCol - minCol + 1) * cellWidth
    val height = (maxRow - minRow + 1) * cellHeight

    return BoundingBox(width, height)
}

data class Coordinates(val x: Int, val y: Int)

fun calculateCoordinates(
    gridCells: List<GridCell>, // List of grid cells the item occupies
    gridWidth: Int,            // Number of columns in the grid
    gridHeight: Int,           // Number of rows in the grid
    screenWidth: Int,          // Total width of the screen
    screenHeight: Int
): Coordinates {
    // Calculate cell dimensions
    val cellWidth = screenWidth / gridWidth
    val cellHeight = screenHeight / gridHeight

    // Find the minimum and maximum row and column indices
    val minRow = gridCells.minOf { it.row }
    val minCol = gridCells.minOf { it.column }

    // Calculate the top-left corner (x, y)
    val x = minCol * cellWidth
    val y = minRow * cellHeight

    return Coordinates(x, y)
}

data class GridCellsParentData(val cells: List<GridCell>)

fun Modifier.gridCells(cells: List<GridCell>): Modifier = then(object : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any {
        return GridCellsParentData(cells = cells)
    }
})

fun isOverlapping(newCells: List<GridCell>, items: List<GridItem>, excludeIndex: Int): Boolean {
    for (i in items.indices) {
        if (i == excludeIndex) continue // Skip the item being moved
        if (items[i].cells.any { it in newCells }) {
            return true // Overlapping cells found
        }
    }
    return false // No overlapping cells
}

fun pixelsToGridCells(
    newPixelWidth: Int, // New width in pixels
    newPixelHeight: Int, // New height in pixels
    gridCellPixelWidth: Int, // Width of a single grid cell in pixels
    gridCellPixelHeight: Int // Height of a single grid cell in pixels
): Pair<Int, Int> {
    val newWidth = (newPixelWidth / gridCellPixelWidth).coerceAtLeast(1) // Ensure at least 1 cell
    val newHeight =
        (newPixelHeight / gridCellPixelHeight).coerceAtLeast(1) // Ensure at least 1 cell
    return Pair(newWidth, newHeight)
}

fun resizeGridItemWithPixels(
    items: MutableList<GridItem>, // The list of grid items
    itemIndex: Int, // The index of the item to resize
    newPixelWidth: Int, // New width in pixels
    newPixelHeight: Int, // New height in pixels
    gridCellPixelWidth: Int, // Width of a single grid cell in pixels
    gridCellPixelHeight: Int, // Height of a single grid cell in pixels
): Boolean {
    // Convert pixel values to grid cells
    val (newWidth, newHeight) = pixelsToGridCells(
        newPixelWidth = newPixelWidth,
        newPixelHeight = newPixelHeight,
        gridCellPixelWidth = gridCellPixelWidth,
        gridCellPixelHeight = gridCellPixelHeight
    )

    // Resize the grid item
    return resizeGridItem(
        items = items, itemIndex = itemIndex, newWidth = newWidth, newHeight = newHeight
    )
}

fun calculateResizedCells(
    oldCells: List<GridCell>, // Current cells of the grid item
    newWidth: Int, // New width in terms of grid cells
    newHeight: Int // New height in terms of grid cells
): List<GridCell> {
    // Find the top-left cell of the grid item
    val topLeftCell = oldCells.minWith(compareBy({ it.row }, { it.column }))

    // Calculate the new cells
    val newCells = mutableListOf<GridCell>()
    for (row in topLeftCell.row until topLeftCell.row + newHeight) {
        for (col in topLeftCell.column until topLeftCell.column + newWidth) {
            newCells.add(GridCell(row, col))
        }
    }
    return newCells
}

fun resizeGridItem(
    items: MutableList<GridItem>, // The list of grid items
    itemIndex: Int, // The index of the item to resize
    newWidth: Int, // New width in terms of grid cells
    newHeight: Int, // New height in terms of grid cells
): Boolean {
    val item = items[itemIndex]
    val newCells = calculateResizedCells(item.cells, newWidth, newHeight)

    // Check if the new cells overlap with other items
    if (isOverlapping(newCells, items, itemIndex)) {
        return false // Cannot resize: overlapping cells
    }

    // Update the grid item with the new cells
    items[itemIndex] = item.copy(cells = newCells)
    return true // Successfully resized
}

fun calculateNewCells(oldCells: List<GridCell>, targetCell: GridCell): List<GridCell> {
    val rowOffset = targetCell.row - oldCells[0].row
    val colOffset = targetCell.column - oldCells[0].column
    return oldCells.map { cell ->
        GridCell(cell.row + rowOffset, cell.column + colOffset)
    }
}

fun moveGridItem(
    items: MutableList<GridItem>, // The list of grid items
    itemIndex: Int, // The index of the item to move
    targetCell: GridCell // The target grid cell
): Boolean {
    val item = items[itemIndex]
    val newCells = calculateNewCells(item.cells, targetCell)

    // Check if the new cells overlap with other items
    if (isOverlapping(newCells, items, itemIndex)) {
        return false // Cannot move: overlapping cells
    }

    // Update the grid item with the new cells
    items[itemIndex] = item.copy(cells = newCells)
    return true // Successfully moved
}

fun moveGridItemWithCoordinates(
    items: MutableList<GridItem>, // The list of grid items
    itemIndex: Int, // The index of the item to move
    x: Int, // X-coordinate in pixels
    y: Int, // Y-coordinate in pixels
    gridWidth: Int, // Number of columns in the grid
    gridHeight: Int, // Number of rows in the grid
    screenWidth: Int, // Total width of the screen
    screenHeight: Int, // Total height of the screen
    boundingBoxWidth: Int, // Width of the item in pixels
    boundingBoxHeight: Int,
): Boolean {
    // Convert coordinates to grid cell
    val targetCell = coordinatesToGridCell(
        x = x,
        y = y,
        gridWidth = gridWidth,
        gridHeight = gridHeight,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        boundingBoxWidth = boundingBoxWidth,
        boundingBoxHeight = boundingBoxHeight
    )

    // Move the grid item to the target cell
    return moveGridItem(items, itemIndex, targetCell)
}

fun coordinatesToGridCell(
    x: Int, // X-coordinate in pixels
    y: Int, // Y-coordinate in pixels
    gridWidth: Int, // Number of columns in the grid
    gridHeight: Int, // Number of rows in the grid
    screenWidth: Int, // Total width of the screen
    screenHeight: Int, // Total height of the screen
    boundingBoxWidth: Int, // Width of the item in pixels
    boundingBoxHeight: Int // Height of the item in pixels
): GridCell {
    val cellWidth = screenWidth / gridWidth
    val cellHeight = screenHeight / gridHeight

    val centerX = x + boundingBoxWidth / 2
    val centerY = y + boundingBoxHeight / 2

    val row = (centerY / cellHeight).coerceIn(0 until gridHeight)
    val column = (centerX / cellWidth).coerceIn(0 until gridWidth)

    return GridCell(row, column)
}

fun isAtScreenEdge(
    x: Int, y: Int, // Top-left of item (pixels)
    boundingBoxWidth: Int, boundingBoxHeight: Int, // Item size (pixels)
    screenWidth: Int, screenHeight: Int, // Screen size
    margin: Int = 0 // Edge tolerance (default = 0)
): EdgeState {
    val touchesTop = y <= margin
    val touchesBottom = (y + boundingBoxHeight) >= (screenHeight - margin)
    val touchesLeft = x <= margin
    val touchesRight = (x + boundingBoxWidth) >= (screenWidth - margin)

    return EdgeState(touchesTop, touchesBottom, touchesLeft, touchesRight)
}

// Data class to represent edge states
data class EdgeState(
    val touchesTop: Boolean,
    val touchesBottom: Boolean,
    val touchesLeft: Boolean,
    val touchesRight: Boolean
)

@Preview
@Composable
fun GridPreview() {
    Greeting(modifier = Modifier.fillMaxSize())
}
