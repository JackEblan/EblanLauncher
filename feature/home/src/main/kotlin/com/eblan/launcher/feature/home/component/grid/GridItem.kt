package com.eblan.launcher.feature.home.component.grid

import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItemData

@Composable
fun WidgetGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Widget,
) {
    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetInfo =
        appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

    if (appWidgetInfo != null) {
        AndroidView(
            factory = {
                appWidgetHost.createView(
                    appWidgetId = data.appWidgetId,
                    appWidgetProviderInfo = appWidgetInfo,
                ).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    )

                    setAppWidget(appWidgetId, appWidgetInfo)
                }
            },
            modifier = modifier,
        )
    } else {
        AsyncImage(
            model = data.preview,
            contentDescription = null,
            modifier = modifier,
        )
    }
}

@Composable
fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ApplicationInfo,
    color: Color,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = data.icon,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp, 40.dp)
                .weight(1f),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = data.label.toString(),
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

@Composable
fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutInfo,
    color: Color,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = data.icon,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp, 40.dp)
                .weight(1f),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = data.shortLabel,
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

@Composable
fun FolderGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Folder,
    color: Color,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FlowRow(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            maxItemsInEachRow = 2,
        ) {
            data.gridItems.forEach { gridItem ->
                when (val currentData = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        AsyncImage(
                            model = currentData.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    is GridItemData.Folder -> {

                    }

                    is GridItemData.ShortcutInfo -> {
                        AsyncImage(
                            model = currentData.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    is GridItemData.Widget -> {
                        AsyncImage(
                            model = currentData.preview,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        Text(
            text = data.label,
            color = color,
            textAlign = TextAlign.Center,
            fontSize = TextUnit(
                value = 10f,
                type = TextUnitType.Sp,
            ),
        )
    }
}