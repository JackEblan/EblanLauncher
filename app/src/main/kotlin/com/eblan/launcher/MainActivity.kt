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
        page: Int, newPixelWidth: Int, newPixelHeight: Int, screenWidth: Int, screenHeight: Int, gridItem: GridItem?
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

    val gridItemCornerSize = 40.dp

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
                             .pointerInput(key1 = gridItemPixel, key2 = isEditing) {
                                 detectTapGestures(onLongPress = {
                                     if (isEditing.not()) {
                                         isEditing = true
                                         selectedGridItemPixel = gridItemPixel
                                         selectedGridItemIntSize = IntSize(
                                             width = gridItemPixel.boundingBox.width,
                                             height = gridItemPixel.boundingBox.height
                                         )
                                         dragOffsetX = gridItemPixel.coordinates.x
                                         dragOffsetY = gridItemPixel.coordinates.y
                                     }
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

            //Bottom Right
            Box(modifier = Modifier
                .offset {
                    val gridItemCornerSizePixel = with(density) {
                        40.dp.toPx()
                    }.roundToInt()

                    val newPixelWidth = with(density) {
                        width.toPx()
                    }.roundToInt()

                    val newPixelHeight = with(density) {
                        height.toPx()
                    }.roundToInt()

                    // Check if the box is out of bound on the bottom right
                    val coercingOffsetX =
                        if ((dragOffsetX + newPixelWidth) + gridItemCornerSizePixel > gridIntSize.width) {
                            (dragOffsetX + newPixelWidth) - gridItemCornerSizePixel
                        } else {
                            dragOffsetX + newPixelWidth
                        }

                    val coercingOffsetY =
                        if ((dragOffsetY + newPixelHeight) + gridItemCornerSizePixel > gridIntSize.height) {
                            (dragOffsetY + newPixelHeight) - gridItemCornerSizePixel
                        } else {
                            dragOffsetY + newPixelHeight
                        }

                    IntOffset(
                        x = coercingOffsetX,
                        y = coercingOffsetY,
                    )
                }
                .size(40.dp)
                .background(Color.Gray)
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
                        )
                    })
                })

            // Bottom Left
            Box(modifier = Modifier
                .offset {
                    val gridItemCornerSizePixel = with(density) {
                        gridItemCornerSize.toPx()
                    }.roundToInt()

                    val newPixelHeight = with(density) {
                        height.toPx()
                    }.roundToInt()

                    // Check if the box is out of bound on the bottom left
                    val coercingOffsetX = if (dragOffsetX - gridItemCornerSizePixel < 0) {
                        dragOffsetX
                    } else {
                        dragOffsetX - gridItemCornerSizePixel
                    }

                    val coercingOffsetY =
                        if ((dragOffsetY + newPixelHeight) + gridItemCornerSizePixel > gridIntSize.height) {
                            (dragOffsetY + newPixelHeight) - gridItemCornerSizePixel
                        } else {
                            dragOffsetY + newPixelHeight
                        }

                    IntOffset(
                        x = coercingOffsetX,
                        y = coercingOffsetY,
                    )
                }
                .size(gridItemCornerSize)
                .background(Color.Gray))

            // Top Left
            Box(modifier = Modifier
                .offset {
                    val gridItemCornerSizePixel = with(density) {
                        gridItemCornerSize.toPx()
                    }.roundToInt()

                    // Check if the box is out of bound on the top left
                    val coercingOffsetX = if (dragOffsetX - gridItemCornerSizePixel < 0) {
                        dragOffsetX
                    } else {
                        dragOffsetX - gridItemCornerSizePixel
                    }

                    val coercingOffsetY = if (dragOffsetY - gridItemCornerSizePixel < 0) {
                        dragOffsetY
                    } else {
                        dragOffsetY - gridItemCornerSizePixel
                    }

                    IntOffset(
                        x = coercingOffsetX,
                        y = coercingOffsetY,
                    )
                }
                .size(gridItemCornerSize)
                .background(Color.Gray))

            // Top Right
            Box(modifier = Modifier
                .offset {
                    val gridItemCornerSizePixel = with(density) {
                        gridItemCornerSize.toPx()
                    }.roundToInt()

                    val newPixelWidth = with(density) {
                        width.toPx()
                    }.roundToInt()

                    // Check if the box is out of bound on the top right
                    val coercingOffsetX =
                        if ((dragOffsetX + newPixelWidth) + gridItemCornerSizePixel > gridIntSize.width) {
                            (dragOffsetX + newPixelWidth) - gridItemCornerSizePixel
                        } else {
                            dragOffsetX + newPixelWidth
                        }

                    val coercingOffsetY = if (dragOffsetY - gridItemCornerSizePixel < 0) {
                        dragOffsetY
                    } else {
                        dragOffsetY - gridItemCornerSizePixel
                    }

                    IntOffset(
                        x = coercingOffsetX,
                        y = coercingOffsetY,
                    )
                }
                .size(gridItemCornerSize)
                .background(Color.Gray))

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