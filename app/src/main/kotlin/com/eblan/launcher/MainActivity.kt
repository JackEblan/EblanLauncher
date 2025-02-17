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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.EdgeState
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemPixel
import com.eblan.launcher.feature.home.Grid
import com.eblan.launcher.feature.home.ResizableBoxWithMenu
import com.eblan.launcher.feature.home.gridItemPlacement
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
    onUpdateScreenDimension: (screenWidthPixel: Int, screenHeightPixel: Int) -> Unit,
    onMoveGridItem: (
        page: Int, x: Int, y: Int, screenWidthPixel: Int, screenHeightPixel: Int, gridItemPixel: GridItemPixel?
    ) -> Unit,
    onResizeGridItem: (
        page: Int, widthPixel: Int, heightPixel: Int, screenWidthPixel: Int, screenHeightPixel: Int, gridItem: GridItem?, anchor: Anchor,
    ) -> Unit,
    onAddGridItem: (
        page: Int,
        x: Int,
        y: Int,
        screenWidthPixel: Int,
        screenHeightPixel: Int,
    ) -> Unit,
) {
    val density = LocalDensity.current

    val gridItems by viewModel.gridItems.collectAsStateWithLifecycle()

    val updatedGridItem by viewModel.updatedGridItem.collectAsStateWithLifecycle()

    var isEditing by remember { mutableStateOf(false) }

    var dragOffsetX by remember { mutableIntStateOf(-1) }

    var dragOffsetY by remember { mutableIntStateOf(-1) }

    var gridIntSize by remember { mutableStateOf(IntSize.Zero) }

    var selectedGridItemIntSize by remember { mutableStateOf(IntSize.Zero) }

    val pagerState = rememberPagerState(pageCount = {
        2
    })

    var selectedGridItemPixel by remember { mutableStateOf<GridItemPixel?>(null) }

    LaunchedEffect(key1 = updatedGridItem) {
        when (updatedGridItem?.edgeState) {
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
                        detectTapGestures(onLongPress = { offset ->
                            onAddGridItem(
                                pagerState.currentPage,
                                offset.x.roundToInt(),
                                offset.y.roundToInt(),
                                gridIntSize.width,
                                gridIntSize.height
                            )
                        })
                    }
                    .onSizeChanged { intSize ->
                        gridIntSize = intSize
                        onUpdateScreenDimension(
                            intSize.width,
                            intSize.height,
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
            val boundingBoxWidthDp = with(density) {
                selectedGridItemIntSize.width.toDp()
            }

            val boundingBoxHeightDp = with(density) {
                selectedGridItemIntSize.height.toDp()
            }

            var widthDp by remember { mutableStateOf(boundingBoxWidthDp) }

            var heightDp by remember { mutableStateOf(boundingBoxHeightDp) }

            var widthPixel by remember { mutableIntStateOf(selectedGridItemIntSize.width) }

            var heightPixel by remember { mutableIntStateOf(selectedGridItemIntSize.height) }

            Box(modifier = Modifier
                .offset {
                    IntOffset(
                        x = dragOffsetX, y = dragOffsetY
                    )
                }
                .size(width = widthDp, height = heightDp)
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

            ResizableBoxWithMenu(
                x = dragOffsetX,
                y = dragOffsetY,
                width = widthPixel,
                height = heightPixel,
                screenWidth = gridIntSize.width,
                screenHeight = gridIntSize.height,
                onDragEnd = {
                    isEditing = false
                },
                onTopStartDrag = { change, dragAmount ->
                    change.consume()
                    val dragAmountXDp = with(density) {
                        dragAmount.x.toDp()
                    }

                    val dragAmountYDp = with(density) {
                        dragAmount.y.toDp()
                    }

                    widthDp += -dragAmountXDp
                    heightDp += -dragAmountYDp

                    dragOffsetX += dragAmount.x.roundToInt()
                    dragOffsetY += dragAmount.y.roundToInt()

                    widthPixel = with(density) {
                        widthDp.toPx()
                    }.roundToInt()

                    heightPixel = with(density) {
                        heightDp.toPx()
                    }.roundToInt()

                    onResizeGridItem(
                        pagerState.currentPage,
                        widthPixel,
                        heightPixel,
                        gridIntSize.width,
                        gridIntSize.height,
                        selectedGridItemPixel?.gridItem,
                        Anchor.BOTTOM_END,
                    )
                },
                onTopEndDrag = { change, dragAmount ->
                    change.consume()
                    val dragAmountXDp = with(density) {
                        dragAmount.x.toDp()
                    }

                    val dragAmountYDp = with(density) {
                        dragAmount.y.toDp()
                    }

                    widthDp += dragAmountXDp
                    heightDp += -dragAmountYDp

                    dragOffsetY += dragAmount.y.roundToInt()

                    widthPixel = with(density) {
                        widthDp.toPx()
                    }.roundToInt()

                    heightPixel = with(density) {
                        heightDp.toPx()
                    }.roundToInt()

                    onResizeGridItem(
                        pagerState.currentPage,
                        widthPixel,
                        heightPixel,
                        gridIntSize.width,
                        gridIntSize.height,
                        selectedGridItemPixel?.gridItem,
                        Anchor.BOTTOM_START,
                    )
                },
                onBottomStartDrag = { change, dragAmount ->
                    change.consume()
                    val dragAmountXDp = with(density) {
                        dragAmount.x.toDp()
                    }

                    val dragAmountYDp = with(density) {
                        dragAmount.y.toDp()
                    }

                    widthDp += -dragAmountXDp
                    heightDp += dragAmountYDp

                    dragOffsetX += dragAmount.x.roundToInt()

                    widthPixel = with(density) {
                        widthDp.toPx()
                    }.roundToInt()

                    heightPixel = with(density) {
                        heightDp.toPx()
                    }.roundToInt()

                    onResizeGridItem(
                        pagerState.currentPage,
                        widthPixel,
                        heightPixel,
                        gridIntSize.width,
                        gridIntSize.height,
                        selectedGridItemPixel?.gridItem,
                        Anchor.TOP_END,
                    )
                },
                onBottomEndDrag = { change, dragAmount ->
                    change.consume()
                    val dragAmountXDp = with(density) {
                        dragAmount.x.toDp()
                    }

                    val dragAmountYDp = with(density) {
                        dragAmount.y.toDp()
                    }

                    widthDp += dragAmountXDp
                    heightDp += dragAmountYDp

                    widthPixel = with(density) {
                        widthDp.toPx()
                    }.roundToInt()

                    heightPixel = with(density) {
                        heightDp.toPx()
                    }.roundToInt()

                    onResizeGridItem(
                        pagerState.currentPage,
                        widthPixel,
                        heightPixel,
                        gridIntSize.width,
                        gridIntSize.height,
                        selectedGridItemPixel?.gridItem,
                        Anchor.TOP_START,
                    )
                },
            )
        }
    }
}