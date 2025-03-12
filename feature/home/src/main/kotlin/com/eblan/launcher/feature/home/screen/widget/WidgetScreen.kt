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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.roundToIntSize
import coil.compose.AsyncImage
import com.eblan.launcher.domain.model.EblanApplicationInfo
import kotlin.math.roundToInt

@Composable
fun WidgetScreen(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    rows: Int,
    columns: Int,
    screenSize: IntSize,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
    onLongPressAppWidgetProviderInfo: (Offset, IntSize) -> Unit,
    onAddAppWidgetProviderInfoGridItem: (
        page: Int,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        minWidth: Int,
        minHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
) {
    val density = LocalDensity.current

    val context = LocalContext.current

    val cellWidth = screenSize.width / columns

    val cellHeight = screenSize.height / rows

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
                                            awaitLongPressOrCancellation(down.id) ?: continue

                                        if (!longPressChange.isConsumed) {
                                            onLongPressAppWidgetProviderInfo(
                                                appWidgetProviderInfoOffset,
                                                previewDpSize.toSize().roundToIntSize(),
                                            )

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                onAddAppWidgetProviderInfoGridItem(
                                                    pagerState.currentPage,
                                                    appWidgetProviderInfoOffset.x.roundToInt(),
                                                    appWidgetProviderInfoOffset.y.roundToInt(),
                                                    appWidgetProviderInfo.targetCellHeight,
                                                    appWidgetProviderInfo.targetCellWidth,
                                                    appWidgetProviderInfo.minWidth,
                                                    appWidgetProviderInfo.minHeight,
                                                    screenSize.width,
                                                    screenSize.height,
                                                )
                                            } else {
                                                onAddAppWidgetProviderInfoGridItem(
                                                    pagerState.currentPage,
                                                    appWidgetProviderInfoOffset.x.roundToInt(),
                                                    appWidgetProviderInfoOffset.y.roundToInt(),
                                                    0,
                                                    0,
                                                    appWidgetProviderInfo.minWidth,
                                                    appWidgetProviderInfo.minHeight,
                                                    screenSize.width,
                                                    screenSize.height,
                                                )
                                            }
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
                    } else {
                        Text(text = "MinWidth = ${appWidgetProviderInfo.minWidth} MinHeight = ${appWidgetProviderInfo.minHeight}")
                    }
                }
            }
        }
    }
}