package com.eblan.launcher.feature.home.screen.resize

import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.DockGrid
import com.eblan.launcher.feature.home.component.ResizeGridSubcomposeLayout
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.util.calculatePage

@Composable
fun ResizeScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dockRows: Int,
    dockColumns: Int,
    gridItems: Map<Int, List<GridItem>>,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    onResizeGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onResizeEnd: () -> Unit,
) {
    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        ResizeGridSubcomposeLayout(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f),
            page = page,
            rows = rows,
            columns = columns,
            gridItemId = gridItemLayoutInfo?.gridItem?.id,
            gridItems = gridItems,
            onResizeGridItem = onResizeGridItem,
            onResizeEnd = onResizeEnd,
            gridItemContent = { gridItem ->
                when (val gridItemData = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        val color = when (textColor) {
                            TextColor.White -> Color.White
                            TextColor.Black -> Color.Black
                        }

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AsyncImage(
                                model = gridItemData.icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp, 40.dp)
                                    .weight(1f),
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = gridItemData.label,
                                modifier = Modifier.weight(1f),
                                color = color,
                                textAlign = TextAlign.Center,
                                fontSize = TextUnit(
                                    value = 10f,
                                    type = TextUnitType.Sp,
                                ),
                            )
                        }
                    }

                    is GridItemData.Widget -> {
                        val appWidgetInfo =
                            appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

                        if (appWidgetInfo != null) {
                            AndroidView(
                                factory = {
                                    appWidgetHost.createView(
                                        appWidgetId = gridItemData.appWidgetId,
                                        appWidgetProviderInfo = appWidgetInfo,
                                    ).apply {
                                        layoutParams = FrameLayout.LayoutParams(
                                            FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT,
                                        )

                                        setAppWidget(appWidgetId, appWidgetInfo)
                                    }
                                },
                            )
                        }
                    }
                }
            },
        )

        DockGrid(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            rows = dockRows,
            columns = dockColumns,
            dockGridItems = dockGridItems,
        ) { dockGridItem, _, _, _, _ ->
            when (val gridItemData = dockGridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    val color = when (textColor) {
                        TextColor.White -> Color.White
                        TextColor.Black -> Color.Black
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AsyncImage(
                            model = gridItemData.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp, 40.dp)
                                .weight(1f),
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = gridItemData.label,
                            modifier = Modifier.weight(1f),
                            color = color,
                            textAlign = TextAlign.Center,
                            fontSize = TextUnit(
                                value = 10f,
                                type = TextUnitType.Sp,
                            ),
                        )
                    }
                }

                is GridItemData.Widget -> {
                    val appWidgetInfo =
                        appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

                    if (appWidgetInfo != null) {
                        AndroidView(
                            factory = {
                                appWidgetHost.createView(
                                    appWidgetId = gridItemData.appWidgetId,
                                    appWidgetProviderInfo = appWidgetInfo,
                                ).apply {
                                    layoutParams = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                    )

                                    setAppWidget(appWidgetId, appWidgetInfo)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}