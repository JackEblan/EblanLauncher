package com.eblan.launcher.feature.home.component.grid

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.TextColor

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun SmallGridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    textColor: Long,
    gridItemSettings: GridItemSettings,
) {
    key(gridItem.id) {
        val currentGridItemSettings = if (gridItem.override) {
            gridItem.gridItemSettings
        } else {
            gridItemSettings
        }

        val currentTextColor = if (gridItem.override) {
            when (gridItem.gridItemSettings.textColor) {
                TextColor.System -> {
                    textColor
                }

                TextColor.Light -> {
                    0xFFFFFFFF
                }

                TextColor.Dark -> {
                    0xFF000000
                }
            }
        } else {
            textColor
        }

        LookaheadScope {
            val gridItemModifier = modifier
                .animateBounds(this)
                .gridItem(gridItem)

            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    ApplicationInfoGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        textColor = currentTextColor,
                        gridItemSettings = currentGridItemSettings,
                    )
                }

                is GridItemData.Widget -> {
                    WidgetGridItem(modifier = gridItemModifier, data = data)
                }

                is GridItemData.ShortcutInfo -> {
                    ShortcutInfoGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        textColor = currentTextColor,
                        gridItemSettings = currentGridItemSettings,
                    )
                }

                is GridItemData.Folder -> {
                    FolderGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        textColor = currentTextColor,
                        gridItemSettings = currentGridItemSettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ApplicationInfo,
    textColor: Long,
    gridItemSettings: GridItemSettings,
) {
    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        gridItemSettings.iconSize.toDp()
    }

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = data.icon,
            contentDescription = null,
            modifier = Modifier.size(iconSizeDp),
        )

        if (gridItemSettings.showLabel) {
            Text(
                text = data.label.toString(),
                color = Color(textColor),
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Widget,
) {
    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

    if (appWidgetInfo != null) {
        AndroidView(
            factory = {
                appWidgetHost.createView(
                    appWidgetId = data.appWidgetId,
                    appWidgetProviderInfo = appWidgetInfo,
                    minWidth = data.minWidth,
                    minHeight = data.minHeight,
                )
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
private fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutInfo,
    textColor: Long,
    gridItemSettings: GridItemSettings,
) {
    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        gridItemSettings.iconSize.toDp()
    }

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = data.icon,
            contentDescription = null,
            modifier = Modifier.size(iconSizeDp),
        )

        if (gridItemSettings.showLabel) {
            Text(
                modifier = Modifier.weight(1f),
                text = data.shortLabel,
                color = Color(textColor),
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun FolderGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Folder,
    textColor: Long,
    gridItemSettings: GridItemSettings,
) {
    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        gridItemSettings.iconSize.toDp()
    }

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FlowRow(
            maxItemsInEachRow = 2,
            maxLines = 2,
        ) {
            if (data.gridItems.isNotEmpty()) {
                data.gridItems.sortedBy { it.startRow + it.startColumn }.forEach { gridItem ->
                    val gridItemModifier = Modifier
                        .padding(2.dp)
                        .size(10.dp)

                    when (val currentData = gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            AsyncImage(
                                model = currentData.icon,
                                contentDescription = null,
                                modifier = gridItemModifier,
                            )
                        }

                        is GridItemData.ShortcutInfo -> {
                            AsyncImage(
                                model = currentData.icon,
                                contentDescription = null,
                                modifier = gridItemModifier,
                            )
                        }

                        is GridItemData.Widget -> {
                            AsyncImage(
                                model = currentData.preview,
                                contentDescription = null,
                                modifier = gridItemModifier,
                            )
                        }

                        is GridItemData.Folder -> {
                            Icon(
                                imageVector = EblanLauncherIcons.Folder,
                                contentDescription = null,
                                modifier = gridItemModifier,
                                tint = Color(textColor),
                            )
                        }
                    }
                }
            } else {
                Icon(
                    imageVector = EblanLauncherIcons.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(iconSizeDp),
                    tint = Color(textColor),
                )
            }
        }

        if (gridItemSettings.showLabel) {
            Text(
                text = data.label,
                color = Color(textColor),
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}