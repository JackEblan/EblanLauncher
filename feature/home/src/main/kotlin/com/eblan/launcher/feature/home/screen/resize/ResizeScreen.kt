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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmapOrNull
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.GridSubcomposeLayout
import com.eblan.launcher.feature.home.component.ResizeGridSubcomposeLayout

@Composable
fun ResizeScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
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
    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val context = LocalContext.current

    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        ResizeGridSubcomposeLayout(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f),
            rows = rows,
            columns = columns,
            gridItemId = gridItemLayoutInfo?.gridItem?.id,
            gridItems = gridItems[currentPage],
            onResizeGridItem = onResizeGridItem,
            onResizeEnd = onResizeEnd,
            gridItemContent = { gridItem ->
                when (val gridItemData = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
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
                                text = gridItemData.label.toString(),
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
                            val preview = remember {
                                appWidgetInfo.loadPreviewImage(context, 0)
                                    .toBitmapOrNull()
                            }

                            AsyncImage(
                                model = preview,
                                contentDescription = null,
                            )
                        }
                    }
                }
            },
        )

        GridSubcomposeLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            rows = dockRows,
            columns = dockColumns,
            gridItems = dockGridItems,
        ) { dockGridItem, _, _, _, _ ->
            when (val gridItemData = dockGridItem.data) {
                is GridItemData.ApplicationInfo -> {
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
                            text = gridItemData.label.toString(),
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