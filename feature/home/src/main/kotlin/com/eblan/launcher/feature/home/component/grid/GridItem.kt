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

import android.graphics.Paint
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalSettings

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.GridItemContent(
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    iconPackFilePaths: Map<String, String>,
    isDragging: Boolean,
    isScrollInProgress: Boolean,
    modifier: Modifier = Modifier,
    screen: Screen,
    statusBarNotifications: Map<String, Int>,
    textColor: TextColor,
) {
    key(gridItem.id) {
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

        if (isDragging) {
            WhiteBox(
                modifier = modifier,
                textColor = currentTextColor,
            )
        } else {
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    ApplicationInfoGridItem(
                        data = data,
                        drag = drag,
                        gridItem = gridItem,
                        gridItemSettings = currentGridItemSettings,
                        iconPackFilePaths = iconPackFilePaths,
                        isScrollInProgress = isScrollInProgress,
                        modifier = modifier,
                        screen = screen,
                        statusBarNotifications = statusBarNotifications,
                        textColor = currentTextColor,
                    )
                }

                is GridItemData.Widget -> {
                    WidgetGridItem(
                        data = data,
                        drag = drag,
                        gridItem = gridItem,
                        isScrollInProgress = isScrollInProgress,
                        modifier = modifier,
                        screen = screen,
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    ShortcutInfoGridItem(
                        data = data,
                        drag = drag,
                        gridItem = gridItem,
                        gridItemSettings = currentGridItemSettings,
                        hasShortcutHostPermission = hasShortcutHostPermission,
                        isScrollInProgress = isScrollInProgress,
                        modifier = modifier,
                        screen = screen,
                        textColor = currentTextColor,
                    )
                }

                is GridItemData.Folder -> {
                    FolderGridItem(
                        data = data,
                        drag = drag,
                        gridItem = gridItem,
                        gridItemSettings = currentGridItemSettings,
                        iconPackFilePaths = iconPackFilePaths,
                        isScrollInProgress = isScrollInProgress,
                        modifier = modifier,
                        screen = screen,
                        textColor = currentTextColor,
                    )
                }

                is GridItemData.ShortcutConfig -> {
                    ShortcutConfigGridItem(
                        data = data,
                        drag = drag,
                        gridItem = gridItem,
                        gridItemSettings = currentGridItemSettings,
                        isScrollInProgress = isScrollInProgress,
                        modifier = modifier,
                        screen = screen,
                        textColor = currentTextColor,
                    )
                }
            }
        }
    }
}

@Composable
internal fun WhiteBox(
    modifier: Modifier,
    textColor: Color,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .drawBehind {
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            style = Paint.Style.STROKE
                            strokeWidth = 1.5.dp.toPx()

                            color = textColor.copy(alpha = 0.3f).toArgb()

                            setShadowLayer(
                                12.dp.toPx(),
                                0f,
                                0f,
                                textColor.toArgb(),
                            )
                        }

                        drawRoundRect(
                            0f,
                            0f,
                            size.width,
                            size.height,
                            5.dp.toPx(),
                            5.dp.toPx(),
                            paint,
                        )
                    }
                }
                .fillMaxSize()
                .padding(3.dp),
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.ApplicationInfoGridItem(
    data: GridItemData.ApplicationInfo,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    iconPackFilePaths: Map<String, String>,
    isScrollInProgress: Boolean,
    modifier: Modifier = Modifier,
    screen: Screen,
    statusBarNotifications: Map<String, Int>,
    textColor: Color,
) {
    val settings = LocalSettings.current

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(modifier = Modifier.size(gridItemSettings.iconSize.dp)) {
            AsyncImage(
                model = Builder(LocalContext.current).data(data.customIcon ?: icon)
                    .addLastModifiedToFileCacheKey(true).build(),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .sharedElementWithCallerManagedVisibility(
                        rememberSharedContentState(
                            key = SharedElementKey(
                                id = gridItem.id,
                                screen = screen,
                            ),
                        ),
                        visible = !isScrollInProgress && (drag == Drag.Cancel || drag == Drag.End),
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.ShortcutInfoGridItem(
    data: GridItemData.ShortcutInfo,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    isScrollInProgress: Boolean,
    modifier: Modifier = Modifier,
    screen: Screen,
    textColor: Color,
) {
    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = gridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = gridItemSettings.verticalArrangement)

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val customIcon = data.customIcon ?: data.icon

    val customShortLabel = data.customShortLabel ?: data.shortLabel

    val alpha = if (hasShortcutHostPermission && data.isEnabled) 1f else 0.3f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(modifier = Modifier.size(gridItemSettings.iconSize.dp)) {
            AsyncImage(
                model = customIcon,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(alpha)
                    .sharedElementWithCallerManagedVisibility(
                        sharedContentState = rememberSharedContentState(
                            key = SharedElementKey(
                                id = gridItem.id,
                                screen = screen,
                            ),
                        ),
                        visible = !isScrollInProgress && (drag == Drag.Cancel || drag == Drag.End),
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.FolderGridItem(
    data: GridItemData.Folder,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    iconPackFilePaths: Map<String, String>,
    isScrollInProgress: Boolean,
    modifier: Modifier = Modifier,
    screen: Screen,
    textColor: Color,
) {
    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = gridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = gridItemSettings.verticalArrangement)

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val commonModifier = Modifier
        .size(gridItemSettings.iconSize.dp)
        .sharedElementWithCallerManagedVisibility(
            sharedContentState = rememberSharedContentState(
                key = SharedElementKey(
                    id = gridItem.id,
                    screen = screen,
                ),
            ),
            visible = !isScrollInProgress && (drag == Drag.Cancel || drag == Drag.End),
        )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
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
                                                screen = screen,
                                            ),
                                        ),
                                        visible = !isScrollInProgress && (drag == Drag.Cancel || drag == Drag.End),
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.WidgetGridItem(
    data: GridItemData.Widget,
    drag: Drag,
    gridItem: GridItem,
    isScrollInProgress: Boolean,
    modifier: Modifier = Modifier,
    screen: Screen,
) {
    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

    val commonModifier = modifier
        .fillMaxSize()
        .sharedElementWithCallerManagedVisibility(
            rememberSharedContentState(
                key = SharedElementKey(
                    id = gridItem.id,
                    screen = screen,
                ),
            ),
            visible = !isScrollInProgress && (drag == Drag.Cancel || drag == Drag.End),
        )

    if (appWidgetInfo != null) {
        AndroidView(
            factory = {
                appWidgetHost.createView(
                    appWidgetId = data.appWidgetId,
                    appWidgetProviderInfo = appWidgetInfo,
                )
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.ShortcutConfigGridItem(
    data: GridItemData.ShortcutConfig,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    isScrollInProgress: Boolean,
    modifier: Modifier = Modifier,
    screen: Screen,
    textColor: Color,
) {
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(modifier = Modifier.size(gridItemSettings.iconSize.dp)) {
            AsyncImage(
                model = Builder(LocalContext.current).data(icon)
                    .addLastModifiedToFileCacheKey(true).build(),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .sharedElementWithCallerManagedVisibility(
                        sharedContentState = rememberSharedContentState(
                            key = SharedElementKey(
                                id = gridItem.id,
                                screen = screen,
                            ),
                        ),
                        visible = !isScrollInProgress && (drag == Drag.Cancel || drag == Drag.End),
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
