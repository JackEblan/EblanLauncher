package com.eblan.launcher.feature.home.component.grid

import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.eblan.launcher.feature.home.component.gestures.detectTapGesturesUnConsume
import kotlinx.coroutines.launch

@Composable
fun InteractiveApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.ApplicationInfo,
    onTap: () -> Unit,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        gridItemSettings.iconSize.toDp()
    }

    val color = Color(color = textColor)

    val textSizeSp = with(density) {
        gridItemSettings.textSize.toSp()
    }

    Column(
        modifier = modifier
            .gridItem(gridItem)
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(Unit) {
                detectTapGesturesUnConsume(
                    onLongPress = {
                        scope.launch {
                            onLongPress(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        onTap()
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = data.icon,
                contentDescription = null,
                modifier = Modifier.size(iconSizeDp),
            )
        }

        if (gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = data.label.toString(),
                color = color,
                textAlign = TextAlign.Center,
                fontSize = textSizeSp,
            )
        }
    }
}

@Composable
fun InteractiveWidgetGridItem(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    data: GridItemData.Widget,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

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
            modifier = modifier
                .gridItem(gridItem)
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .pointerInput(Unit) {
                    detectTapGesturesUnConsume(
                        requireUnconsumed = false,
                        onLongPress = {
                            scope.launch {
                                onLongPress(graphicsLayer.toImageBitmap())
                            }
                        },
                    )
                },
        )
    }
}

@Composable
fun InteractiveShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.ShortcutInfo,
    onTap: () -> Unit,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        gridItemSettings.iconSize.toDp()
    }

    val color = Color(color = textColor)

    val textSizeSp = with(density) {
        gridItemSettings.textSize.toSp()
    }

    Column(
        modifier = modifier
            .gridItem(gridItem)
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(Unit) {
                detectTapGesturesUnConsume(
                    onLongPress = {
                        scope.launch {
                            onLongPress(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        onTap()
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = data.icon,
                contentDescription = null,
                modifier = Modifier.size(iconSizeDp),
            )
        }

        if (gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = data.shortLabel,
                color = color,
                textAlign = TextAlign.Center,
                fontSize = textSizeSp,
            )
        }
    }
}

@Composable
fun InteractiveFolderGridItem(
    modifier: Modifier = Modifier,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.Folder,
    onTap: () -> Unit,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    val color = Color(color = textColor)

    val textSizeSp = with(density) {
        gridItemSettings.textSize.toSp()
    }

    Column(
        modifier = modifier
            .gridItem(gridItem)
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(Unit) {
                detectTapGesturesUnConsume(
                    onLongPress = {
                        scope.launch {
                            onLongPress(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        onTap()
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            maxItemsInEachRow = 2,
        ) {
            data.gridItems
                .take(6)
                .sortedBy { it.startRow + it.startColumn }
                .forEach { gridItem ->
                    Column {
                        when (val currentData = gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                AsyncImage(
                                    model = currentData.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            is GridItemData.ShortcutInfo -> {
                                AsyncImage(
                                    model = currentData.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            is GridItemData.Widget -> {
                                Icon(
                                    imageVector = EblanLauncherIcons.Widgets,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            is GridItemData.Folder -> {
                                Icon(
                                    imageVector = EblanLauncherIcons.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
        }

        if (gridItemSettings.showLabel) {
            Text(
                modifier = Modifier.weight(1f),
                text = data.label,
                color = color,
                textAlign = TextAlign.Center,
                fontSize = textSizeSp,
            )
        }
    }
}

@Composable
fun InteractiveNestedFolderGridItem(
    modifier: Modifier = Modifier,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.Folder,
    onTap: () -> Unit,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        gridItemSettings.iconSize.toDp()
    }

    val color = Color(color = textColor)

    val textSizeSp = with(density) {
        gridItemSettings.textSize.toSp()
    }

    Column(
        modifier = modifier
            .gridItem(gridItem)
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(Unit) {
                detectTapGesturesUnConsume(
                    onLongPress = {
                        scope.launch {
                            onLongPress(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        onTap()
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = EblanLauncherIcons.Folder,
                contentDescription = null,
                modifier = Modifier.height(iconSizeDp),
            )
        }

        if (gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = data.label,
                color = color,
                textAlign = TextAlign.Center,
                fontSize = textSizeSp,
            )
        }
    }
}

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
    textColor: Long,
    gridItemSettings: GridItemSettings,
) {
    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        gridItemSettings.iconSize.toDp()
    }

    val color = Color(textColor)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = data.icon,
                contentDescription = null,
                modifier = Modifier.size(iconSizeDp),
            )
        }

        if (gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = data.label.toString(),
                color = color,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutInfo,
    textColor: Long,
    gridItemSettings: GridItemSettings,
) {
    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        gridItemSettings.iconSize.toDp()
    }

    val color = Color(textColor)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = data.icon,
                contentDescription = null,
                modifier = Modifier.size(iconSizeDp),
            )
        }

        if (gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = data.shortLabel,
                color = color,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun FolderGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Folder,
    textColor: Long,
    gridItemSettings: GridItemSettings,
) {
    val color = Color(textColor)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            maxItemsInEachRow = 2,
        ) {
            data.gridItems
                .take(6)
                .sortedBy { it.startRow + it.startColumn }
                .forEach { gridItem ->
                    Column {
                        when (val currentData = gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                AsyncImage(
                                    model = currentData.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            is GridItemData.ShortcutInfo -> {
                                AsyncImage(
                                    model = currentData.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            is GridItemData.Widget -> {
                                Icon(
                                    imageVector = EblanLauncherIcons.Widgets,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            is GridItemData.Folder -> {
                                Icon(
                                    imageVector = EblanLauncherIcons.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
        }

        if (gridItemSettings.showLabel) {
            Text(
                modifier = Modifier.weight(1f),
                text = data.label,
                color = color,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun NestedFolderGridItem(
    modifier: Modifier,
    data: GridItemData.Folder,
    textColor: Long,
    gridItemSettings: GridItemSettings,
) {
    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        gridItemSettings.iconSize.toDp()
    }

    val color = Color(textColor)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = EblanLauncherIcons.Folder,
                contentDescription = null,
                modifier = Modifier.height(iconSizeDp),
            )
        }

        if (gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = data.label,
                color = color,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}