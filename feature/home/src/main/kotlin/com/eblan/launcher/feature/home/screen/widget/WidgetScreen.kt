package com.eblan.launcher.feature.home.screen.widget

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch

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
    gridItemLayoutInfo: GridItemLayoutInfo?,
    onLongPressWidget: (
        currentPage: Int,
        imageBitmap: ImageBitmap?,
        intOffset: IntOffset,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onDragStart: () -> Unit,
) {
    val density = LocalDensity.current

    val appWidgetHost = LocalAppWidgetHost.current

    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Start && gridItemLayoutInfo != null) {
            onDragStart()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            eblanAppWidgetProviderInfos.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                LazyColumn {
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
                                val graphicsLayer = rememberGraphicsLayer()

                                val preview = eblanAppWidgetProviderInfo.preview
                                    ?: eblanAppWidgetProviderInfo.eblanApplicationInfo.icon

                                val size = with(density) {
                                    val (width, height) = getSize(
                                        columns = columns,
                                        gridHeight = rootHeight - dockHeight,
                                        gridWidth = rootWidth,
                                        minHeight = eblanAppWidgetProviderInfo.minHeight,
                                        minWidth = eblanAppWidgetProviderInfo.minWidth,
                                        rows = rows,
                                        targetCellHeight = eblanAppWidgetProviderInfo.targetCellHeight,
                                        targetCellWidth = eblanAppWidgetProviderInfo.targetCellWidth,
                                    )

                                    DpSize(width = width.toDp(), height = height.toDp())
                                }

                                var intOffset by remember { mutableStateOf(IntOffset.Zero) }

                                AsyncImage(
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
                                                        onLongPressWidget(
                                                            page,
                                                            graphicsLayer.toImageBitmap(),
                                                            intOffset,
                                                            getGridItemLayoutInfo(
                                                                allocateAppWidgetId = appWidgetHost.allocateAppWidgetId(),
                                                                page = page,
                                                                componentName = eblanAppWidgetProviderInfo.componentName,
                                                                configure = eblanAppWidgetProviderInfo.configure,
                                                                packageName = eblanAppWidgetProviderInfo.packageName,
                                                                rows = rows,
                                                                columns = columns,
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
                                                        )
                                                    }
                                                },
                                            )
                                        }
                                        .onGloballyPositioned { layoutCoordinates ->
                                            intOffset = layoutCoordinates.positionInRoot().round()
                                        }
                                        .size(size),
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

private fun getGridItemLayoutInfo(
    allocateAppWidgetId: Int,
    page: Int,
    componentName: String,
    configure: String?,
    packageName: String,
    rows: Int,
    columns: Int,
    targetCellHeight: Int,
    targetCellWidth: Int,
    minWidth: Int,
    minHeight: Int,
    resizeMode: Int,
    minResizeWidth: Int,
    minResizeHeight: Int,
    maxResizeWidth: Int,
    maxResizeHeight: Int,
    preview: String?,
    gridWidth: Int,
    gridHeight: Int,
): GridItemLayoutInfo {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val (rowSpan, columnSpan) = getSpan(
        cellHeight = cellHeight,
        cellWidth = cellWidth,
        minHeight = minHeight,
        minWidth = minWidth,
        targetCellHeight = targetCellHeight,
        targetCellWidth = targetCellWidth,
    )

    val (width, height) = getSize(
        columns = columns,
        gridHeight = gridHeight,
        gridWidth = gridWidth,
        minHeight = minHeight,
        minWidth = minWidth,
        rows = rows,
        targetCellHeight = targetCellHeight,
        targetCellWidth = targetCellWidth,
    )

    val data = GridItemData.Widget(
        appWidgetId = allocateAppWidgetId,
        componentName = componentName,
        configure = configure,
        width = width,
        height = height,
        resizeMode = resizeMode,
        minResizeWidth = minResizeWidth,
        minResizeHeight = minResizeHeight,
        maxResizeWidth = maxResizeWidth,
        maxResizeHeight = maxResizeHeight,
        preview = preview,
    )

    val gridItem = GridItem(
        id = 0,
        page = page,
        startRow = 0,
        startColumn = 0,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        dataId = packageName,
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

private fun getSpan(
    cellHeight: Int,
    cellWidth: Int,
    minHeight: Int,
    minWidth: Int,
    targetCellHeight: Int,
    targetCellWidth: Int,
): Pair<Int, Int> {
    val rowSpan = if (targetCellHeight == 0) {
        (minHeight + cellHeight - 1) / cellHeight
    } else {
        targetCellHeight
    }

    val columnSpan = if (targetCellWidth == 0) {
        (minWidth + cellWidth - 1) / cellWidth
    } else {
        targetCellWidth
    }

    return rowSpan to columnSpan
}

private fun getSize(
    columns: Int,
    rows: Int,
    gridWidth: Int,
    gridHeight: Int,
    targetCellWidth: Int,
    targetCellHeight: Int,
    minWidth: Int,
    minHeight: Int,
): Pair<Int, Int> {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val width = if (targetCellWidth > 0) {
        targetCellWidth * cellWidth
    } else {
        minWidth
    }

    val height = if (targetCellHeight > 0) {
        targetCellHeight * cellHeight
    } else {
        minHeight
    }

    return width to height
}