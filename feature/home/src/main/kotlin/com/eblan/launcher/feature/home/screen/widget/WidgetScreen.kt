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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.util.calculatePage
import kotlin.math.roundToInt

@Composable
fun WidgetScreen(
    modifier: Modifier = Modifier,
    widgetState: WidgetState,
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

    val cellWidth = screenSize.width / userData.columns

    val cellHeight = screenSize.height / userData.rows

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

                    val previewDpSize = with(density) {
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

                    AsyncImage(
                        modifier = Modifier
                            .pointerInput(key1 = appWidgetProviderInfo) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val down = awaitFirstDown(requireUnconsumed = false)

                                        val longPressChange =
                                            awaitLongPressOrCancellation(down.id)

                                        if (longPressChange != null) {
                                            val gridItemLayoutInfo =
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                    widgetState.getGridItemLayoutInfo(
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
                                                    widgetState.getGridItemLayoutInfo(
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