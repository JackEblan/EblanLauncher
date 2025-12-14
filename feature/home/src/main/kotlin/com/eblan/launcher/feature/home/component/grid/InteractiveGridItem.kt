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

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import kotlinx.coroutines.launch

@Composable
internal fun InteractiveGridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    textColor: TextColor,
    hasShortcutHostPermission: Boolean,
    drag: Drag,
    iconPackInfoPackageName: String,
    statusBarNotifications: Map<String, Int>,
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
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap?) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
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
                data = data,
                drag = drag,
                iconPackInfoPackageName = iconPackInfoPackageName,
                onTap = {
                    onTapApplicationInfo(
                        data.serialNumber,
                        data.componentName,
                    )
                },
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onResetOverlay = onResetOverlay,
                statusBarNotifications = statusBarNotifications,
                onDraggingGridItem = onDraggingGridItem,
            )
        }

        is GridItemData.Widget -> {
            InteractiveWidgetGridItem(
                modifier = modifier,
                data = data,
                drag = drag,
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onResetOverlay = onResetOverlay,
                onDraggingGridItem = onDraggingGridItem,
            )
        }

        is GridItemData.ShortcutInfo -> {
            InteractiveShortcutInfoGridItem(
                modifier = modifier,
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                data = data,
                drag = drag,
                hasShortcutHostPermission = hasShortcutHostPermission,
                onTap = {
                    onTapShortcutInfo(
                        data.serialNumber,
                        data.packageName,
                        data.shortcutId,
                    )
                },
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onResetOverlay = onResetOverlay,
                onDraggingGridItem = onDraggingGridItem,
            )
        }

        is GridItemData.Folder -> {
            InteractiveFolderGridItem(
                modifier = modifier,
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                data = data,
                drag = drag,
                iconPackInfoPackageName = iconPackInfoPackageName,
                onTap = onTapFolderGridItem,
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onResetOverlay = onResetOverlay,
                onDraggingGridItem = onDraggingGridItem,
            )
        }

        is GridItemData.ShortcutConfig -> {
            InteractiveShortcutConfigGridItem(
                modifier = modifier,
                textColor = currentTextColor,
                gridItemSettings = gridItemSettings,
                data = data,
                drag = drag,
                onTap = {
                    data.shortcutIntentUri?.let(onTapShortcutConfig)
                },
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onResetOverlay = onResetOverlay,
                onDraggingGridItem = onDraggingGridItem,
            )
        }
    }
}

@Composable
private fun InteractiveApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    data: GridItemData.ApplicationInfo,
    drag: Drag,
    iconPackInfoPackageName: String,
    statusBarNotifications: Map<String, Int>,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var alpha by remember { mutableFloatStateOf(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging -> {
                if (isLongPress) {
                    onDraggingGridItem()
                }
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                alpha = 1f

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }

                onResetOverlay()
            }

            else -> Unit
        }
    }

    ApplicationInfoGridItem(
        modifier = modifier
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onLongPress()

                            isLongPress = true

                            alpha = 0f
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

                        alpha = 1f

                        onResetOverlay()

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .alpha(alpha)
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            )
            .fillMaxSize(),
        data = data,
        textColor = textColor,
        gridItemSettings = gridItemSettings,
        iconPackInfoPackageName = iconPackInfoPackageName,
        statusBarNotifications = statusBarNotifications,
    )
}

@Composable
private fun InteractiveWidgetGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Widget,
    drag: Drag,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var alpha by remember { mutableFloatStateOf(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging -> {
                if (isLongPress) {
                    onDraggingGridItem()
                }
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                alpha = 1f

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }

                onResetOverlay()
            }

            else -> Unit
        }
    }

    if (appWidgetInfo != null) {
        AndroidView(
            factory = {
                appWidgetHost.createView(
                    appWidgetId = data.appWidgetId,
                    appWidgetProviderInfo = appWidgetInfo,
                    minWidth = data.minWidth,
                    minHeight = data.minHeight,
                ).apply {
                    setOnLongClickListener {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onLongPress()

                            isLongPress = true

                            alpha = 0f
                        }

                        true
                    }
                }
            },
            modifier = modifier
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .alpha(alpha)
                .scale(
                    scaleX = scale.value,
                    scaleY = scale.value,
                )
                .fillMaxSize(),
        )
    } else {
        AsyncImage(
            model = data.preview ?: data.icon,
            contentDescription = null,
            modifier = modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun InteractiveShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    data: GridItemData.ShortcutInfo,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    val defaultAlpha = if (hasShortcutHostPermission && data.isEnabled) 1f else 0.3f

    var alpha by remember(key1 = defaultAlpha) {
        mutableFloatStateOf(defaultAlpha)
    }

    var isLongPress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging -> {
                if (isLongPress) {
                    onDraggingGridItem()
                }
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                alpha = defaultAlpha

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }

                onResetOverlay()
            }

            else -> Unit
        }
    }

    ShortcutInfoGridItem(
        modifier = modifier
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onLongPress()

                            isLongPress = true

                            alpha = 0f
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

                        alpha = defaultAlpha

                        onResetOverlay()

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .alpha(alpha)
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            )
            .fillMaxSize(),
        data = data,
        textColor = textColor,
        gridItemSettings = gridItemSettings,
    )
}

@Composable
private fun InteractiveFolderGridItem(
    modifier: Modifier = Modifier,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    data: GridItemData.Folder,
    drag: Drag,
    iconPackInfoPackageName: String,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var alpha by remember { mutableFloatStateOf(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging -> {
                if (isLongPress) {
                    onDraggingGridItem()
                }
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                alpha = 1f

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }

                onResetOverlay()
            }

            else -> Unit
        }
    }

    FolderGridItem(
        modifier = modifier
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onLongPress()

                            isLongPress = true

                            alpha = 0f
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

                        alpha = 1f

                        onResetOverlay()

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .alpha(alpha)
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            )
            .fillMaxSize(),
        data = data,
        textColor = textColor,
        gridItemSettings = gridItemSettings,
        iconPackInfoPackageName = iconPackInfoPackageName,
    )
}

@Composable
private fun InteractiveShortcutConfigGridItem(
    modifier: Modifier = Modifier,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    data: GridItemData.ShortcutConfig,
    drag: Drag,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var alpha by remember { mutableFloatStateOf(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging -> {
                if (isLongPress) {
                    onDraggingGridItem()
                }
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                alpha = 1f

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }

                onResetOverlay()
            }

            else -> Unit
        }
    }

    ShortcutConfigGridItem(
        modifier = modifier
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onLongPress()

                            isLongPress = true

                            alpha = 0f
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

                        alpha = 1f

                        onResetOverlay()

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .alpha(alpha)
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            )
            .fillMaxSize(),
        data = data,
        textColor = textColor,
        gridItemSettings = gridItemSettings,
    )
}
