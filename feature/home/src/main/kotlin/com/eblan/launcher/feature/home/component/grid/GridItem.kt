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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.eblan.launcher.domain.model.VerticalArrangement
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalSettings
import java.io.File

@Composable
fun GridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    iconPackInfoPackageName: String,
    isDragging: Boolean,
    hasShortcutHostPermission: Boolean,
    statusBarNotifications: Map<String, Int>,
) {
    key(gridItem.id) {
        if (isDragging) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .border(
                        width = Dp.Hairline,
                        color = textColor,
                        shape = RoundedCornerShape(5.dp),
                    ),
            )
        } else {
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    ApplicationInfoGridItem(
                        modifier = modifier,
                        data = data,
                        textColor = textColor,
                        gridItemSettings = gridItemSettings,
                        iconPackInfoPackageName = iconPackInfoPackageName,
                        statusBarNotifications = statusBarNotifications,
                    )
                }

                is GridItemData.Widget -> {
                    WidgetGridItem(
                        modifier = modifier,
                        data = data,
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    ShortcutInfoGridItem(
                        modifier = modifier,
                        data = data,
                        textColor = textColor,
                        gridItemSettings = gridItemSettings,
                        hasShortcutHostPermission = hasShortcutHostPermission,
                    )
                }

                is GridItemData.Folder -> {
                    FolderGridItem(
                        modifier = modifier,
                        data = data,
                        textColor = textColor,
                        gridItemSettings = gridItemSettings,
                        iconPackInfoPackageName = iconPackInfoPackageName,
                    )
                }
            }
        }
    }
}

@Composable
fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ApplicationInfo,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    iconPackInfoPackageName: String,
    statusBarNotifications: Map<String, Int>,
) {
    val context = LocalContext.current

    val settings = LocalSettings.current

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

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

    val hasNotifications =
        statusBarNotifications[data.packageName] != null && statusBarNotifications[data.packageName]!! > 0

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(modifier = Modifier.size(gridItemSettings.iconSize.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(icon)
                    .addLastModifiedToFileCacheKey(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
            )

            if (settings.isNotificationAccessGranted() && hasNotifications) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
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
                        .size((gridItemSettings.iconSize * 0.40).dp)
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
                text = data.label.toString(),
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
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Widget,
) {
    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

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
            modifier = modifier.fillMaxSize(),
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
fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutInfo,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
) {
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

    val alpha = if (hasShortcutHostPermission || data.isEnabled) 1f else 0.5f

    Column(
        modifier = modifier
            .alpha(alpha)
            .fillMaxSize(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(modifier = Modifier.size(gridItemSettings.iconSize.dp)) {
            AsyncImage(
                model = data.icon,
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
                text = data.shortLabel,
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
fun FolderGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Folder,
    textColor: Color,
    gridItemSettings: GridItemSettings,
    iconPackInfoPackageName: String,
) {
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

    Column(
        modifier = modifier.fillMaxSize(),
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
