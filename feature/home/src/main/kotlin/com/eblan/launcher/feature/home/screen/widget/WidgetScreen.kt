package com.eblan.launcher.feature.home.screen.widget

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.grid.getSize
import com.eblan.launcher.domain.grid.getWidgetGridItem
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.calculatePage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    drag: Drag,
    dragIntOffset: IntOffset,
    alpha: Float,
    onLongPress: (
        currentPage: Int,
        gridItemSource: GridItemSource,
    ) -> Unit,
    onDragging: () -> Unit,
    onUpdateAlpha: (Float) -> Unit,
    onDismiss: suspend () -> Unit,
) {
    val density = LocalDensity.current

    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    var isLongPress by remember { mutableStateOf(false) }

    val state = rememberLazyListState()

    var totalDrag by remember { mutableFloatStateOf(0f) }

    val maxDrag = with(LocalDensity.current) { 100.dp.toPx() }

    var isPointerUp by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val atTop = state.firstVisibleItemIndex == 0 &&
                        state.firstVisibleItemScrollOffset == 0

                if (atTop) {
                    totalDrag += available.y

                    totalDrag = totalDrag.coerceIn(-maxDrag, maxDrag)

                    val newAlpha = 1f - (totalDrag / maxDrag)

                    onUpdateAlpha(newAlpha.coerceIn(0f, 1f))
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (state.firstVisibleItemIndex > 0 ||
                    state.firstVisibleItemScrollOffset > 0
                ) {
                    totalDrag = 0f

                    onUpdateAlpha(1f)
                }

                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                isPointerUp = true

                return super.onPostFling(consumed, available)
            }
        }
    }

    LaunchedEffect(key1 = dragIntOffset) {
        val isDraggingOnGrid = dragIntOffset.y < (rootHeight - dockHeight)

        if (drag == Drag.Dragging &&
            isLongPress &&
            isDraggingOnGrid
        ) {
            onDragging()
        }
    }

    LaunchedEffect(key1 = isPointerUp) {
        if (isPointerUp && alpha < 0.5f) {
            onDismiss()
        }

        if (isPointerUp && alpha >= 0.5f) {
            onUpdateAlpha(1f)

            isPointerUp = false
        }
    }

    Box(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
            .fillMaxSize(),
    ) {
        when {
            eblanAppWidgetProviderInfos.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                LazyColumn(state = state) {
                    items(eblanAppWidgetProviderInfos.keys.toList()) { eblanApplicationInfo ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AsyncImage(
                                model = eblanApplicationInfo.icon,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                            )

                            Text(
                                text = eblanApplicationInfo.label.toString(),
                            )

                            eblanAppWidgetProviderInfos[eblanApplicationInfo]?.forEach { eblanAppWidgetProviderInfo ->
                                var intOffset by remember { mutableStateOf(IntOffset.Zero) }

                                val preview = eblanAppWidgetProviderInfo.preview
                                    ?: eblanAppWidgetProviderInfo.eblanApplicationInfo.icon

                                val size = with(density) {
                                    val (width, height) = getSize(
                                        rows = rows,
                                        columns = columns,
                                        gridWidth = rootWidth,
                                        gridHeight = rootHeight - dockHeight,
                                        targetCellWidth = eblanAppWidgetProviderInfo.targetCellWidth,
                                        targetCellHeight = eblanAppWidgetProviderInfo.targetCellHeight,
                                        minWidth = eblanAppWidgetProviderInfo.minWidth,
                                        minHeight = eblanAppWidgetProviderInfo.minHeight,
                                    )

                                    DpSize(width = width.toDp(), height = height.toDp())
                                }

                                AsyncImage(
                                    modifier = Modifier
                                        .dragAndDropSource(
                                            block = {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        isLongPress = true

                                                        onLongPress(
                                                            page,
                                                            GridItemSource.New(
                                                                gridItem = getWidgetGridItem(
                                                                    page = page,
                                                                    rows = rows,
                                                                    columns = columns,
                                                                    componentName = eblanAppWidgetProviderInfo.componentName,
                                                                    configure = eblanAppWidgetProviderInfo.configure,
                                                                    packageName = eblanAppWidgetProviderInfo.packageName,
                                                                    targetCellHeight = eblanAppWidgetProviderInfo.targetCellHeight,
                                                                    targetCellWidth = eblanAppWidgetProviderInfo.targetCellWidth,
                                                                    minWidth = eblanAppWidgetProviderInfo.minWidth,
                                                                    minHeight = eblanAppWidgetProviderInfo.minHeight,
                                                                    resizeMode = eblanAppWidgetProviderInfo.resizeMode,
                                                                    minResizeWidth = eblanAppWidgetProviderInfo.minResizeWidth,
                                                                    minResizeHeight = eblanAppWidgetProviderInfo.minResizeHeight,
                                                                    maxResizeWidth = eblanAppWidgetProviderInfo.maxResizeWidth,
                                                                    maxResizeHeight = eblanAppWidgetProviderInfo.maxResizeHeight,
                                                                    preview = eblanAppWidgetProviderInfo.preview,
                                                                    gridWidth = rootWidth,
                                                                    gridHeight = rootHeight - dockHeight,
                                                                ),
                                                            ),
                                                        )

                                                        startTransfer(
                                                            DragAndDropTransferData(
                                                                clipData = ClipData.newPlainText(
                                                                    "Drag",
                                                                    "Drag",
                                                                ),
                                                            ),
                                                        )
                                                    },
                                                )
                                            },
                                        )
                                        .size(size)
                                        .onGloballyPositioned { layoutCoordinates ->
                                            intOffset = layoutCoordinates.positionInRoot().round()
                                        },
                                    model = preview,
                                    contentDescription = null,
                                )

                                val infoText = """
    ${eblanAppWidgetProviderInfo.targetCellWidth}x${eblanAppWidgetProviderInfo.targetCellHeight}
    MinWidth = ${eblanAppWidgetProviderInfo.minWidth} MinHeight = ${eblanAppWidgetProviderInfo.minHeight}
    ResizeMode = ${eblanAppWidgetProviderInfo.resizeMode}
    MinResizeWidth = ${eblanAppWidgetProviderInfo.minResizeWidth} MinResizeHeight = ${eblanAppWidgetProviderInfo.minResizeHeight}
    MaxResizeWidth = ${eblanAppWidgetProviderInfo.maxResizeWidth} MaxResizeHeight = ${eblanAppWidgetProviderInfo.maxResizeHeight}
    """.trimIndent()

                                Text(
                                    text = infoText,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}