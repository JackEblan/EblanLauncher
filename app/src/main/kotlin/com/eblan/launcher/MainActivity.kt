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
import com.eblan.launcher.domain.model.BoundingBox
import com.eblan.launcher.domain.model.Coordinates
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemPixel
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

        var gridItems by mutableStateOf(emptyMap<Int, List<GridItemPixel>>())

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

@OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    gridItems: Map<Int, List<GridItemPixel>>,
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
    var isDragging by remember { mutableStateOf(false) }

    var dragOffsetX by remember { mutableIntStateOf(-1) }

    var dragOffsetY by remember { mutableIntStateOf(-1) }

    var gridIntSize by remember { mutableStateOf(IntSize.Zero) }

    var selectedGridItemIntSize by remember { mutableStateOf(IntSize.Zero) }

    val pagerState = rememberPagerState(pageCount = {
        10
    })

    var selectedGridItemPixel by remember { mutableStateOf<GridItemPixel?>(null) }

    var edgeState by remember { mutableStateOf<EdgeState?>(null) }

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
                    var gridItemIntSize by remember { mutableStateOf(IntSize.Zero) }
                    var gridItemOffsetX by remember { mutableIntStateOf(-1) }
                    var gridItemOffsetY by remember { mutableIntStateOf(-1) }

                    Text(text = "Hello ${gridItemPixel.gridItem.id}",
                         modifier = Modifier
                             .fillMaxSize()
                             .pointerInput(key1 = gridItemPixel, key2 = isDragging) {
                                 detectTapGestures(onLongPress = {
                                     if (isDragging.not()) {
                                         isDragging = true
                                         selectedGridItemPixel = gridItemPixel
                                         selectedGridItemIntSize = gridItemIntSize
                                         dragOffsetX = gridItemOffsetX
                                         dragOffsetY = gridItemOffsetY
                                     }
                                 })
                             }
                             .onSizeChanged {
                                 gridItemIntSize = it
                             }
                             .onGloballyPositioned {
                                 gridItemOffsetX = it.positionInParent().x.roundToInt()
                                 gridItemOffsetY = it.positionInParent().y.roundToInt()
                             }
                             .background(Color.Blue)
                             .gridItemPlacement(gridItemPixel))
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

                        edgeState = isGridItemOutOfBounds(
                            x = dragOffsetX,
                            boundingBoxWidth = selectedGridItemIntSize.width,
                            screenWidth = gridIntSize.width,
                            margin = 0
                        )

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

fun isGridItemOutOfBounds(
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