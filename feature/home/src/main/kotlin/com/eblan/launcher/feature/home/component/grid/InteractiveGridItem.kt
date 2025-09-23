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
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HorizontalAlignment
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.model.VerticalArrangement
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun InteractiveGridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    textColor: TextColor,
    hasShortcutHostPermission: Boolean,
    drag: Drag,
    iconPackInfoPackageName: String,
    onTapApplicationInfo: (String?) -> Unit,
    onTapShortcutInfo: (
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onTapFolderGridItem: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap?) -> Unit,
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
            ApplicationInfoGridItem(
                modifier = modifier,
                textColor = currentTextColor,
                gridItemSettings = currentGridItemSettings,
                data = data,
                drag = drag,
                iconPackInfoPackageName = iconPackInfoPackageName,
                onTap = {
                    onTapApplicationInfo(data.componentName)
                },
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onDraggingGridItem = onDraggingGridItem,
            )
        }

        is GridItemData.Widget -> {
            WidgetGridItem(
                modifier = modifier,
                data = data,
                drag = drag,
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onDraggingGridItem = onDraggingGridItem,
            )
        }

        is GridItemData.ShortcutInfo -> {
            ShortcutInfoGridItem(
                modifier = modifier,
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
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
                onDraggingGridItem = onDraggingGridItem,
            )
        }

        is GridItemData.Folder -> {
            FolderGridItem(
                modifier = modifier,
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                data = data,
                drag = drag,
                iconPackInfoPackageName = iconPackInfoPackageName,
                onTap = onTapFolderGridItem,
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onDraggingGridItem = onDraggingGridItem,
            )
        }
    }
}

@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    data: GridItemData.ApplicationInfo,
    drag: Drag,
    iconPackInfoPackageName: String,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val context = LocalContext.current

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

    var isLongPressed by remember { mutableStateOf(false) }

    val iconPacksDirectory = File(context.filesDir, FileManager.ICON_PACKS_DIR)

    val iconPackDirectory = File(iconPacksDirectory, iconPackInfoPackageName)

    val iconFile = File(iconPackDirectory, data.packageName)

    val icon = if (iconPackInfoPackageName.isNotEmpty() && iconFile.exists()) {
        iconFile.absolutePath
    } else {
        data.icon
    }

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

    LaunchedEffect(key1 = drag) {
        if (isLongPressed) {
            when (drag) {
                Drag.Dragging -> {
                    onDraggingGridItem()
                }

                Drag.Cancel, Drag.End -> {
                    isLongPressed = false
                }

                else -> Unit
            }
        }
    }

    Column(
        modifier = modifier
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
                        isLongPressed = true

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
            }
            .fillMaxSize(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(icon)
                .addLastModifiedToFileCacheKey(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(iconSizeDp),
        )

        if (gridItemSettings.showLabel) {
            Text(
                text = data.label.toString(),
                color = textColor,
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
    data: GridItemData.Widget,
    drag: Drag,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var isLongPressed by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = drag) {
        if (isLongPressed) {
            when (drag) {
                Drag.Dragging -> {
                    onDraggingGridItem()
                }

                Drag.Cancel, Drag.End -> {
                    isLongPressed = false
                }

                else -> Unit
            }
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
                        isLongPressed = true

                        onLongPress()

                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())
                        }

                        true
                    }
                }
            },
            modifier = modifier
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
                .fillMaxSize(),
        )
    }
}

@Composable
private fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    data: GridItemData.ShortcutInfo,
    drag: Drag,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onDraggingGridItem: () -> Unit,
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

    var isLongPressed by remember { mutableStateOf(false) }

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

    LaunchedEffect(key1 = drag) {
        if (isLongPressed) {
            when (drag) {
                Drag.Dragging -> {
                    onDraggingGridItem()
                }

                Drag.Cancel, Drag.End -> {
                    isLongPressed = false
                }

                else -> Unit
            }
        }
    }

    Column(
        modifier = modifier
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
                        isLongPressed = true

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
            }
            .fillMaxSize(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        AsyncImage(
            model = data.icon,
            contentDescription = null,
            modifier = Modifier
                .size(iconSizeDp),
        )

        if (gridItemSettings.showLabel) {
            Text(
                modifier = Modifier.weight(1f),
                text = data.shortLabel,
                color = textColor,
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
    textColor: Color,
    gridItemSettings: GridItemSettings,
    data: GridItemData.Folder,
    drag: Drag,
    iconPackInfoPackageName: String,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val context = LocalContext.current

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

    var isLongPressed by remember { mutableStateOf(false) }

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

    LaunchedEffect(key1 = drag) {
        if (isLongPressed) {
            when (drag) {
                Drag.Dragging -> {
                    onDraggingGridItem()
                }

                Drag.Cancel, Drag.End -> {
                    isLongPressed = false
                }

                else -> Unit
            }
        }
    }

    Column(
        modifier = modifier
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
                        isLongPressed = true

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
            }
            .fillMaxSize(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (data.gridItems.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(5.dp),
                    )
                    .size(iconSizeDp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.SpaceEvenly,
                maxItemsInEachRow = 2,
                maxLines = 2,
            ) {
                data.gridItems.sortedBy { it.startRow + it.startColumn }.forEach { gridItem ->
                    val gridItemIconSizeDp = with(density) {
                        (gridItemSettings.iconSize * 0.25).toInt().toDp()
                    }

                    val gridItemModifier = Modifier.size(gridItemIconSizeDp)

                    when (val currentData = gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            val iconPacksDirectory =
                                File(context.filesDir, FileManager.ICON_PACKS_DIR)

                            val iconPackDirectory =
                                File(iconPacksDirectory, iconPackInfoPackageName)

                            val iconFile = File(iconPackDirectory, currentData.packageName)

                            val icon =
                                if (iconPackInfoPackageName.isNotEmpty() && iconFile.exists()) {
                                    iconFile.absolutePath
                                } else {
                                    currentData.icon
                                }

                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(icon)
                                    .addLastModifiedToFileCacheKey(true)
                                    .build(),
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
                                tint = textColor,
                            )
                        }
                    }
                }
            }
        } else {
            Icon(
                imageVector = EblanLauncherIcons.Folder,
                contentDescription = null,
                modifier = Modifier.size(iconSizeDp),
                tint = textColor,
            )
        }

        if (gridItemSettings.showLabel) {
            Text(
                text = data.label,
                color = textColor,
                textAlign = TextAlign.Center,
                fontSize = textSizeSp,
                maxLines = maxLines,
            )
        }
    }
}
