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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
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
import com.eblan.launcher.ui.local.LocalSettings
import kotlinx.coroutines.launch
import java.io.File

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
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
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
                onUpdateGridItemOffset = onUpdateGridItemOffset,
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
                onUpdateGridItemOffset = onUpdateGridItemOffset,
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
                onUpdateGridItemOffset = onUpdateGridItemOffset,
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
                onUpdateGridItemOffset = onUpdateGridItemOffset,
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
                onUpdateGridItemOffset = onUpdateGridItemOffset,
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
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var alpha by remember { mutableFloatStateOf(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val settings = LocalSettings.current

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val iconPacksDirectory = File(context.filesDir, FileManager.ICON_PACKS_DIR)

    val iconPackDirectory = File(iconPacksDirectory, iconPackInfoPackageName)

    val iconPackFile = File(
        iconPackDirectory,
        data.componentName.replace("/", "-"),
    )

    val icon = if (iconPackInfoPackageName.isNotEmpty() && iconPackFile.exists()) {
        iconPackFile.absolutePath
    } else {
        data.icon
    }

    val customIcon = data.customIcon ?: icon

    val customLabel = data.customLabel ?: data.label

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

    val hasNotifications =
        statusBarNotifications[data.packageName] != null && statusBarNotifications[data.packageName]!! > 0

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
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(
            modifier = Modifier
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
                .size(gridItemSettings.iconSize.dp),
        ) {
            AsyncImage(
                model = Builder(context).data(customIcon).addLastModifiedToFileCacheKey(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize(),
            )

            if (settings.isNotificationAccessGranted() && hasNotifications) {
                Box(
                    modifier = Modifier
                        .size((gridItemSettings.iconSize * 0.3).dp)
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
                text = customLabel,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun InteractiveWidgetGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Widget,
    drag: Drag,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

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

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

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
                .onGloballyPositioned { layoutCoordinates ->
                    intOffset = layoutCoordinates.positionInRoot().round()

                    intSize = layoutCoordinates.size
                }
                .fillMaxSize(),
        )
    } else {
        AsyncImage(
            model = data.preview ?: data.icon,
            contentDescription = null,
            modifier = modifier
                .onGloballyPositioned { layoutCoordinates ->
                    intOffset = layoutCoordinates.positionInRoot().round()

                    intSize = layoutCoordinates.size
                }
                .fillMaxSize(),
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
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    val defaultAlpha = if (hasShortcutHostPermission && data.isEnabled) 1f else 0.3f

    var alpha by remember(key1 = defaultAlpha) {
        mutableFloatStateOf(defaultAlpha)
    }

    var isLongPress by remember { mutableStateOf(false) }

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

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

    val customIcon = data.customIcon ?: data.icon

    val customShortLabel = data.customShortLabel ?: data.shortLabel

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
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            )
            .fillMaxSize(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(
            modifier = Modifier
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
                .alpha(alpha)
                .size(gridItemSettings.iconSize.dp),
        ) {
            AsyncImage(
                model = customIcon,
                modifier = Modifier.matchParentSize(),
                contentDescription = null,
            )

            AsyncImage(
                model = data.eblanApplicationInfoIcon,
                modifier = Modifier
                    .size((gridItemSettings.iconSize * 0.25).dp)
                    .align(Alignment.BottomEnd),
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

@Composable
private fun InteractiveFolderGridItem(
    modifier: Modifier = Modifier,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    data: GridItemData.Folder,
    drag: Drag,
    iconPackInfoPackageName: String,
    onTap: () -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var alpha by remember { mutableFloatStateOf(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

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
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (data.gridItems.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(graphicsLayer)
                    }
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(5.dp),
                    )
                    .onGloballyPositioned { layoutCoordinates ->
                        intOffset = layoutCoordinates.positionInRoot().round()

                        intSize = layoutCoordinates.size
                    }
                    .size(gridItemSettings.iconSize.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.SpaceEvenly,
                maxItemsInEachRow = 2,
                maxLines = 2,
            ) {
                data.gridItems.sortedBy { it.startRow + it.startColumn }.forEach { gridItem ->
                    val gridItemModifier = Modifier.size((gridItemSettings.iconSize * 0.25).dp)

                    when (val currentData = gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            val iconPacksDirectory = File(
                                context.filesDir,
                                FileManager.ICON_PACKS_DIR,
                            )

                            val iconPackDirectory = File(
                                iconPacksDirectory,
                                iconPackInfoPackageName,
                            )

                            val iconPackFile = File(
                                iconPackDirectory,
                                currentData.componentName.replace("/", "-"),
                            )

                            val icon =
                                if (iconPackInfoPackageName.isNotEmpty() && iconPackFile.exists()) {
                                    iconPackFile.absolutePath
                                } else {
                                    currentData.icon
                                }

                            AsyncImage(
                                model = Builder(context).data(icon)
                                    .addLastModifiedToFileCacheKey(true).build(),
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
                            if (currentData.icon != null) {
                                AsyncImage(
                                    model = currentData.icon,
                                    contentDescription = null,
                                    modifier = gridItemModifier,
                                )
                            } else {
                                Icon(
                                    imageVector = EblanLauncherIcons.Folder,
                                    contentDescription = null,
                                    modifier = gridItemModifier,
                                    tint = textColor,
                                )
                            }
                        }

                        is GridItemData.ShortcutConfig -> {
                            val icon = when {
                                currentData.shortcutIntentIcon != null -> currentData.shortcutIntentIcon
                                currentData.activityIcon != null -> currentData.activityIcon
                                else -> currentData.applicationIcon
                            }

                            AsyncImage(
                                model = currentData.customIcon ?: icon,
                                contentDescription = null,
                                modifier = gridItemModifier,
                            )
                        }
                    }
                }
            }
        } else if (data.icon != null) {
            AsyncImage(
                model = data.icon,
                contentDescription = null,
                modifier = Modifier.size(gridItemSettings.iconSize.dp),
            )
        } else {
            Icon(
                imageVector = EblanLauncherIcons.Folder,
                contentDescription = null,
                modifier = Modifier.size(gridItemSettings.iconSize.dp),
                tint = textColor,
            )
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

@Composable
private fun InteractiveShortcutConfigGridItem(
    modifier: Modifier = Modifier,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    data: GridItemData.ShortcutConfig,
    drag: Drag,
    onTap: () -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var alpha by remember { mutableFloatStateOf(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

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

    val icon = when {
        data.shortcutIntentIcon != null -> data.shortcutIntentIcon
        data.activityIcon != null -> data.activityIcon
        else -> data.applicationIcon
    }

    val label = when {
        data.shortcutIntentName != null -> data.shortcutIntentName
        data.activityLabel != null -> data.activityLabel
        else -> data.applicationLabel
    }

    val customIcon = data.customIcon ?: icon

    val customLabel = data.customLabel ?: label

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
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(
            modifier = Modifier
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
                .size(gridItemSettings.iconSize.dp),
        ) {
            AsyncImage(
                model = Builder(context).data(customIcon).addLastModifiedToFileCacheKey(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
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
                text = customLabel.toString(),
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
