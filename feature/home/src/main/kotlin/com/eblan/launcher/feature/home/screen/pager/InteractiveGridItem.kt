/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.feature.home.screen.pager

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest.Builder
import coil3.request.addLastModifiedToFileCacheKey
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.modifier.onDoubleTap
import com.eblan.launcher.feature.home.component.modifier.swipeGestures
import com.eblan.launcher.feature.home.component.modifier.whiteBox
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.model.SharedElementKeyParent
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.InteractiveGridItemContent(
    modifier: Modifier = Modifier,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    iconPackFilePaths: Map<String, String>,
    isScrollInProgress: Boolean,
    statusBarNotifications: Map<String, Int>,
    textColor: TextColor,
    gridItemSource: GridItemSource?,
    isLongPress: Boolean,
    onDraggingGridItem: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onTapApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onTapFolderGridItem: () -> Unit,
    onTapShortcutConfig: (String) -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateIsLongPress: (Boolean) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
) {
    val isSelected = gridItemSource != null && gridItem.id == gridItemSource.gridItem.id

    val currentGridItemSettings = if (gridItem.override) {
        gridItem.gridItemSettings
    } else {
        gridItemSettings
    }

    val currentTextColor = if (gridItem.override) {
        getGridItemTextColor(
            gridItemCustomTextColor = gridItem.gridItemSettings.customTextColor,
            gridItemTextColor = gridItem.gridItemSettings.textColor,
            systemCustomTextColor = gridItemSettings.customTextColor,
            systemTextColor = textColor,
        )
    } else {
        getSystemTextColor(
            systemCustomTextColor = gridItemSettings.customTextColor,
            systemTextColor = textColor,
        )
    }

    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> {
            InteractiveApplicationInfoGridItem(
                modifier = modifier,
                data = data,
                drag = drag,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                iconPackFilePaths = iconPackFilePaths,
                isScrollInProgress = isScrollInProgress,
                statusBarNotifications = statusBarNotifications,
                textColor = currentTextColor,
                isSelected = isSelected,
                isLongPress = isLongPress,
                onDraggingGridItem = onDraggingGridItem,
                onOpenAppDrawer = onOpenAppDrawer,
                onTapApplicationInfo = onTapApplicationInfo,
                onUpdateGridItemOffset = onUpdateGridItemOffset,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateIsLongPress = onUpdateIsLongPress,
                onUpdateIsDragging = onUpdateIsDragging,
                )
        }

        is GridItemData.Widget -> {
            InteractiveWidgetGridItem(
                modifier = modifier,
                data = data,
                drag = drag,
                gridItem = gridItem,
                isScrollInProgress = isScrollInProgress,
                isSelected = isSelected,
                isLongPress = isLongPress,
                textColor = currentTextColor,
                onDraggingGridItem = onDraggingGridItem,
                onUpdateGridItemOffset = onUpdateGridItemOffset,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateIsLongPress = onUpdateIsLongPress,
                onUpdateIsDragging = onUpdateIsDragging,
            )
        }

        is GridItemData.ShortcutInfo -> {
            InteractiveShortcutInfoGridItem(
                modifier = modifier,
                data = data,
                drag = drag,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                hasShortcutHostPermission = hasShortcutHostPermission,
                isScrollInProgress = isScrollInProgress,
                textColor = currentTextColor,
                isSelected = isSelected,
                isLongPress = isLongPress,
                onDraggingGridItem = onDraggingGridItem,
                onOpenAppDrawer = onOpenAppDrawer,
                onTapShortcutInfo = onTapShortcutInfo,
                onUpdateGridItemOffset = onUpdateGridItemOffset,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateIsLongPress = onUpdateIsLongPress,
                onUpdateIsDragging = onUpdateIsDragging,
            )
        }

        is GridItemData.Folder -> {
            InteractiveFolderGridItem(
                modifier = modifier,
                data = data,
                drag = drag,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                iconPackFilePaths = iconPackFilePaths,
                isScrollInProgress = isScrollInProgress,
                textColor = currentTextColor,
                isSelected = isSelected,
                isLongPress = isLongPress,
                onDraggingGridItem = onDraggingGridItem,
                onOpenAppDrawer = onOpenAppDrawer,
                onTap = onTapFolderGridItem,
                onUpdateGridItemOffset = onUpdateGridItemOffset,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateIsLongPress = onUpdateIsLongPress,
                onUpdateIsDragging = onUpdateIsDragging,
            )
        }

        is GridItemData.ShortcutConfig -> {
            InteractiveShortcutConfigGridItem(
                modifier = modifier,
                data = data,
                drag = drag,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                isScrollInProgress = isScrollInProgress,
                textColor = currentTextColor,
                isSelected = isSelected,
                isLongPress = isLongPress,
                onDraggingGridItem = onDraggingGridItem,
                onOpenAppDrawer = onOpenAppDrawer,
                onTapShortcutConfig = onTapShortcutConfig,
                onUpdateGridItemOffset = onUpdateGridItemOffset,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateIsLongPress = onUpdateIsLongPress,
                onUpdateIsDragging = onUpdateIsDragging,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.InteractiveApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ApplicationInfo,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    iconPackFilePaths: Map<String, String>,
    isScrollInProgress: Boolean,
    statusBarNotifications: Map<String, Int>,
    textColor: Color,
    isSelected: Boolean,
    isLongPress: Boolean,
    onDraggingGridItem: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onTapApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateIsLongPress: (Boolean) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
) {
    val launcherApps = LocalLauncherApps.current

    val context = LocalContext.current

    val settings = LocalSettings.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = gridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = gridItemSettings.verticalArrangement)

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val icon = iconPackFilePaths[data.componentName] ?: data.icon

    val hasNotifications =
        statusBarNotifications[data.packageName] != null && (
                statusBarNotifications[data.packageName]
                    ?: 0
                ) > 0

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && isSelected && isLongPress) {
            onUpdateIsDragging(true)

            onDraggingGridItem()
        }
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onDoubleTap = onDoubleTap(
                        doubleTap = gridItem.doubleTap,
                        launcherApps = launcherApps,
                        context = context,
                        onOpenAppDrawer = onOpenAppDrawer,
                    ),
                    onLongPress = {
                        scope.launch {
                            onUpdateGridItemSource(GridItemSource.Existing(gridItem = gridItem))

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

                            onUpdateSharedElementKey(
                                SharedElementKey(
                                    id = gridItem.id,
                                    parent = SharedElementKeyParent.Grid,
                                ),
                            )

                            onUpdateIsLongPress(true)
                        }
                    },
                    onTap = {
                        scope.launch {
                            onTapApplicationInfo(
                                data.serialNumber,
                                data.componentName,
                            )
                        }
                    },
                )
            }
            .swipeGestures(
                swipeUp = gridItem.swipeUp,
                swipeDown = gridItem.swipeDown,
                onOpenAppDrawer = onOpenAppDrawer,
            )
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .whiteBox(
                visible = isSelected && drag == Drag.Dragging,
                textColor = textColor,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (!(isSelected && isLongPress && (drag == Drag.Start || drag == Drag.Dragging))) {
            Box(modifier = Modifier.size(gridItemSettings.iconSize.dp)) {
                AsyncImage(
                    model = Builder(LocalContext.current).data(data.customIcon ?: icon)
                        .addLastModifiedToFileCacheKey(true).build(),
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }

                            drawLayer(graphicsLayer)
                        }
                        .onGloballyPositioned { layoutCoordinates ->
                            intOffset = layoutCoordinates.positionInRoot().round()

                            intSize = layoutCoordinates.size
                        }
                        .sharedElementWithCallerManagedVisibility(
                            rememberSharedContentState(
                                key = SharedElementKey(
                                    id = gridItem.id,
                                    parent = SharedElementKeyParent.Grid,
                                ),
                            ),
                            visible = !isScrollInProgress,
                        ),
                )

                if (settings.isNotificationAccessGranted() && hasNotifications) {
                    Box(
                        modifier = Modifier
                            .size((gridItemSettings.iconSize * 0.4).dp)
                            .align(Alignment.TopEnd)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                    )
                }

                if (data.serialNumber != 0L) {
                    ElevatedCard(
                        modifier = Modifier
                            .size((gridItemSettings.iconSize * 0.4).dp)
                            .align(Alignment.BottomEnd),
                    ) {
                        Icon(
                            imageVector = EblanLauncherIcons.Work,
                            contentDescription = null,
                            modifier = Modifier.padding(2.dp),
                        )
                    }
                }
            }

            if (gridItemSettings.showLabel) {
                Text(
                    text = data.customLabel ?: data.label,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = maxLines,
                    fontSize = gridItemSettings.textSize.sp,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.InteractiveWidgetGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Widget,
    drag: Drag,
    gridItem: GridItem,
    isScrollInProgress: Boolean,
    isSelected: Boolean,
    textColor: Color,
    isLongPress: Boolean,
    onDraggingGridItem: () -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsLongPress: (Boolean) -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && isSelected && isLongPress) {
            onUpdateIsDragging(true)

            onDraggingGridItem()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .whiteBox(
                visible = isSelected && drag == Drag.Dragging,
                textColor = textColor,
            ),
    ) {
        if (!(isSelected && isLongPress && (drag == Drag.Start || drag == Drag.Dragging))) {
            val commonModifier = Modifier
                .matchParentSize()
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .onGloballyPositioned { layoutCoordinates ->
                    intOffset = layoutCoordinates.positionInRoot().round()

                    intSize = layoutCoordinates.size
                }
                .sharedElementWithCallerManagedVisibility(
                    rememberSharedContentState(
                        key = SharedElementKey(
                            id = gridItem.id,
                            parent = SharedElementKeyParent.Grid,
                        ),
                    ),
                    visible = !isScrollInProgress,
                )

            if (appWidgetInfo != null) {
                AndroidView(
                    factory = {
                        appWidgetHost.createView(
                            appWidgetId = data.appWidgetId,
                            appWidgetProviderInfo = appWidgetInfo,
                        ).apply {
                            setOnLongClickListener {
                                scope.launch {
                                    onUpdateGridItemSource(GridItemSource.Existing(gridItem = gridItem))

                                    onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                                    onUpdateGridItemOffset(
                                        intOffset,
                                        intSize,
                                    )

                                    onUpdateSharedElementKey(
                                        SharedElementKey(
                                            id = gridItem.id,
                                            parent = SharedElementKeyParent.Grid,
                                        ),
                                    )

                                    onUpdateIsLongPress(true)
                                }

                                true
                            }
                        }
                    },
                    modifier = commonModifier,
                )
            } else {
                AsyncImage(
                    model = data.preview ?: data.icon,
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.InteractiveShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutInfo,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    isScrollInProgress: Boolean,
    textColor: Color,
    isSelected: Boolean,
    isLongPress: Boolean,
    onDraggingGridItem: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsLongPress: (Boolean) -> Unit,
) {
    val launcherApps = LocalLauncherApps.current

    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = gridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = gridItemSettings.verticalArrangement)

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val customIcon = data.customIcon ?: data.icon

    val customShortLabel = data.customShortLabel ?: data.shortLabel

    val alpha = if (hasShortcutHostPermission && data.isEnabled) 1f else 0.3f

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && isSelected && isLongPress) {
            onUpdateIsDragging(true)

            onDraggingGridItem()
        }
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onDoubleTap = onDoubleTap(
                        doubleTap = gridItem.doubleTap,
                        launcherApps = launcherApps,
                        context = context,
                        onOpenAppDrawer = onOpenAppDrawer,
                    ),
                    onLongPress = {
                        scope.launch {
                            onUpdateGridItemSource(GridItemSource.Existing(gridItem = gridItem))

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

                            onUpdateSharedElementKey(
                                SharedElementKey(
                                    id = gridItem.id,
                                    parent = SharedElementKeyParent.Grid,
                                ),
                            )

                            onUpdateIsLongPress(true)
                        }
                    },
                    onTap = {
                        if (hasShortcutHostPermission && data.isEnabled) {
                            scope.launch {
                                onTapShortcutInfo(
                                    data.serialNumber,
                                    data.packageName,
                                    data.shortcutId,
                                )
                            }
                        }
                    },
                )
            }
            .swipeGestures(
                swipeUp = gridItem.swipeUp,
                swipeDown = gridItem.swipeDown,
                onOpenAppDrawer = onOpenAppDrawer,
            )
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .whiteBox(
                visible = isSelected && drag == Drag.Dragging,
                textColor = textColor,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (!(isSelected && isLongPress && (drag == Drag.Start || drag == Drag.Dragging))) {
            Box(modifier = Modifier.size(gridItemSettings.iconSize.dp)) {
                AsyncImage(
                    model = customIcon,
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(alpha)
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }

                            drawLayer(graphicsLayer)
                        }
                        .onGloballyPositioned { layoutCoordinates ->
                            intOffset = layoutCoordinates.positionInRoot().round()

                            intSize = layoutCoordinates.size
                        }
                        .sharedElementWithCallerManagedVisibility(
                            rememberSharedContentState(
                                key = SharedElementKey(
                                    id = gridItem.id,
                                    parent = SharedElementKeyParent.Grid,
                                ),
                            ),
                            visible = !isScrollInProgress,
                        ),
                    contentDescription = null,
                )

                AsyncImage(
                    model = data.eblanApplicationInfoIcon,
                    modifier = Modifier
                        .size((gridItemSettings.iconSize * 0.25).dp)
                        .align(Alignment.BottomEnd)
                        .alpha(alpha),
                    contentDescription = null,
                )
            }

            if (gridItemSettings.showLabel) {
                Text(
                    modifier = Modifier.alpha(alpha),
                    text = customShortLabel,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = maxLines,
                    fontSize = gridItemSettings.textSize.sp,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.InteractiveFolderGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Folder,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    iconPackFilePaths: Map<String, String>,
    isScrollInProgress: Boolean,
    textColor: Color,
    isSelected: Boolean,
    isLongPress: Boolean,
    onDraggingGridItem: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onTap: () -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsLongPress: (Boolean) -> Unit,
) {
    val launcherApps = LocalLauncherApps.current

    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = gridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = gridItemSettings.verticalArrangement)

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && isSelected && isLongPress) {
            onUpdateIsDragging(true)

            onDraggingGridItem()
        }
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onDoubleTap = onDoubleTap(
                        doubleTap = gridItem.doubleTap,
                        launcherApps = launcherApps,
                        context = context,
                        onOpenAppDrawer = onOpenAppDrawer,
                    ),
                    onLongPress = {
                        scope.launch {
                            onUpdateGridItemSource(GridItemSource.Existing(gridItem = gridItem))

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

                            onUpdateSharedElementKey(
                                SharedElementKey(
                                    id = gridItem.id,
                                    parent = SharedElementKeyParent.Grid,
                                ),
                            )

                            onUpdateIsLongPress(true)
                        }
                    },
                    onTap = {
                        onTap()
                    },
                )
            }
            .swipeGestures(
                swipeUp = gridItem.swipeUp,
                swipeDown = gridItem.swipeDown,
                onOpenAppDrawer = onOpenAppDrawer,
            )
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .whiteBox(
                visible = isSelected && drag == Drag.Dragging,
                textColor = textColor,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (!(isSelected && isLongPress && (drag == Drag.Start || drag == Drag.Dragging))) {
            val commonModifier = Modifier
                .size(gridItemSettings.iconSize.dp)
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .onGloballyPositioned { layoutCoordinates ->
                    intOffset = layoutCoordinates.positionInRoot().round()

                    intSize = layoutCoordinates.size
                }
                .sharedElementWithCallerManagedVisibility(
                    rememberSharedContentState(
                        key = SharedElementKey(
                            id = gridItem.id,
                            parent = SharedElementKeyParent.Grid,
                        ),
                    ),
                    visible = !isScrollInProgress,
                )

            if (data.icon != null) {
                AsyncImage(
                    model = data.icon,
                    contentDescription = null,
                    modifier = commonModifier,
                )
            } else {
                Box(
                    modifier = commonModifier.background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(5.dp),
                    ),
                ) {
                    FlowRow(
                        modifier = Modifier.matchParentSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.SpaceEvenly,
                        maxItemsInEachRow = 3,
                        maxLines = 3,
                    ) {
                        data.previewGridItemsByPage.forEach { applicationInfoFolderGridItem ->
                            key(applicationInfoFolderGridItem.id) {
                                val icon =
                                    iconPackFilePaths[applicationInfoFolderGridItem.componentName]
                                        ?: applicationInfoFolderGridItem.icon

                                AsyncImage(
                                    model = Builder(LocalContext.current)
                                        .data(applicationInfoFolderGridItem.customIcon ?: icon)
                                        .addLastModifiedToFileCacheKey(true).build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size((gridItemSettings.iconSize * 0.25).dp)
                                        .sharedElementWithCallerManagedVisibility(
                                            rememberSharedContentState(
                                                key = SharedElementKey(
                                                    id = applicationInfoFolderGridItem.id,
                                                    parent = SharedElementKeyParent.Grid,
                                                ),
                                            ),
                                            visible = !isScrollInProgress,
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            if (gridItemSettings.showLabel) {
                Text(
                    text = data.label,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = maxLines,
                    fontSize = gridItemSettings.textSize.sp,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.InteractiveShortcutConfigGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutConfig,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    isScrollInProgress: Boolean,
    textColor: Color,
    isSelected: Boolean,
    isLongPress: Boolean,
    onDraggingGridItem: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onTapShortcutConfig: (String) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsLongPress: (Boolean) -> Unit,
) {
    val launcherApps = LocalLauncherApps.current

    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = gridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = gridItemSettings.verticalArrangement)

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val icon = when {
        data.customIcon != null -> {
            data.customIcon
        }

        data.shortcutIntentIcon != null -> {
            data.shortcutIntentIcon
        }

        data.activityIcon != null -> {
            data.activityIcon
        }

        else -> {
            data.applicationIcon
        }
    }

    val label = when {
        data.customLabel != null -> {
            data.customLabel
        }

        data.shortcutIntentName != null -> {
            data.shortcutIntentName
        }

        data.activityLabel != null -> {
            data.activityLabel
        }

        else -> {
            data.applicationLabel
        }
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && isSelected && isLongPress) {
            onUpdateIsDragging(true)

            onDraggingGridItem()
        }
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onDoubleTap = onDoubleTap(
                        doubleTap = gridItem.doubleTap,
                        launcherApps = launcherApps,
                        context = context,
                        onOpenAppDrawer = onOpenAppDrawer,
                    ),
                    onLongPress = {
                        scope.launch {
                            onUpdateGridItemSource(GridItemSource.Existing(gridItem = gridItem))

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

                            onUpdateSharedElementKey(
                                SharedElementKey(
                                    id = gridItem.id,
                                    parent = SharedElementKeyParent.Grid,
                                ),
                            )

                            onUpdateIsLongPress(true)
                        }
                    },
                    onTap = {
                        data.shortcutIntentUri?.let(onTapShortcutConfig)
                    },
                )
            }
            .swipeGestures(
                swipeUp = gridItem.swipeUp,
                swipeDown = gridItem.swipeDown,
                onOpenAppDrawer = onOpenAppDrawer,
            )
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .whiteBox(
                visible = isSelected && drag == Drag.Dragging,
                textColor = textColor,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (!(isSelected && isLongPress && (drag == Drag.Start || drag == Drag.Dragging))) {
            Box(modifier = Modifier.size(gridItemSettings.iconSize.dp)) {
                AsyncImage(
                    model = Builder(LocalContext.current).data(icon)
                        .addLastModifiedToFileCacheKey(true).build(),
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }

                            drawLayer(graphicsLayer)
                        }
                        .onGloballyPositioned { layoutCoordinates ->
                            intOffset = layoutCoordinates.positionInRoot().round()

                            intSize = layoutCoordinates.size
                        }
                        .sharedElementWithCallerManagedVisibility(
                            rememberSharedContentState(
                                key = SharedElementKey(
                                    id = gridItem.id,
                                    parent = SharedElementKeyParent.Grid,
                                ),
                            ),
                            visible = !isScrollInProgress,
                        ),
                )

                if (data.serialNumber != 0L) {
                    ElevatedCard(
                        modifier = Modifier
                            .size((gridItemSettings.iconSize * 0.4).dp)
                            .align(Alignment.BottomEnd),
                    ) {
                        Icon(
                            imageVector = EblanLauncherIcons.Work,
                            contentDescription = null,
                            modifier = Modifier.padding(2.dp),
                        )
                    }
                }
            }

            if (gridItemSettings.showLabel) {
                Text(
                    text = label.toString(),
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = maxLines,
                    fontSize = gridItemSettings.textSize.sp,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
