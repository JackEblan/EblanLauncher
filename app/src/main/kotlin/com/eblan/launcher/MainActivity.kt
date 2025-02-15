package com.eblan.launcher


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.BoundingBox
import com.eblan.launcher.domain.model.Coordinates
import com.eblan.launcher.domain.model.EdgeState
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemPixel
import com.eblan.launcher.ui.theme.EblanLauncherTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val mainActivityViewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EblanLauncherTheme {
                Scaffold { innerPadding ->
                    Greeting(
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .fillMaxSize(),
                        viewModel = mainActivityViewModel,
                        onUpdateScreenDimension = mainActivityViewModel::updateScreenDimension,
                        onMoveGridItem = mainActivityViewModel::moveGridItem,
                        onResizeGridItem = mainActivityViewModel::resizeGridItem,
                        onAddGridItem = mainActivityViewModel::addGridItem
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel,
    onUpdateScreenDimension: (screenWidth: Int, screenHeight: Int) -> Unit,
    onMoveGridItem: (
        page: Int, x: Int, y: Int, screenWidth: Int, screenHeight: Int, gridItemPixel: GridItemPixel?
    ) -> Unit,
    onResizeGridItem: (
        page: Int, newPixelWidth: Int, newPixelHeight: Int, screenWidth: Int, screenHeight: Int, gridItem: GridItem?, anchor: Anchor,
    ) -> Unit,
    onAddGridItem: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
) {
    val gridItems by viewModel.gridItems.collectAsStateWithLifecycle()

    val currentGridItem by viewModel.currentGridItem.collectAsStateWithLifecycle()

    var isEditing by remember { mutableStateOf(false) }

    var dragOffsetX by remember { mutableIntStateOf(-1) }

    var dragOffsetY by remember { mutableIntStateOf(-1) }

    var gridIntSize by remember { mutableStateOf(IntSize.Zero) }

    var selectedGridItemIntSize by remember { mutableStateOf(IntSize.Zero) }

    val pagerState = rememberPagerState(pageCount = {
        10
    })

    var selectedGridItemPixel by remember { mutableStateOf<GridItemPixel?>(null) }

    LaunchedEffect(key1 = currentGridItem) {
        when (currentGridItem?.edgeState) {
            EdgeState.Left -> {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }

            EdgeState.Right -> {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }

            EdgeState.None, null -> Unit
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        HorizontalPager(state = pagerState) { page ->
            Grid(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            onAddGridItem(
                                pagerState.currentPage,
                                it.x.roundToInt(),
                                it.y.roundToInt(),
                                gridIntSize.width,
                                gridIntSize.height
                            )
                        })
                    }
                    .onSizeChanged {
                        gridIntSize = it
                        onUpdateScreenDimension(
                            it.width,
                            it.height,
                        )
                    },
            ) {
                gridItems[page]?.forEach { gridItemPixel ->
                    Text(text = "Hello ${gridItemPixel.gridItem.id}",
                         modifier = Modifier
                             .fillMaxSize()
                             .pointerInput(key1 = gridItemPixel) {
                                 detectTapGestures(onLongPress = {
                                     isEditing = true
                                     selectedGridItemPixel = gridItemPixel
                                     selectedGridItemIntSize = IntSize(
                                         width = gridItemPixel.boundingBox.width,
                                         height = gridItemPixel.boundingBox.height
                                     )
                                     dragOffsetX = gridItemPixel.coordinates.x
                                     dragOffsetY = gridItemPixel.coordinates.y
                                 })
                             }
                             .background(Color.Blue)
                             .gridItemPlacement(gridItemPixel))
                }
            }
        }

        if (isEditing) {
            val density = LocalDensity.current

            val boundingBoxWidth = with(density) {
                selectedGridItemIntSize.width.toDp()
            }

            val boundingBoxHeight = with(density) {
                selectedGridItemIntSize.height.toDp()
            }

            var width by remember { mutableStateOf(boundingBoxWidth) }

            var height by remember { mutableStateOf(boundingBoxHeight) }

            val pixelWidth = with(density) {
                width.toPx()
            }.roundToInt()

            val pixelHeight = with(density) {
                height.toPx()
            }.roundToInt()

            val resizeBoundingBox = calculateResizeBoundingBox(
                coordinates = Coordinates(
                    x = dragOffsetX, y = dragOffsetY,
                ), boundingBox = BoundingBox(
                    width = pixelWidth, height = pixelHeight
                )
            )

            val resizeBoundingBoxWidth = with(density) {
                resizeBoundingBox.width.toDp()
            }

            val resizeBoundingBoxHeight = with(density) {
                resizeBoundingBox.height.toDp()
            }

            var menuIntSize by remember { mutableStateOf(IntSize.Zero) }

            val menuSizeMargin = with(density) {
                10.dp.toPx()
            }.roundToInt()

            val menuCoordinates = calculateMenuCoordinates(
                parentX = resizeBoundingBox.x,
                parentY = resizeBoundingBox.y,
                parentWidth = resizeBoundingBox.width,
                parentHeight = resizeBoundingBox.height,
                childWidth = menuIntSize.width,
                childHeight = menuIntSize.height,
                screenWidth = gridIntSize.width,
                screenHeight = gridIntSize.height,
                margin = menuSizeMargin,
            )

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
                        isEditing = false
                    }, onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x.roundToInt()
                        dragOffsetY += dragAmount.y.roundToInt()

                        onMoveGridItem(
                            pagerState.currentPage,
                            dragOffsetX,
                            dragOffsetY,
                            gridIntSize.width,
                            gridIntSize.height,
                            selectedGridItemPixel
                        )
                    })
                }) {
                Text(text = "Drag")
            }

            Box(modifier = Modifier
                .offset {
                    IntOffset(x = resizeBoundingBox.x, y = resizeBoundingBox.y)
                }
                .size(
                    width = resizeBoundingBoxWidth, height = resizeBoundingBoxHeight
                )
                .border(width = 2.dp, color = Color.White)) {

                Box(modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.TopStart)
                    .offset((-15).dp, (-15).dp) // Negative offset moves it outside
                    .background(Color.White, shape = CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures(onDragEnd = {
                            isEditing = false
                        }, onDrag = { change, dragAmount ->
                            change.consume()
                            width += -dragAmount.x.toDp()
                            height += -dragAmount.y.toDp()

                            dragOffsetX += dragAmount.x.roundToInt()
                            dragOffsetY += dragAmount.y.roundToInt()

                            val newPixelWidth = with(density) {
                                width.toPx()
                            }

                            val newPixelHeight = with(density) {
                                height.toPx()
                            }

                            onResizeGridItem(
                                pagerState.currentPage,
                                newPixelWidth.roundToInt(),
                                newPixelHeight.roundToInt(),
                                gridIntSize.width,
                                gridIntSize.height,
                                selectedGridItemPixel?.gridItem,
                                Anchor.BOTTOM_END,
                            )
                        })
                    })

                Box(modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .offset(15.dp, (-15).dp)
                    .background(Color.White, shape = CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures(onDragEnd = {
                            isEditing = false
                        }, onDrag = { change, dragAmount ->
                            change.consume()
                            width += dragAmount.x.toDp()
                            height += -dragAmount.y.toDp()

                            dragOffsetY += dragAmount.y.roundToInt()

                            val newPixelWidth = with(density) {
                                width.toPx()
                            }

                            val newPixelHeight = with(density) {
                                height.toPx()
                            }

                            onResizeGridItem(
                                pagerState.currentPage,
                                newPixelWidth.roundToInt(),
                                newPixelHeight.roundToInt(),
                                gridIntSize.width,
                                gridIntSize.height,
                                selectedGridItemPixel?.gridItem,
                                Anchor.BOTTOM_START,
                            )
                        })
                    })

                Box(modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.BottomStart)
                    .offset((-15).dp, 15.dp)
                    .background(Color.White, shape = CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures(onDragEnd = {
                            isEditing = false
                        }, onDrag = { change, dragAmount ->
                            change.consume()
                            width += -dragAmount.x.toDp()
                            height += dragAmount.y.toDp()

                            dragOffsetX += dragAmount.x.roundToInt()

                            val newPixelWidth = with(density) {
                                width.toPx()
                            }

                            val newPixelHeight = with(density) {
                                height.toPx()
                            }

                            onResizeGridItem(
                                pagerState.currentPage,
                                newPixelWidth.roundToInt(),
                                newPixelHeight.roundToInt(),
                                gridIntSize.width,
                                gridIntSize.height,
                                selectedGridItemPixel?.gridItem,
                                Anchor.TOP_END,
                            )
                        })
                    })

                Box(modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.BottomEnd)
                    .offset(15.dp, 15.dp)
                    .background(Color.White, shape = CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures(onDragEnd = {
                            isEditing = false
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

                            onResizeGridItem(
                                pagerState.currentPage,
                                newPixelWidth.roundToInt(),
                                newPixelHeight.roundToInt(),
                                gridIntSize.width,
                                gridIntSize.height,
                                selectedGridItemPixel?.gridItem,
                                Anchor.TOP_START,
                            )
                        })
                    })
            }

            Box(modifier = Modifier
                .offset {
                    IntOffset(x = menuCoordinates.x, y = menuCoordinates.y)
                }
                .background(Color.Gray)
                .onSizeChanged {
                    menuIntSize = it
                }) {
                Text(text = "Menu here")
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
            val gridItemPlacement = measurable.parentData as GridItemPlacementParentData

            measurable.measure(
                Constraints(
                    maxWidth = gridItemPlacement.boundingBox.width,
                    maxHeight = gridItemPlacement.boundingBox.height
                )
            )
        }

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            placeables.forEach { placeable ->
                val gridItemPlacement = placeable.parentData as GridItemPlacementParentData

                placeable.placeRelative(
                    x = gridItemPlacement.coordinates.x, y = gridItemPlacement.coordinates.y
                )
            }
        }
    }
}

data class GridItemPlacementParentData(
    val boundingBox: BoundingBox, val coordinates: Coordinates
)

fun Modifier.gridItemPlacement(gridItemPixel: GridItemPixel): Modifier =
    then(object : ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?): Any {
            return GridItemPlacementParentData(
                boundingBox = gridItemPixel.boundingBox, coordinates = gridItemPixel.coordinates
            )
        }
    })

data class ResizeBoundingBox(val x: Int, val y: Int, val width: Int, val height: Int)
data class MenuCoordinates(val x: Int, val y: Int)

fun calculateResizeBoundingBox(
    coordinates: Coordinates, boundingBox: BoundingBox, margin: Int = 100
): ResizeBoundingBox {
    val newWidth = (boundingBox.width + margin).coerceAtLeast(margin)
    val newHeight = (boundingBox.height + margin).coerceAtLeast(margin)

    val centerX = coordinates.x + boundingBox.width / 2
    val centerY = coordinates.y + boundingBox.height / 2

    val newX = centerX - newWidth / 2
    val newY = centerY - newHeight / 2

    return ResizeBoundingBox(x = newX, y = newY, width = newWidth, height = newHeight)
}

fun calculateMenuCoordinates(
    parentX: Int,
    parentY: Int,       // Parent's top coordinate
    parentWidth: Int,
    parentHeight: Int,
    childWidth: Int,
    childHeight: Int,
    screenWidth: Int,   // Screen width
    screenHeight: Int,  // Screen height
    margin: Int         // Margin between parent and child
): MenuCoordinates {
    // Calculate the parent's horizontal center
    val parentCenterX = parentX + parentWidth / 2

    // Compute the initial x-position so the child's center aligns with the parent's center
    val childXInitial = parentCenterX - childWidth / 2

    // Coerce the child's x-position to ensure it doesn't go off the screen horizontally.
    val childX = childXInitial.coerceIn(0, screenWidth - childWidth)

    // Determine the vertical position:
    // Calculate the y-coordinate if the child is placed above the parent.
    // With margin: child's bottom = parent's top - margin, so child's top = parent's top - margin - childHeight
    val topPositionY = parentY - margin - childHeight

    // Calculate the y-coordinate if the child is placed below the parent.
    // With margin: child's top = parent's bottom + margin
    val bottomPositionY = parentY + parentHeight + margin

    // Decide where to place the child: use top if it fits (child not off-screen), else use bottom.
    val childYInitial = if (topPositionY < 0) bottomPositionY else topPositionY

    // Ensure the child's vertical position stays within screen bounds.
    val childY = childYInitial.coerceIn(0, screenHeight - childHeight)

    return MenuCoordinates(x = childX, y = childY)
}
