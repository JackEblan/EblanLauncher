package com.eblan.launcher.feature.home.component

import android.appwidget.AppWidgetHostView
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItemData

@Composable
fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.ApplicationInfo,
) {
    ApplicationInfoGridItemBody(
        modifier = modifier.fillMaxSize(),
        gridItemData = gridItemData,
    )
}

@Composable
fun WidgetGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.Widget,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

    if (appWidgetInfo != null) {
        WidgetGridItemBody(
            modifier = modifier,
            appWidgetHostView = appWidgetHost.createView(
                appWidgetId = gridItemData.appWidgetId,
                appWidgetProviderInfo = appWidgetInfo,
            ).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )

                setAppWidget(appWidgetId, appWidgetInfo)
            },
        )
    }
}

@Composable
fun ApplicationInfoGridItemBody(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.ApplicationInfo,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = gridItemData.icon,
            contentDescription = null,
            modifier = Modifier.size(width = 40.dp, height = 40.dp),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = gridItemData.label,
            textAlign = TextAlign.Center,
            fontSize = TextUnit(value = 10f, type = TextUnitType.Sp),
        )
    }
}

@Composable
fun WidgetGridItemBody(
    modifier: Modifier = Modifier,
    appWidgetHostView: AppWidgetHostView,
) {
    AndroidView(
        factory = {
            appWidgetHostView
        },
        modifier = modifier,
    )
}