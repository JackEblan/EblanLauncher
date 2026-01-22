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
package com.eblan.launcher.feature.home.component.grid

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HorizontalAlignment
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.model.VerticalArrangement
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.InteractiveGridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    textColor: TextColor,
    hasShortcutHostPermission: Boolean,
    drag: Drag,
    statusBarNotifications: Map<String, Int>,
    isScrollInProgress: Boolean,
    iconPackFilePaths: Map<String, String>,
    screen: Screen,
    onTapApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onTapShortcutConfig: (String) -> Unit,
    onTapFolderGridItem: () -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap?) -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    key(gridItem.id) {
        val currentGridItemSettings = if (gridItem.override) {
            gridItem.gridItemSettings
        } else {
            gridItemSettings
        }

        val currentTextColor = if (gridItem.override) {
            getGridItemTextColor(
                systemTextColor = textColor,
                gridItemTextColor = gridItem.gridItemSettings.textColor,
            )
        } else {
            getSystemTextColor(textColor = textColor)
        }

        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                InteractiveApplicationInfoGridItem(
                    modifier = modifier,
                    textColor = currentTextColor,
                    gridItemSettings = currentGridItemSettings,
                    gridItem = gridItem,
                    data = data,
                    drag = drag,
                    isScrollInProgress = isScrollInProgress,
                    iconPackFilePaths = iconPackFilePaths,
                    screen = screen,
                    onTap = {
                        onTapApplicationInfo(
                            data.serialNumber,
                            data.componentName,
                        )
                    },
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onUpdateImageBitmap = onUpdateImageBitmap,
                    statusBarNotifications = statusBarNotifications,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }

            is GridItemData.Widget -> {
                InteractiveWidgetGridItem(
                    modifier = modifier,
                    gridItem = gridItem,
                    data = data,
                    drag = drag,
                    isScrollInProgress = isScrollInProgress,
                    screen = screen,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onUpdateImageBitmap = onUpdateImageBitmap,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }

            is GridItemData.ShortcutInfo -> {
                InteractiveShortcutInfoGridItem(
                    modifier = modifier,
                    gridItemSettings = currentGridItemSettings,
                    textColor = currentTextColor,
                    gridItem = gridItem,
                    data = data,
                    drag = drag,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    isScrollInProgress = isScrollInProgress,
                    screen = screen,
                    onTap = {
                        onTapShortcutInfo(
                            data.serialNumber,
                            data.packageName,
                            data.shortcutId,
                        )
                    },
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onUpdateImageBitmap = onUpdateImageBitmap,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }

            is GridItemData.Folder -> {
                InteractiveFolderGridItem(
                    modifier = modifier,
                    gridItemSettings = currentGridItemSettings,
                    textColor = currentTextColor,
                    gridItem = gridItem,
                    data = data,
                    drag = drag,
                    isScrollInProgress = isScrollInProgress,
                    iconPackFilePaths = iconPackFilePaths,
                    screen = screen,
                    onTap = onTapFolderGridItem,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onUpdateImageBitmap = onUpdateImageBitmap,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }

            is GridItemData.ShortcutConfig -> {
                InteractiveShortcutConfigGridItem(
                    modifier = modifier,
                    textColor = currentTextColor,
                    gridItemSettings = gridItemSettings,
                    gridItem = gridItem,
                    data = data,
                    drag = drag,
                    isScrollInProgress = isScrollInProgress,
                    screen = screen,
                    onTap = {
                        data.shortcutIntentUri?.let(onTapShortcutConfig)
                    },
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onUpdateImageBitmap = onUpdateImageBitmap,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.InteractiveApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.ApplicationInfo,
    drag: Drag,
    statusBarNotifications: Map<String, Int>,
    isScrollInProgress: Boolean,
    iconPackFilePaths: Map<String, String>,
    screen: Screen,
    onTap: () -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    val horizontalAlignment = when (gridItemSettings.horizontalAlignment) {
        HorizontalAlignment.Start -> Alignment.Start
        HorizontalAlignment.CenterHorizontally -> Alignment.CenterHorizontally
        HorizontalAlignment.End -> Alignment.End
    }

    val verticalArrangement = when (gridItemSettings.verticalArrangement) {
        VerticalArrangement.Top -> Arrangement.Top
        VerticalArrangement.Center -> Arrangement.Center
        VerticalArrangement.Bottom -> Arrangement.Bottom
    }

    val isDragging by remember(key1 = drag) {
        derivedStateOf {
            isLongPress && (drag == Drag.Start || drag == Drag.Dragging)
        }
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging if isLongPress -> {
                onDraggingGridItem()
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }
            }

            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

                            onUpdateSharedElementKey(
                                SharedElementKey(
                                    id = gridItem.id,
                                    screen = screen,
                                ),
                            )

                            isLongPress = true
                        }
                    },
                    onTap = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onTap()
                        }
                    },
                    onPress = {
                        awaitRelease()

                        scale.stop()

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            )
            .fillMaxSize(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (!isDragging) {
            ApplicationInfoGridItemContent(
                modifier = Modifier
                    .sharedElementWithCallerManagedVisibility(
                        rememberSharedContentState(
                            key = SharedElementKey(
                                id = gridItem.id,
                                screen = screen,
                            ),
                        ),
                        visible = !isScrollInProgress && (drag == Drag.Cancel || drag == Drag.End),
                    )
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(graphicsLayer)
                    }
                    .onGloballyPositioned { layoutCoordinates ->
                        intOffset = layoutCoordinates.positionInRoot().round()

                        intSize = layoutCoordinates.size
                    },
                data = data,
                textColor = textColor,
                gridItemSettings = gridItemSettings,
                statusBarNotifications = statusBarNotifications,
                iconPackFilePaths = iconPackFilePaths,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.InteractiveWidgetGridItem(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    data: GridItemData.Widget,
    drag: Drag,
    isScrollInProgress: Boolean,
    screen: Screen,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    val isDragging by remember(key1 = drag) {
        derivedStateOf {
            isLongPress && (drag == Drag.Start || drag == Drag.Dragging)
        }
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging if isLongPress -> {
                onDraggingGridItem()
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }
            }

            else -> Unit
        }
    }

    if (!isDragging) {
        val commonModifier = modifier
            .sharedElementWithCallerManagedVisibility(
                rememberSharedContentState(
                    key = SharedElementKey(
                        id = gridItem.id,
                        screen = screen,
                    ),
                ),
                visible = !isScrollInProgress && (drag == Drag.Cancel || drag == Drag.End),
            )
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
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            )
            .fillMaxSize()

        if (appWidgetInfo != null) {
            AndroidView(
                factory = {
                    appWidgetHost.createView(
                        appWidgetId = data.appWidgetId,
                        appWidgetProviderInfo = appWidgetInfo,
                    ).apply {
                        setOnLongClickListener {
                            scope.launch {
                                scale.animateTo(0.5f)

                                scale.animateTo(1f)

                                onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                                onUpdateGridItemOffset(
                                    intOffset,
                                    intSize,
                                )

                                onUpdateSharedElementKey(
                                    SharedElementKey(
                                        id = gridItem.id,
                                        screen = screen,
                                    ),
                                )

                                isLongPress = true
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.InteractiveShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.ShortcutInfo,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    isScrollInProgress: Boolean,
    screen: Screen,
    onTap: () -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    val horizontalAlignment = when (gridItemSettings.horizontalAlignment) {
        HorizontalAlignment.Start -> Alignment.Start
        HorizontalAlignment.CenterHorizontally -> Alignment.CenterHorizontally
        HorizontalAlignment.End -> Alignment.End
    }

    val verticalArrangement = when (gridItemSettings.verticalArrangement) {
        VerticalArrangement.Top -> Arrangement.Top
        VerticalArrangement.Center -> Arrangement.Center
        VerticalArrangement.Bottom -> Arrangement.Bottom
    }

    val isDragging by remember(key1 = drag) {
        derivedStateOf {
            isLongPress && (drag == Drag.Start || drag == Drag.Dragging)
        }
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging if isLongPress -> {
                onDraggingGridItem()
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }
            }

            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

                            onUpdateSharedElementKey(
                                SharedElementKey(
                                    id = gridItem.id,
                                    screen = screen,
                                ),
                            )

                            isLongPress = true
                        }
                    },
                    onTap = {
                        if (hasShortcutHostPermission && data.isEnabled) {
                            scope.launch {
                                scale.animateTo(0.5f)

                                scale.animateTo(1f)

                                onTap()
                            }
                        }
                    },
                    onPress = {
                        awaitRelease()

                        scale.stop()

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            )
            .fillMaxSize(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (!isDragging) {
            ShortcutInfoGridItemContent(
                modifier = Modifier
                    .sharedElementWithCallerManagedVisibility(
                        rememberSharedContentState(
                            key = SharedElementKey(
                                id = gridItem.id,
                                screen = screen,
                            ),
                        ),
                        visible = !isScrollInProgress && (drag == Drag.Cancel || drag == Drag.End),
                    )
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(graphicsLayer)
                    }
                    .onGloballyPositioned { layoutCoordinates ->
                        intOffset = layoutCoordinates.positionInRoot().round()

                        intSize = layoutCoordinates.size
                    },
                data = data,
                textColor = textColor,
                gridItemSettings = gridItemSettings,
                hasShortcutHostPermission = hasShortcutHostPermission,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.InteractiveFolderGridItem(
    modifier: Modifier = Modifier,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.Folder,
    drag: Drag,
    isScrollInProgress: Boolean,
    iconPackFilePaths: Map<String, String>,
    screen: Screen,
    onTap: () -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    val horizontalAlignment = when (gridItemSettings.horizontalAlignment) {
        HorizontalAlignment.Start -> Alignment.Start
        HorizontalAlignment.CenterHorizontally -> Alignment.CenterHorizontally
        HorizontalAlignment.End -> Alignment.End
    }

    val verticalArrangement = when (gridItemSettings.verticalArrangement) {
        VerticalArrangement.Top -> Arrangement.Top
        VerticalArrangement.Center -> Arrangement.Center
        VerticalArrangement.Bottom -> Arrangement.Bottom
    }

    val isDragging by remember(key1 = drag) {
        derivedStateOf {
            isLongPress && (drag == Drag.Start || drag == Drag.Dragging)
        }
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging if isLongPress -> {
                onDraggingGridItem()
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }
            }

            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

                            onUpdateSharedElementKey(
                                SharedElementKey(
                                    id = gridItem.id,
                                    screen = screen,
                                ),
                            )

                            isLongPress = true
                        }
                    },
                    onTap = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onTap()
                        }
                    },
                    onPress = {
                        awaitRelease()

                        scale.stop()

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            )
            .fillMaxSize(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (!isDragging) {
            FolderGridItemContent(
                modifier = Modifier
                    .sharedElementWithCallerManagedVisibility(
                        rememberSharedContentState(
                            key = SharedElementKey(
                                id = gridItem.id,
                                screen = screen,
                            ),
                        ),
                        visible = !isScrollInProgress && (drag == Drag.Cancel || drag == Drag.End),
                    )
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(graphicsLayer)
                    }
                    .onGloballyPositioned { layoutCoordinates ->
                        intOffset = layoutCoordinates.positionInRoot().round()

                        intSize = layoutCoordinates.size
                    },
                gridItemSettings = gridItemSettings,
                data = data,
                iconPackFilePaths = iconPackFilePaths,
                textColor = textColor,
                screen = screen,
                drag = drag,
                isScrollInProgress = isScrollInProgress,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.InteractiveShortcutConfigGridItem(
    modifier: Modifier = Modifier,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.ShortcutConfig,
    drag: Drag,
    isScrollInProgress: Boolean,
    screen: Screen,
    onTap: () -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    val horizontalAlignment = when (gridItemSettings.horizontalAlignment) {
        HorizontalAlignment.Start -> Alignment.Start
        HorizontalAlignment.CenterHorizontally -> Alignment.CenterHorizontally
        HorizontalAlignment.End -> Alignment.End
    }

    val verticalArrangement = when (gridItemSettings.verticalArrangement) {
        VerticalArrangement.Top -> Arrangement.Top
        VerticalArrangement.Center -> Arrangement.Center
        VerticalArrangement.Bottom -> Arrangement.Bottom
    }

    val isDragging by remember(key1 = drag) {
        derivedStateOf {
            isLongPress && (drag == Drag.Start || drag == Drag.Dragging)
        }
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging if isLongPress -> {
                onDraggingGridItem()
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }
            }

            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

                            onUpdateSharedElementKey(
                                SharedElementKey(
                                    id = gridItem.id,
                                    screen = screen,
                                ),
                            )

                            isLongPress = true
                        }
                    },
                    onTap = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onTap()
                        }
                    },
                    onPress = {
                        awaitRelease()

                        scale.stop()

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            )
            .fillMaxSize(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (!isDragging) {
            ShortcutConfigGridItemContent(
                modifier = Modifier
                    .sharedElementWithCallerManagedVisibility(
                        rememberSharedContentState(
                            key = SharedElementKey(
                                id = gridItem.id,
                                screen = screen,
                            ),
                        ),
                        visible = !isScrollInProgress && (drag == Drag.Cancel || drag == Drag.End),
                    )
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(graphicsLayer)
                    }
                    .onGloballyPositioned { layoutCoordinates ->
                        intOffset = layoutCoordinates.positionInRoot().round()

                        intSize = layoutCoordinates.size
                    },
                data = data,
                textColor = textColor,
                gridItemSettings = gridItemSettings,
            )
        }
    }
}
