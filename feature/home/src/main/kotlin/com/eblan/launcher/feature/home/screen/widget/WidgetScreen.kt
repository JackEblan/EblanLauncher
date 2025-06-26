package com.eblan.launcher.feature.home.screen.widget

import android.graphics.drawable.BitmapDrawable
import android.os.Build
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.widgetmanager.AppWidgetHostWrapper
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun WidgetScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dragIntOffset: IntOffset,
    eblanAppWidgetProviderInfosByGroup: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    drag: Drag,
    onLongPressWidget: (ImageBitmap?) -> Unit,
    onDragStart: (
        intOffset: IntOffset,
        intSize: IntSize,
        GridItemLayoutInfo,
    ) -> Unit,
) {
    var selectedEblanAppWidgetProviderInfo by remember {
        mutableStateOf<EblanAppWidgetProviderInfo?>(
            null,
        )
    }

    val appWidgetHost = LocalAppWidgetHost.current

    LaunchedEffect(key1 = drag) {
        handleDrag(
            appWidgetHost = appWidgetHost,
            drag = drag,
            selectedEblanAppWidgetProviderInfo = selectedEblanAppWidgetProviderInfo,
            currentPage = currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            rows = rows,
            columns = columns,
            dragIntOffset = dragIntOffset,
            rootWidth = rootWidth,
            rootHeight = rootHeight,
            dockHeight = dockHeight,
            onDragStart = onDragStart,
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            eblanAppWidgetProviderInfosByGroup.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                LazyColumn(
                    modifier = modifier.fillMaxWidth(),
                ) {
                    items(eblanAppWidgetProviderInfosByGroup.keys.toList()) { eblanApplicationInfo ->
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

                            eblanAppWidgetProviderInfosByGroup[eblanApplicationInfo]?.forEach { eblanAppWidgetProviderInfo ->
                                var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

                                val context = LocalContext.current

                                val preview = eblanAppWidgetProviderInfo.preview
                                    ?: eblanAppWidgetProviderInfo.eblanApplicationInfo.icon

                                LaunchedEffect(key1 = preview) {
                                    val loader = ImageLoader(context)

                                    val request = ImageRequest.Builder(context).data(preview)
                                        .allowHardware(false).build()

                                    val result = loader.execute(request)

                                    if (result is SuccessResult) {
                                        val drawable = result.drawable

                                        val bitmap = (drawable as BitmapDrawable).bitmap

                                        imageBitmap = bitmap.asImageBitmap()
                                    }
                                }

                                AsyncImage(
                                    modifier = Modifier
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    selectedEblanAppWidgetProviderInfo =
                                                        eblanAppWidgetProviderInfo

                                                    onLongPressWidget(imageBitmap)
                                                },
                                            )
                                        }
                                        .fillMaxWidth(fraction = 0.5f),
                                    model = preview,
                                    contentDescription = null,
                                )

                                val infoText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    """
    ${eblanAppWidgetProviderInfo.targetCellWidth}x${eblanAppWidgetProviderInfo.targetCellHeight}
    MinWidth = ${eblanAppWidgetProviderInfo.minWidth} MinHeight = ${eblanAppWidgetProviderInfo.minHeight}
    ResizeMode = ${eblanAppWidgetProviderInfo.resizeMode}
    MinResizeWidth = ${eblanAppWidgetProviderInfo.minResizeWidth} MinResizeHeight = ${eblanAppWidgetProviderInfo.minResizeHeight}
    MaxResizeWidth = ${eblanAppWidgetProviderInfo.maxResizeWidth} MaxResizeHeight = ${eblanAppWidgetProviderInfo.maxResizeHeight}
    """.trimIndent()
                                } else {
                                    """
    MinWidth = ${eblanAppWidgetProviderInfo.minWidth} MinHeight = ${eblanAppWidgetProviderInfo.minHeight}
    ResizeMode = ${eblanAppWidgetProviderInfo.resizeMode}
    MinResizeWidth = ${eblanAppWidgetProviderInfo.minResizeWidth} MinResizeHeight = ${eblanAppWidgetProviderInfo.minResizeHeight}
    """.trimIndent()
                                }

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

private fun handleDrag(
    appWidgetHost: AppWidgetHostWrapper,
    drag: Drag,
    selectedEblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo?,
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    rows: Int,
    columns: Int,
    dragIntOffset: IntOffset,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    onDragStart: (intOffset: IntOffset, intSize: IntSize, GridItemLayoutInfo) -> Unit,
) {
    if (drag == Drag.Start && selectedEblanAppWidgetProviderInfo != null) {
        val allocateAppWidgetId = appWidgetHost.allocateAppWidgetId()

        val page = calculatePage(
            index = currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        val gridItemLayoutInfo = getGridItemLayoutInfo(
            allocateAppWidgetId = allocateAppWidgetId,
            page = page,
            eblanAppWidgetProviderInfo = selectedEblanAppWidgetProviderInfo,
            rows = rows,
            columns = columns,
            appWidgetProviderInfoOffset = dragIntOffset,
            gridWidth = rootWidth,
            gridHeight = rootHeight - dockHeight,
        )

        val intOffset = IntOffset(
            x = dragIntOffset.x - gridItemLayoutInfo.width / 2,
            y = dragIntOffset.y - gridItemLayoutInfo.height / 2,
        )

        val intSize = IntSize(
            gridItemLayoutInfo.width,
            gridItemLayoutInfo.height,
        )

        onDragStart(
            intOffset,
            intSize,
            gridItemLayoutInfo,
        )
    }
}

private fun getGridItemLayoutInfo(
    allocateAppWidgetId: Int,
    page: Int,
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    rows: Int,
    columns: Int,
    appWidgetProviderInfoOffset: IntOffset,
    gridWidth: Int,
    gridHeight: Int,
): GridItemLayoutInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getGridItemLayoutInfo(
            allocateAppWidgetId = allocateAppWidgetId,
            page = page,
            componentName = eblanAppWidgetProviderInfo.componentName,
            packageName = eblanAppWidgetProviderInfo.packageName,
            rows = rows,
            columns = columns,
            x = appWidgetProviderInfoOffset.x,
            y = appWidgetProviderInfoOffset.y,
            rowSpan = eblanAppWidgetProviderInfo.targetCellHeight,
            columnSpan = eblanAppWidgetProviderInfo.targetCellWidth,
            minWidth = eblanAppWidgetProviderInfo.minWidth,
            minHeight = eblanAppWidgetProviderInfo.minHeight,
            resizeMode = eblanAppWidgetProviderInfo.resizeMode,
            minResizeWidth = eblanAppWidgetProviderInfo.minResizeWidth,
            minResizeHeight = eblanAppWidgetProviderInfo.minResizeHeight,
            maxResizeWidth = eblanAppWidgetProviderInfo.maxResizeWidth,
            maxResizeHeight = eblanAppWidgetProviderInfo.maxResizeHeight,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
        )
    } else {
        getGridItemLayoutInfo(
            allocateAppWidgetId = allocateAppWidgetId,
            page = page,
            componentName = eblanAppWidgetProviderInfo.componentName,
            packageName = eblanAppWidgetProviderInfo.packageName,
            rows = rows,
            columns = columns,
            x = appWidgetProviderInfoOffset.x,
            y = appWidgetProviderInfoOffset.y,
            rowSpan = 0,
            columnSpan = 0,
            minWidth = eblanAppWidgetProviderInfo.minWidth,
            minHeight = eblanAppWidgetProviderInfo.minHeight,
            resizeMode = eblanAppWidgetProviderInfo.resizeMode,
            minResizeWidth = eblanAppWidgetProviderInfo.minResizeWidth,
            minResizeHeight = eblanAppWidgetProviderInfo.minResizeHeight,
            maxResizeWidth = 0,
            maxResizeHeight = 0,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun getGridItemLayoutInfo(
    allocateAppWidgetId: Int,
    page: Int,
    componentName: String,
    packageName: String,
    rows: Int,
    columns: Int,
    x: Int,
    y: Int,
    rowSpan: Int,
    columnSpan: Int,
    minWidth: Int,
    minHeight: Int,
    resizeMode: Int,
    minResizeWidth: Int,
    minResizeHeight: Int,
    maxResizeWidth: Int,
    maxResizeHeight: Int,
    gridWidth: Int,
    gridHeight: Int,
): GridItemLayoutInfo {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val newRowSpan = if (rowSpan == 0) {
        (minHeight + cellHeight - 1) / cellHeight
    } else {
        rowSpan
    }

    val newColumnSpan = if (columnSpan == 0) {
        (minWidth + cellWidth - 1) / cellWidth
    } else {
        columnSpan
    }

    val newWidth = if (columnSpan == 0) {
        minWidth
    } else {
        columnSpan * cellWidth
    }

    val newHeight = if (rowSpan == 0) {
        minHeight
    } else {
        rowSpan * cellHeight
    }

    val startColumn = x / cellWidth

    val startRow = y / cellHeight

    val data = GridItemData.Widget(
        appWidgetId = allocateAppWidgetId,
        componentName = componentName,
        width = newWidth,
        height = newHeight,
        resizeMode = resizeMode,
        minResizeWidth = minResizeWidth,
        minResizeHeight = minResizeHeight,
        maxResizeWidth = maxResizeWidth,
        maxResizeHeight = maxResizeHeight,
    )

    val gridItem = GridItem(
        id = Uuid.random().toHexString(),
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = newRowSpan,
        columnSpan = newColumnSpan,
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