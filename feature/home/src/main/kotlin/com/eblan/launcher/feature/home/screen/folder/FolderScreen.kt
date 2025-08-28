package com.eblan.launcher.feature.home.screen.folder

import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.gestures.detectTapGesturesUnConsume
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen
import kotlinx.coroutines.launch

@Composable
fun FolderScreen(
    modifier: Modifier = Modifier,
    startCurrentPage: Int,
    foldersDataById: ArrayDeque<FolderDataById>,
    folderRows: Int,
    folderColumns: Int,
    drag: Drag,
    gridItemSource: GridItemSource?,
    paddingValues: PaddingValues,
    hasShortcutHostPermission: Boolean,
    gridWidth: Int,
    gridHeight: Int,
    onUpdateScreen: (Screen) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
    onResetTargetPage: () -> Unit,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
        intOffset: IntOffset,
    ) -> Unit,
    onDraggingGridItem: (List<GridItem>) -> Unit,
) {
    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val titleHeightDp = 30.dp

    val titleHeightPx = with(density) {
        titleHeightDp.roundToPx()
    }

    LaunchedEffect(key1 = foldersDataById) {
        if (foldersDataById.isEmpty()) {
            onResetTargetPage()

            onUpdateScreen(Screen.Pager)
        }
    }

    BackHandler(foldersDataById.isNotEmpty()) {
        onRemoveLastFolder()
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && gridItemSource != null) {
            onDraggingGridItem(foldersDataById.last().gridItems)
        }
    }

    foldersDataById.forEach { folderDataById ->
        val horizontalPagerState = rememberPagerState(
            initialPage = startCurrentPage,
            pageCount = {
                folderDataById.pageCount
            },
        )

        Surface(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding(),
                    )
                    .fillMaxSize(),
            ) {
                Text(
                    modifier = Modifier.height(titleHeightDp),
                    text = folderDataById.label,
                )

                HorizontalPager(state = horizontalPagerState) { index ->
                    GridLayout(
                        modifier = Modifier
                            .padding(
                                start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
                                end = paddingValues.calculateRightPadding(LayoutDirection.Ltr),
                            )
                            .fillMaxSize(),
                        rows = folderRows,
                        columns = folderColumns,
                    ) {
                        folderDataById.gridItemsByPage[index]?.forEach { gridItem ->
                            val cellWidth = gridWidth / folderColumns

                            val cellHeight = (gridHeight - titleHeightPx) / folderRows

                            val x = gridItem.startColumn * cellWidth

                            val y = gridItem.startRow * cellHeight

                            GridItemContent(
                                gridItem = gridItem,
                                hasShortcutHostPermission = hasShortcutHostPermission,
                                onTapApplicationInfo = launcherApps::startMainActivity,
                                onTapShortcutInfo = launcherApps::startShortcut,
                                onTapFolderGridItem = {
                                    onResetTargetPage()

                                    onAddFolder(gridItem.id)
                                },
                                onLongPress = { imageBitmap ->
                                    val intOffset =
                                        IntOffset(
                                            x = x + leftPadding,
                                            y = y + (topPadding + titleHeightPx),
                                        )

                                    onLongPressGridItem(
                                        index,
                                        GridItemSource.Existing(gridItem = gridItem),
                                        imageBitmap,
                                        intOffset,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridItemContent(
    gridItem: GridItem,
    hasShortcutHostPermission: Boolean,
    onTapApplicationInfo: (String?) -> Unit,
    onTapShortcutInfo: (
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onTapFolderGridItem: () -> Unit,
    onLongPress: (ImageBitmap?) -> Unit,
) {
    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> {
            ApplicationInfoGridItem(
                gridItem = gridItem,
                data = data,
                onTap = {
                    onTapApplicationInfo(data.componentName)
                },
                onLongPress = onLongPress,
            )
        }

        is GridItemData.Widget -> {
            WidgetGridItem(
                gridItem = gridItem,
                data = data,
                onLongPress = onLongPress,
            )
        }

        is GridItemData.ShortcutInfo -> {
            ShortcutInfoGridItem(
                gridItem = gridItem,
                data = data,
                onTap = {
                    if (hasShortcutHostPermission) {
                        onTapShortcutInfo(
                            data.packageName,
                            data.shortcutId,
                        )
                    }
                },
                onLongPress = onLongPress,
            )
        }

        is GridItemData.Folder -> {
            NestedFolderGridItem(
                gridItem = gridItem,
                data = data,
                onTap = onTapFolderGridItem,
                onLongPress = onLongPress,
            )
        }
    }
}

@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    data: GridItemData.ApplicationInfo,
    onTap: () -> Unit,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

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
            .pointerInput(Unit) {
                detectTapGesturesUnConsume(
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onLongPress(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        onTap()
                    },
                )
            }.padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = data.icon,
                contentDescription = null,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = data.label.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun WidgetGridItem(
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

    val scale = remember { Animatable(1f) }

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
                            scope.launch {
                                scale.animateTo(0.5f)

                                scale.animateTo(1f)

                                onLongPress(graphicsLayer.toImageBitmap())
                            }
                        },
                    )
                },
        )
    }
}

@Composable
private fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    data: GridItemData.ShortcutInfo,
    onTap: () -> Unit,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

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
            .pointerInput(Unit) {
                detectTapGesturesUnConsume(
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onLongPress(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        onTap()
                    },
                )
            }.padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = data.icon,
                contentDescription = null,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = data.shortLabel,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun NestedFolderGridItem(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    data: GridItemData.Folder,
    onTap: () -> Unit,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

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
            .pointerInput(Unit) {
                detectTapGesturesUnConsume(
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onLongPress(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        onTap()
                    },
                )
            }.padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = EblanLauncherIcons.Folder,
                contentDescription = null,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = data.label,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}