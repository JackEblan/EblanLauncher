package com.eblan.launcher.feature.home.screen.application

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ApplicationScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    gridItemOffset: IntOffset,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    constraintsMaxWidth: Int,
    constraintsMaxHeight: Int,
    dockHeight: Int,
    drag: Drag,
    applicationScreenY: Float,
    onLongPressApplicationInfo: (ImageBitmap) -> Unit,
    onDragStart: (
        size: IntSize,
        GridItemLayoutInfo,
    ) -> Unit,
    onClose: (Float) -> Unit,
) {
    var data by remember { mutableStateOf<GridItemData?>(null) }

    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    var accumulated = remember { Animatable(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val isAtTop =
                    gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0

                if (isAtTop) {
                    val newOffset = (accumulated.value + available.y).coerceAtLeast(0f)

                    if (newOffset != accumulated.value) {
                        scope.launch {
                            accumulated.snapTo(newOffset)
                        }

                        return Offset(0f, available.y) // consume scroll
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y < 0 && accumulated.value > 0f) {
                    val newOffset = (accumulated.value + available.y).coerceAtLeast(0f)

                    scope.launch {
                        accumulated.snapTo(newOffset)
                    }

                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (accumulated.value > constraintsMaxHeight / 2) {
                    onClose(accumulated.value)
                } else {
                    scope.launch {
                        accumulated.animateTo(0f)
                    }
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Start) {
            val page = calculatePage(
                index = currentPage,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )

            val layoutInfo = getGridItemLayoutInfo(
                page = page,
                rows = rows,
                columns = columns,
                x = gridItemOffset.x,
                y = gridItemOffset.y,
                gridWidth = constraintsMaxWidth,
                gridHeight = constraintsMaxHeight - dockHeight,
                data = data!!,
            )

            val size = IntSize(layoutInfo.width, layoutInfo.height)

            onDragStart(size, layoutInfo)
        }
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(0, applicationScreenY.roundToInt()) }
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = modifier
                .offset { IntOffset(0, accumulated.value.roundToInt()) }
                .fillMaxWidth(),
            state = gridState,
        ) {
            items(eblanApplicationInfos) { appInfo ->
                val graphicsLayer = rememberGraphicsLayer()

                Column(
                    modifier = Modifier
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    scope.launch {
                                        data = GridItemData.ApplicationInfo(
                                            packageName = appInfo.packageName,
                                            icon = appInfo.icon,
                                            label = appInfo.label,
                                        )
                                        onLongPressApplicationInfo(graphicsLayer.toImageBitmap())
                                    }
                                },
                            )
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(5.dp))

                    AsyncImage(
                        model = appInfo.icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = appInfo.label,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun getGridItemLayoutInfo(
    page: Int,
    rows: Int,
    columns: Int,
    x: Int,
    y: Int,
    gridWidth: Int,
    gridHeight: Int,
    data: GridItemData,
): GridItemLayoutInfo {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val startColumn = x / cellWidth

    val startRow = y / cellHeight

    val gridItem = GridItem(
        id = Uuid.random().toHexString(),
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = 1,
        columnSpan = 1,
        data = data,
        associate = Associate.Grid,
    )

    return GridItemLayoutInfo(
        gridItem = gridItem,
        width = gridItem.columnSpan * cellWidth,
        height = gridItem.rowSpan * cellHeight,
        x = gridItem.startColumn * cellWidth,
        y = gridItem.startRow * cellHeight,
    )
}
