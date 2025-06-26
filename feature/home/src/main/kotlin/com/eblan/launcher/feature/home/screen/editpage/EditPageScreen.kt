package com.eblan.launcher.feature.home.screen.editpage

import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.DockGrid
import com.eblan.launcher.feature.home.component.DragGridSubcomposeLayout
import com.eblan.launcher.feature.home.component.SettingsMenu
import com.eblan.launcher.feature.home.component.SettingsMenuPositionProvider
import com.eblan.launcher.feature.home.util.calculatePage

@Composable
fun EditPageScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dockRows: Int,
    dockColumns: Int,
    dragIntOffset: IntOffset,
    gridItems: Map<Int, List<GridItem>>,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    onSettings: () -> Unit,
) {
    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

    val horizontalPagerState = rememberPagerState(
        initialPage = if (infiniteScroll) (Int.MAX_VALUE / 2) + currentPage else currentPage,
        pageCount = {
            if (infiniteScroll) {
                Int.MAX_VALUE
            } else {
                pageCount
            }
        },
    )

    val horizontalPagerPadding = 20.dp

    val cardPadding = 5.dp

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(all = horizontalPagerPadding),
        ) { index ->
            val horizontalPage = calculatePage(
                index = index,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )

            OutlinedCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(cardPadding),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f)),
                border = BorderStroke(width = 2.dp, color = Color.White),
            ) {
                DragGridSubcomposeLayout(
                    modifier = Modifier.fillMaxSize(),
                    index = horizontalPage,
                    rows = rows,
                    columns = columns,
                    gridItems = gridItems,
                    gridItemContent = { gridItem ->
                        when (val gridItemData = gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize(),
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
                    },
                )
            }
        }

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
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
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