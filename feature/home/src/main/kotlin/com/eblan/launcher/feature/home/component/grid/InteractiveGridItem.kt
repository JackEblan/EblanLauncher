package com.eblan.launcher.feature.home.component.grid

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.gestures.detectTapGesturesUnConsume
import com.eblan.launcher.feature.home.model.Drag
import kotlinx.coroutines.launch


@Composable
fun InteractiveGridItemContent(
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    textColor: Long,
    hasShortcutHostPermission: Boolean,
    drag: Drag,
    onTapApplicationInfo: (String?) -> Unit,
    onTapShortcutInfo: (
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onTapFolderGridItem: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap?) -> Unit,
) {
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

    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> {
            ApplicationInfoGridItem(
                textColor = currentTextColor,
                gridItemSettings = currentGridItemSettings,
                gridItem = gridItem,
                data = data,
                drag = drag,
                onTap = {
                    onTapApplicationInfo(data.componentName)
                },
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
            )
        }

        is GridItemData.Widget -> {
            WidgetGridItem(
                gridItem = gridItem,
                data = data,
                drag = drag,
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
            )
        }

        is GridItemData.ShortcutInfo -> {
            ShortcutInfoGridItem(
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                gridItem = gridItem,
                data = data,
                drag = drag,
                onTap = {
                    if (hasShortcutHostPermission) {
                        onTapShortcutInfo(
                            data.packageName,
                            data.shortcutId,
                        )
                    }
                },
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
            )
        }

        is GridItemData.Folder -> {
            FolderGridItem(
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                gridItem = gridItem,
                data = data,
                drag = drag,
                onTap = onTapFolderGridItem,
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
            )
        }
    }
}


@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.ApplicationInfo,
    drag: Drag,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
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

    val scale = remember { Animatable(1f) }

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    Column(
        modifier = modifier
            .gridItem(gridItem)
            .drawWithContent {
                graphicsLayer.record {
                    drawContext.transform.scale(
                        scaleX = scale.value,
                        scaleY = scale.value,
                    )

                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        onLongPress()

                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onTap()
                        }
                    },
                )
            },
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
                color = color,
                textAlign = TextAlign.Center,
                fontSize = textSizeSp,
                maxLines = maxLines,
            )
        }
    }
}

@Composable
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    data: GridItemData.Widget,
    drag: Drag,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

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
            modifier = modifier
                .gridItem(gridItem)
                .drawWithContent {
                    graphicsLayer.record {
                        drawContext.transform.scale(
                            scaleX = scale.value,
                            scaleY = scale.value,
                        )

                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .pointerInput(Unit) {
                    detectTapGesturesUnConsume(
                        requireUnconsumed = false,
                        onLongPress = {
                            onLongPress()

                            scope.launch {
                                scale.animateTo(0.5f)

                                scale.animateTo(1f)

                                onUpdateImageBitmap(graphicsLayer.toImageBitmap())
                            }
                        },
                    )
                },
            update = { appWidgetHostView ->
                if (drag == Drag.Start) {
                    appWidgetHostView.isPressed = false
                }
            },
        )
    }
}

@Composable
private fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.ShortcutInfo,
    drag: Drag,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
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

    val scale = remember { Animatable(1f) }

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    Column(
        modifier = modifier
            .gridItem(gridItem)
            .drawWithContent {
                graphicsLayer.record {
                    drawContext.transform.scale(
                        scaleX = scale.value,
                        scaleY = scale.value,
                    )

                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        onLongPress()

                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onTap()
                        }
                    },
                )
            },
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
                color = color,
                textAlign = TextAlign.Center,
                fontSize = textSizeSp,
                maxLines = maxLines,
            )
        }
    }
}

@Composable
private fun FolderGridItem(
    modifier: Modifier = Modifier,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.Folder,
    drag: Drag,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        gridItemSettings.iconSize.toDp()
    }

    val textSizeSp = with(density) {
        gridItemSettings.textSize.toSp()
    }

    val scale = remember { Animatable(1f) }

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    Column(
        modifier = modifier
            .gridItem(gridItem)
            .drawWithContent {
                graphicsLayer.record {
                    drawContext.transform.scale(
                        scaleX = scale.value,
                        scaleY = scale.value,
                    )

                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        onLongPress()

                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onTap()
                        }
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (data.gridItems.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.size(iconSizeDp),
                maxItemsInEachRow = 2,
                maxLines = 2,
            ) {
                data.gridItems.sortedBy { it.startRow + it.startColumn }
                    .forEach { gridItem ->
                        Column {
                            val gridItemModifier = Modifier
                                .padding(2.dp)
                                .size(20.dp)

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
                    }
            }
        } else {
            Icon(
                imageVector = EblanLauncherIcons.Folder,
                contentDescription = null,
                modifier = Modifier.size(iconSizeDp),
                tint = Color(color = textColor),
            )
        }

        if (gridItemSettings.showLabel) {
            Text(
                text = data.label,
                color = Color(color = textColor),
                textAlign = TextAlign.Center,
                fontSize = textSizeSp,
                maxLines = maxLines,
            )
        }
    }
}