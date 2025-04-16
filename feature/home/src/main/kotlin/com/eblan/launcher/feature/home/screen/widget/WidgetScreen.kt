package com.eblan.launcher.feature.home.screen.widget

import android.appwidget.AppWidgetProviderInfo
import android.os.Build
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eblan.launcher.domain.grid.coordinatesToStartPosition
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.util.calculatePage
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun WidgetScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    userData: UserData,
    screenSize: IntSize,
    drag: Drag,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
    onLongPressAppWidgetProviderInfo: (GridItemLayoutInfo) -> Unit,
    onDragStart: () -> Unit,
) {
    val density = LocalDensity.current

    val context = LocalContext.current

    val page = calculatePage(
        index = currentPage,
        infiniteScroll = userData.infiniteScroll,
        pageCount = userData.pageCount,
    )

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Start) {
            onDragStart()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
    ) {
        items(appWidgetProviderInfos) { (eblanApplicationInfo, appWidgetProviderInfos) ->
            Column(
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

                appWidgetProviderInfos.forEach { appWidgetProviderInfo ->
                    var appWidgetProviderInfoOffset = Offset.Zero

                    val previewDpSize = getPreviewDpSize(
                        rows = userData.rows,
                        columns = userData.columns,
                        screenSize = screenSize,
                        density = density,
                        appWidgetProviderInfo = appWidgetProviderInfo,
                    )

                    AsyncImage(
                        modifier = Modifier
                            .pointerInput(key1 = appWidgetProviderInfo) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val down = awaitFirstDown(requireUnconsumed = false)

                                        val longPressChange =
                                            awaitLongPressOrCancellation(down.id)

                                        if (longPressChange != null) {
                                            val gridItemLayoutInfo = getGridItemLayoutInfo(
                                                page = page,
                                                appWidgetProviderInfo = appWidgetProviderInfo,
                                                userData = userData,
                                                appWidgetProviderInfoOffset = appWidgetProviderInfoOffset,
                                                screenSize = screenSize,
                                            )

                                            onLongPressAppWidgetProviderInfo(gridItemLayoutInfo)
                                        }
                                    }
                                }
                            }
                            .size(previewDpSize)
                            .onGloballyPositioned { layoutCoordinates ->
                                appWidgetProviderInfoOffset = layoutCoordinates.positionOnScreen()
                            },

                        model = appWidgetProviderInfo.loadPreviewImage(context, 0),
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

private fun getPreviewDpSize(
    rows: Int,
    columns: Int,
    screenSize: IntSize,
    density: Density,
    appWidgetProviderInfo: AppWidgetProviderInfo,
): DpSize {
    val cellWidth = screenSize.width / columns

    val cellHeight = screenSize.height / rows

    return with(density) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && appWidgetProviderInfo.targetCellWidth != 0 && appWidgetProviderInfo.targetCellHeight != 0) {
            DpSize(
                width = (appWidgetProviderInfo.targetCellWidth * cellWidth).toDp(),
                height = (appWidgetProviderInfo.targetCellHeight * cellHeight).toDp(),
            )
        } else {
            DpSize(
                width = appWidgetProviderInfo.minWidth.toDp(),
                height = appWidgetProviderInfo.minHeight.toDp(),
            )
        }
    }
}

private fun getGridItemLayoutInfo(
    page: Int,
    appWidgetProviderInfo: AppWidgetProviderInfo,
    userData: UserData,
    appWidgetProviderInfoOffset: Offset,
    screenSize: IntSize,
): GridItemLayoutInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getGridItemLayoutInfo(
            page = page,
            componentName = appWidgetProviderInfo.provider.flattenToString(),
            rows = userData.rows,
            columns = userData.columns,
            x = appWidgetProviderInfoOffset.x.roundToInt(),
            y = appWidgetProviderInfoOffset.y.roundToInt(),
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
            rows = userData.rows,
            columns = userData.columns,
            x = appWidgetProviderInfoOffset.x.roundToInt(),
            y = appWidgetProviderInfoOffset.y.roundToInt(),
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
    screenSize: IntSize,
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
        screenWidth = screenSize.width,
        screenHeight = screenSize.height,
    )
}