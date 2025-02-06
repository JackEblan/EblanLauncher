package com.eblan.launcher


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.eblan.launcher.ui.theme.EblanLauncherTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val mainActivityViewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var gridItems by mutableStateOf(emptyMap<Int, List<GridItem>>())

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivityViewModel.gridItems.collect {
                    gridItems = it
                }
            }
        }

        setContent {
            EblanLauncherTheme {
                Scaffold { innerPadding ->
                    Greeting(
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .fillMaxSize(),
                        gridItems = gridItems,
                        onUpdateGridItem = mainActivityViewModel::updateGridItem,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    gridItems: Map<Int, List<GridItem>>,
    onUpdateGridItem: (page: Int, gridItem: GridItem) -> Unit,
) {
    var isDragging by remember { mutableStateOf(false) }

    var dragOffsetX by remember { mutableIntStateOf(-1) }

    var dragOffsetY by remember { mutableIntStateOf(-1) }

    var gridIntSize by remember { mutableStateOf(IntSize.Zero) }

    var selectedGridItemIntSize by remember { mutableStateOf(IntSize.Zero) }

    val pagerState = rememberPagerState(pageCount = {
        10
    })

    var selectedGridItem by remember { mutableStateOf<GridItem?>(null) }

    var updatedGridItem by remember { mutableStateOf<GridItem?>(null) }

    var edgeState by remember { mutableStateOf<EdgeState?>(null) }

    LaunchedEffect(key1 = true) {
        snapshotFlow { updatedGridItem }.filterNotNull().distinctUntilChanged()
            .collect { newGridItem ->
                onUpdateGridItem(pagerState.currentPage, newGridItem)
            }
    }

    LaunchedEffect(key1 = true) {
        snapshotFlow { edgeState }.filterNotNull().debounce(2000).distinctUntilChanged()
            .collect { newEdgeState ->
                when (newEdgeState) {
                    EdgeState.Left -> {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }

                    EdgeState.Right -> {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }

                    EdgeState.None -> Unit
                }
            }
    }

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
                gridItems[page]?.forEach { gridItem ->
                    var gridItemIntSize by remember { mutableStateOf(IntSize.Zero) }
                    var gridItemOffsetX by remember { mutableIntStateOf(-1) }
                    var gridItemOffsetY by remember { mutableIntStateOf(-1) }

                    Text(text = "Hello ${gridItem.id}",
                         modifier = Modifier
                             .pointerInput(key1 = gridItem, key2 = isDragging) {
                                 detectTapGestures(onLongPress = {
                                     if (isDragging.not()) {
                                         isDragging = true
                                         selectedGridItem = gridItem
                                         selectedGridItemIntSize = gridItemIntSize
                                         dragOffsetX = gridItemOffsetX
                                         dragOffsetY = gridItemOffsetY
                                     }
                                 })
                             }
                             .fillMaxSize()
                             .onSizeChanged {
                                 gridItemIntSize = it
                             }
                             .onGloballyPositioned {
                                 gridItemOffsetX = it.positionInParent().x.roundToInt()
                                 gridItemOffsetY = it.positionInParent().y.roundToInt()
                             }
                             .background(Color.Blue)
                             .gridCells(gridItem.cells))
                }
            }
        }

        if (isDragging) {
            val density = LocalDensity.current

            val boundingBoxWidth = with(density) {
                selectedGridItemIntSize.width.toDp()
            }

            val boundingBoxHeight = with(density) {
                selectedGridItemIntSize.height.toDp()
            }

            var width by remember { mutableStateOf(boundingBoxWidth) }

            var height by remember { mutableStateOf(boundingBoxHeight) }

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
                        isDragging = false
                    }, onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x.roundToInt()
                        dragOffsetY += dragAmount.y.roundToInt()

                        edgeState = isAtScreenEdge(
                            x = dragOffsetX,
                            boundingBoxWidth = selectedGridItemIntSize.width,
                            screenWidth = gridIntSize.width,
                            margin = 0
                        )

                        updatedGridItem = moveGridItemWithCoordinates(
                            gridItem = selectedGridItem,
                            x = dragOffsetX,
                            y = dragOffsetY,
                            gridWidth = 4,
                            gridHeight = 4,
                            screenWidth = gridIntSize.width,
                            screenHeight = gridIntSize.height,
                            boundingBoxWidth = selectedGridItemIntSize.width,
                            boundingBoxHeight = selectedGridItemIntSize.height
                        )
                    })
                }) {
                Text(text = "Drag")

                Box(modifier = Modifier
                    .size(40.dp)
                    .background(Color.Gray)
                    .align(Alignment.BottomEnd)
                    .pointerInput(Unit) {
                        detectDragGestures(onDragEnd = {
                            isDragging = false
                        }, onDrag = { change, dragAmount ->
                            change.consume()
                            width += dragAmount.x.toDp()
                            height += dragAmount.y.toDp()

                            val newPixelWidth = with(density) {
                                width.toPx()
                            }

                            val newPixelHeight = with(density) {
                                height.toPx()
                            }

                            updatedGridItem = resizeGridItemWithPixels(
                                gridItem = selectedGridItem,
                                newPixelWidth = newPixelWidth.roundToInt(),
                                newPixelHeight = newPixelHeight.roundToInt(),
                                gridCellPixelWidth = gridIntSize.width / 4,
                                gridCellPixelHeight = gridIntSize.height / 4
                            )
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
    val page: Int, val id: Int, val cells: List<GridCell> // List of grid cells the item occupies
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
    gridItem: GridItem?, // The list of grid items
    newPixelWidth: Int, // New width in pixels
    newPixelHeight: Int, // New height in pixels
    gridCellPixelWidth: Int, // Width of a single grid cell in pixels
    gridCellPixelHeight: Int, // Height of a single grid cell in pixels
): GridItem? {
    val (newWidth, newHeight) = pixelsToGridCells(
        newPixelWidth = newPixelWidth,
        newPixelHeight = newPixelHeight,
        gridCellPixelWidth = gridCellPixelWidth,
        gridCellPixelHeight = gridCellPixelHeight
    )

    return resizeGridItem(
        gridItem = gridItem,
        newWidth = newWidth,
        newHeight = newHeight,
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
    gridItem: GridItem?, // The list of grid items
    newWidth: Int, // New width in terms of grid cells
    newHeight: Int, // New height in terms of grid
): GridItem? {
    return if (gridItem != null) {
        val newCells = calculateResizedCells(gridItem.cells, newWidth, newHeight)
        gridItem.copy(cells = newCells)
    } else {
        null
    }
}

fun calculateNewCells(oldCells: List<GridCell>, targetCell: GridCell): List<GridCell> {
    val rowOffset = targetCell.row - oldCells[0].row
    val colOffset = targetCell.column - oldCells[0].column
    return oldCells.map { cell ->
        GridCell(cell.row + rowOffset, cell.column + colOffset)
    }
}

fun moveGridItem(
    gridItem: GridItem?,
    targetCell: GridCell,
): GridItem? {
    return if (gridItem != null) {
        val newCells = calculateNewCells(gridItem.cells, targetCell)
        gridItem.copy(cells = newCells)
    } else {
        null
    }
}

fun moveGridItemWithCoordinates(
    gridItem: GridItem?,
    x: Int, // X-coordinate in pixels
    y: Int, // Y-coordinate in pixels
    gridWidth: Int, // Number of columns in the grid
    gridHeight: Int, // Number of rows in the grid
    screenWidth: Int, // Total width of the screen
    screenHeight: Int, // Total height of the screen
    boundingBoxWidth: Int, // Width of the item in pixels
    boundingBoxHeight: Int,
): GridItem? {
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

    return moveGridItem(
        gridItem = gridItem, targetCell = targetCell
    )
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
    x: Int, boundingBoxWidth: Int, screenWidth: Int, margin: Int = 0
): EdgeState {
    val touchesLeft = x <= margin
    val touchesRight = (x + boundingBoxWidth) >= (screenWidth - margin)

    return when {
        touchesLeft -> EdgeState.Left
        touchesRight -> EdgeState.Right
        else -> EdgeState.None
    }
}

enum class EdgeState {
    Left, Right, None
}