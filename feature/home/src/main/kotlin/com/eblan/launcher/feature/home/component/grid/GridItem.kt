package com.eblan.launcher.feature.home.component.grid

import android.content.ClipData
import android.widget.FrameLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
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
import com.eblan.launcher.feature.home.model.Screen


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InteractiveApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    iconSize: Int,
    textColor: Long,
    textSize: Int,
    gridItem: GridItem,
    data: GridItemData.ApplicationInfo,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onDragging: () -> Unit,
) {
    ApplicationInfoGridItem(
        modifier = modifier
            .gridItem(gridItem)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onTap()
                    },
                )
            }
            .dragAndDropSource(
                block = {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            onLongPress()
                        },
                        onDrag = { _, _ ->
                            startTransfer(
                                DragAndDropTransferData(
                                    clipData = ClipData.newPlainText("Screen", Screen.Drag.name),
                                ),
                            )

                            onDragging()
                        },
                    )
                },
            ),
        data = data,
        iconSize = iconSize,
        textColor = textColor,
        textSize = textSize,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InteractiveShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    iconSize: Int,
    textColor: Long,
    textSize: Int,
    gridItem: GridItem,
    data: GridItemData.ShortcutInfo,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onDragging: () -> Unit,
) {
    ShortcutInfoGridItem(
        modifier = modifier
            .gridItem(gridItem)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onTap()
                    },
                )
            }
            .dragAndDropSource(
                block = {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            onLongPress()
                        },
                        onDrag = { _, _ ->
                            startTransfer(
                                DragAndDropTransferData(
                                    clipData = ClipData.newPlainText("Screen", Screen.Drag.name),
                                ),
                            )

                            onDragging()
                        },
                    )
                },
            ),
        data = data,
        iconSize = iconSize,
        textColor = textColor,
        textSize = textSize,

        )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InteractiveWidgetGridItem(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    gridItemData: GridItemData.Widget,
    onLongPress: () -> Unit,
    onDragging: () -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

    Box(modifier = modifier.gridItem(gridItem)) {
        if (appWidgetInfo != null) {
            Box(
                modifier = modifier
                    .dragAndDropSource(
                        drawDragDecoration = {
                            drawRoundRect(
                                color = Color.White,
                                alpha = 0.2f,
                                cornerRadius = CornerRadius(
                                    x = 25f,
                                    y = 25f,
                                ),
                            )
                        },
                        block = {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)

                                down.consume()

                                val longPress = awaitLongPressOrCancellation(pointerId = down.id)

                                if (longPress != null) {
                                    onLongPress()

                                    startTransfer(
                                        DragAndDropTransferData(
                                            clipData = ClipData.newPlainText(
                                                "Screen",
                                                Screen.Drag.name,
                                            ),
                                        ),
                                    )
                                }
                            }
                        },
                    )
                    .matchParentSize(),
            )

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InteractiveFolderGridItem(
    modifier: Modifier = Modifier,
    iconSize: Int,
    textColor: Long,
    textSize: Int,
    gridItem: GridItem,
    data: GridItemData.Folder,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onDragging: () -> Unit,
) {
    FolderGridItem(
        modifier = modifier
            .gridItem(gridItem)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onTap()
                    },
                )
            }
            .dragAndDropSource(
                block = {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            onLongPress()
                        },
                        onDrag = { _, _ ->
                            startTransfer(
                                DragAndDropTransferData(
                                    clipData = ClipData.newPlainText("Screen", Screen.Drag.name),
                                ),
                            )

                            onDragging()
                        },
                    )
                },
            ),
        data = data,
        iconSize = iconSize,
        textColor = textColor,
        textSize = textSize,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InteractiveNestedFolderGridItem(
    modifier: Modifier = Modifier,
    iconSize: Int,
    textColor: Long,
    textSize: Int,
    gridItem: GridItem,
    data: GridItemData.Folder,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onDragging: () -> Unit,
) {
    NestedFolderGridItem(
        modifier = modifier
            .gridItem(gridItem)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onTap()
                    },
                )
            }
            .dragAndDropSource(
                block = {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            onLongPress()
                        },
                        onDrag = { _, _ ->
                            startTransfer(
                                DragAndDropTransferData(
                                    clipData = ClipData.newPlainText("Screen", Screen.Drag.name),
                                ),
                            )

                            onDragging()
                        },
                    )
                },
            ),
        data = data,
        iconSize = iconSize,
        textColor = textColor,
        textSize = textSize,
    )
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
    iconSize: Int,
    textColor: Long,
    textSize: Int,
) {
    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        iconSize.toDp()
    }

    val color = Color(textColor)

    val textSizeSp = with(density) {
        textSize.toSp()
    }

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

@Composable
fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutInfo,
    iconSize: Int,
    textColor: Long,
    textSize: Int,
) {
    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        iconSize.toDp()
    }

    val color = Color(textColor)

    val textSizeSp = with(density) {
        textSize.toSp()
    }

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

@Composable
fun FolderGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Folder,
    iconSize: Int,
    textColor: Long,
    textSize: Int,
) {
    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        iconSize.toDp()
    }

    val color = Color(textColor)

    val textSizeSp = with(density) {
        textSize.toSp()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceAround,
            maxItemsInEachRow = 3,
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

        Text(
            modifier = Modifier.weight(1f),
            text = data.label,
            color = color,
            textAlign = TextAlign.Center,
            fontSize = textSizeSp,
        )
    }
}

@Composable
fun NestedFolderGridItem(
    modifier: Modifier,
    data: GridItemData.Folder,
    iconSize: Int,
    textColor: Long,
    textSize: Int,
) {
    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        iconSize.toDp()
    }

    val color = Color(textColor)

    val textSizeSp = with(density) {
        textSize.toSp()
    }

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