package com.eblan.launcher.feature.home.screen.widget

import android.appwidget.AppWidgetProviderInfo
import android.os.Build
import android.util.DisplayMetrics
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import coil.compose.AsyncImage
import com.eblan.launcher.domain.grid.coordinatesToStartPosition
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.windowmanager.ScreenSize
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
    drag: Drag,
    gridItemOffset: IntOffset,
    appWidgetProviderInfos: Map<EblanApplicationInfo, List<AppWidgetProviderInfo>>,
    screenSize: ScreenSize,
    onLongPressWidget: (ImageBitmap?) -> Unit,
    onDragStart: (
        offset: IntOffset,
        size: IntSize,
        GridItemLayoutInfo,
    ) -> Unit,
) {
    val context = LocalContext.current

    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    var providerInfo by remember { mutableStateOf<AppWidgetProviderInfo?>(null) }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Start) {
            val addGridItemLayoutInfo = getGridItemLayoutInfo(
                page = page,
                appWidgetProviderInfo = providerInfo!!,
                rows = rows,
                columns = columns,
                appWidgetProviderInfoOffset = gridItemOffset,
                screenSize = screenSize,
            )

            val size = IntSize(
                addGridItemLayoutInfo.width,
                addGridItemLayoutInfo.height,
            )

            val offset = IntOffset(
                gridItemOffset.x - size.width / 2,
                gridItemOffset.y - size.height / 2,
            )

            onDragStart(
                offset,
                size,
                addGridItemLayoutInfo,
            )
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
    ) {
        items(appWidgetProviderInfos.keys.toList()) { eblanApplicationInfo ->
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
                    text = eblanApplicationInfo.label,
                )

                appWidgetProviderInfos[eblanApplicationInfo]?.forEach { appWidgetProviderInfo ->
                    AsyncImage(
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        providerInfo = appWidgetProviderInfo

                                        val preview =
                                            appWidgetProviderInfo.loadPreviewImage(context, 0)

                                        onLongPressWidget(
                                            preview.toBitmapOrNull()?.asImageBitmap(),
                                        )
                                    },
                                )
                            }
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 100.dp),
                        model = appWidgetProviderInfo.loadPreviewImage(
                            context,
                            DisplayMetrics.DENSITY_DEFAULT,
                        ) ?: appWidgetProviderInfo.loadIcon(
                            context,
                            DisplayMetrics.DENSITY_DEFAULT,
                        ),
                        contentDescription = null,
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Text(
                            text = "${appWidgetProviderInfo.targetCellWidth}x${appWidgetProviderInfo.targetCellHeight}",
                        )

                        Text(text = "MinWidth = ${appWidgetProviderInfo.minWidth} MinHeight = ${appWidgetProviderInfo.minHeight}")

                        Text(text = "ResizeMode = ${appWidgetProviderInfo.resizeMode}")

                        Text(text = "MinResizeWidth = ${appWidgetProviderInfo.minResizeWidth} MinResizeHeight = ${appWidgetProviderInfo.minResizeHeight}")

                        Text(text = "MaxResizeWidth = ${appWidgetProviderInfo.maxResizeWidth} MaxResizeHeight = ${appWidgetProviderInfo.maxResizeHeight}")
                    } else {
                        Text(text = "MinWidth = ${appWidgetProviderInfo.minWidth} MinHeight = ${appWidgetProviderInfo.minHeight}")

                        Text(text = "ResizeMode = ${appWidgetProviderInfo.resizeMode}")

                        Text(text = "MinResizeWidth = ${appWidgetProviderInfo.minResizeWidth} MinResizeHeight = ${appWidgetProviderInfo.minResizeHeight}")
                    }
                }
            }
        }
    }
}

private fun getGridItemLayoutInfo(
    page: Int,
    appWidgetProviderInfo: AppWidgetProviderInfo,
    rows: Int,
    columns: Int,
    appWidgetProviderInfoOffset: IntOffset,
    screenSize: ScreenSize,
): GridItemLayoutInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getGridItemLayoutInfo(
            page = page,
            componentName = appWidgetProviderInfo.provider.flattenToString(),
            rows = rows,
            columns = columns,
            x = appWidgetProviderInfoOffset.x,
            y = appWidgetProviderInfoOffset.y,
            rowSpan = appWidgetProviderInfo.targetCellHeight,
            columnSpan = appWidgetProviderInfo.targetCellWidth,
            minWidth = appWidgetProviderInfo.minWidth,
            minHeight = appWidgetProviderInfo.minHeight,
            resizeMode = appWidgetProviderInfo.resizeMode,
            minResizeWidth = appWidgetProviderInfo.minResizeWidth,
            minResizeHeight = appWidgetProviderInfo.minResizeHeight,
            maxResizeWidth = appWidgetProviderInfo.maxResizeWidth,
            maxResizeHeight = appWidgetProviderInfo.maxResizeHeight,
            screenSize = screenSize,
        )
    } else {
        getGridItemLayoutInfo(
            page = page,
            componentName = appWidgetProviderInfo.provider.flattenToString(),
            rows = rows,
            columns = columns,
            x = appWidgetProviderInfoOffset.x,
            y = appWidgetProviderInfoOffset.y,
            rowSpan = 0,
            columnSpan = 0,
            minWidth = appWidgetProviderInfo.minWidth,
            minHeight = appWidgetProviderInfo.minHeight,
            resizeMode = appWidgetProviderInfo.resizeMode,
            minResizeWidth = appWidgetProviderInfo.minResizeWidth,
            minResizeHeight = appWidgetProviderInfo.minResizeHeight,
            maxResizeWidth = 0,
            maxResizeHeight = 0,
            screenSize = screenSize,
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun getGridItemLayoutInfo(
    page: Int,
    componentName: String,
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
    screenSize: ScreenSize,
): GridItemLayoutInfo {
    val cellWidth = screenSize.width / columns

    val cellHeight = screenSize.height / rows

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

    val (startRow, startColumn) = coordinatesToStartPosition(
        x = x,
        y = y,
        rows = rows,
        columns = columns,
        screenWidth = screenSize.width,
        screenHeight = screenSize.height,
    )

    val data = GridItemData.Widget(
        appWidgetId = -1,
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
        data = data,
    )

    return GridItemLayoutInfo(
        gridItem = gridItem,
        width = gridItem.columnSpan * cellWidth,
        height = gridItem.rowSpan * cellHeight,
        x = gridItem.startColumn * cellWidth,
        y = gridItem.startRow * cellHeight,
    )
}