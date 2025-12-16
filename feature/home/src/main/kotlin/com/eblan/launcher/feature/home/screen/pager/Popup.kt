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

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.component.popup.SettingsPopupPositionProvider
import com.eblan.launcher.feature.home.component.popup.ShortcutInfoMenu
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey

@Composable
internal fun SettingsPopup(
    popupSettingsIntOffset: IntOffset,
    gridItems: List<GridItem>,
    hasSystemFeatureAppWidgets: Boolean,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onWidgets: () -> Unit,
    onShortcutConfigActivities: () -> Unit,
    onWallpaper: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Popup(
        popupPositionProvider = SettingsPopupPositionProvider(
            x = popupSettingsIntOffset.x,
            y = popupSettingsIntOffset.y,
        ),
        onDismissRequest = onDismissRequest,
    ) {
        SettingsMenu(
            hasSystemFeatureAppWidgets = hasSystemFeatureAppWidgets,
            onSettings = {
                onSettings()

                onDismissRequest()
            },
            onEditPage = {
                onEditPage(gridItems)

                onDismissRequest()
            },

            onWidgets = {
                onWidgets()

                onDismissRequest()
            },
            onShortcutConfigActivities = {
                onShortcutConfigActivities()

                onDismissRequest()
            },
            onWallpaper = {
                onWallpaper()

                onDismissRequest()
            },
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.GridItemPopup(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    eblanShortcutInfos: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    hasShortcutHostPermission: Boolean,
    currentPage: Int,
    drag: Drag,
    gridItemSettings: GridItemSettings,
    eblanAppWidgetProviderInfos: Map<String, List<EblanAppWidgetProviderInfo>>,
    paddingValues: PaddingValues,
    onEdit: (String) -> Unit,
    onResize: () -> Unit,
    onWidgets: (EblanApplicationInfoGroup) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onInfo: (Long, String) -> Unit,
    onDismissRequest: () -> Unit,
    onTapShortcutInfo: (Long, String, String) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val density = LocalDensity.current

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    Layout(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    awaitRelease()

                    onDismissRequest()
                })
            }
            .fillMaxSize(),
        content = {
            GridItemPopupContent(
                modifier = Modifier.padding(5.dp),
                eblanShortcutInfos = eblanShortcutInfos,
                gridItem = gridItem,
                hasShortcutHostPermission = hasShortcutHostPermission,
                currentPage = currentPage,
                drag = drag,
                gridItemSettings = gridItemSettings,
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                onEdit = onEdit,
                onDismissRequest = onDismissRequest,
                onResize = onResize,
                onInfo = onInfo,
                onWidgets = onWidgets,
                onDeleteGridItem = onDeleteGridItem,
                onTapShortcutInfo = onTapShortcutInfo,
                onLongPressGridItem = onLongPressGridItem,
                onUpdateGridItemOffset = onUpdateGridItemOffset,
                onResetOverlay = onResetOverlay,
                onDraggingGridItem = onDraggingGridItem,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
            )
        },
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(
            constraints.copy(
                minWidth = 0,
                minHeight = 0,
            ),
        )

        val parentCenterX = x + width / 2

        val childX = (parentCenterX - placeable.width / 2)
            .coerceIn(0, constraints.maxWidth - placeable.width)

        val topY = y - placeable.height
        val bottomY = y + height + placeable.height

        val childY = if (topY < topPadding) bottomY else topY

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(childX, childY)
        }
    }
}

@Composable
private fun SettingsMenu(
    modifier: Modifier = Modifier,
    hasSystemFeatureAppWidgets: Boolean,
    onSettings: () -> Unit,
    onEditPage: () -> Unit,
    onWidgets: () -> Unit,
    onShortcutConfigActivities: () -> Unit,
    onWallpaper: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 2.dp,
        content = {
            Column {
                PopupMenuRow(
                    imageVector = EblanLauncherIcons.Settings,
                    title = "Settings",
                    onClick = onSettings,
                )

                Spacer(modifier = Modifier.height(5.dp))

                PopupMenuRow(
                    imageVector = EblanLauncherIcons.Pages,
                    title = "Edit Pages",
                    onClick = onEditPage,
                )

                if (hasSystemFeatureAppWidgets) {
                    Spacer(modifier = Modifier.height(5.dp))

                    PopupMenuRow(
                        imageVector = EblanLauncherIcons.Widgets,
                        title = "Widgets",
                        onClick = onWidgets,
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                PopupMenuRow(
                    imageVector = EblanLauncherIcons.Shortcut,
                    title = "Shortcuts",
                    onClick = onShortcutConfigActivities,
                )

                Spacer(modifier = Modifier.height(5.dp))

                PopupMenuRow(
                    imageVector = EblanLauncherIcons.Image,
                    title = "Wallpaper",
                    onClick = onWallpaper,
                )
            }
        },
    )
}

@Composable
private fun PopupMenuRow(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .width(150.dp)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(text = title)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.GridItemPopupContent(
    modifier: Modifier = Modifier,
    eblanShortcutInfos: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    gridItem: GridItem,
    hasShortcutHostPermission: Boolean,
    currentPage: Int,
    drag: Drag,
    gridItemSettings: GridItemSettings,
    eblanAppWidgetProviderInfos: Map<String, List<EblanAppWidgetProviderInfo>>,
    onEdit: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onResize: () -> Unit,
    onInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onWidgets: (EblanApplicationInfoGroup) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> {
            ApplicationInfoGridItemMenu(
                modifier = modifier,
                eblanShortcutInfosByPackageName = eblanShortcutInfos[
                    EblanShortcutInfoByGroup(
                        serialNumber = data.serialNumber,
                        packageName = data.packageName,
                    ),
                ],
                hasShortcutHostPermission = hasShortcutHostPermission,
                currentPage = currentPage,
                drag = drag,
                icon = data.icon,
                gridItemSettings = gridItemSettings,
                eblanAppWidgetProviderInfosByPackageName = eblanAppWidgetProviderInfos[data.packageName],
                onEdit = {
                    onDismissRequest()

                    onEdit(gridItem.id)
                },
                onResize = {
                    onResize()

                    onDismissRequest()
                },
                onInfo = {
                    onInfo(
                        data.serialNumber,
                        data.componentName,
                    )

                    onDismissRequest()
                },
                onDelete = {
                    onDeleteGridItem(gridItem)

                    onDismissRequest()
                },
                onWidgets = {
                    onWidgets(
                        EblanApplicationInfoGroup(
                            packageName = data.packageName,
                            icon = data.icon,
                            label = data.label,
                        ),
                    )

                    onDismissRequest()
                },
                onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                    onTapShortcutInfo(
                        serialNumber,
                        packageName,
                        shortcutId,
                    )

                    onDismissRequest()
                },
                onLongPressGridItem = onLongPressGridItem,
                onUpdateGridItemOffset = onUpdateGridItemOffset,
                onResetOverlay = onResetOverlay,
                onDraggingGridItem = {
                    onDraggingGridItem()

                    onDismissRequest()
                },
                onUpdateSharedElementKey = onUpdateSharedElementKey,
            )
        }

        is GridItemData.Folder, is GridItemData.ShortcutInfo, is GridItemData.ShortcutConfig -> {
            GridItemMenu(
                modifier = modifier,
                onEdit = {
                    onEdit(gridItem.id)

                    onDismissRequest()
                },
                onResize = {
                    onResize()

                    onDismissRequest()
                },
                onDelete = {
                    onDeleteGridItem(gridItem)

                    onDismissRequest()
                },
            )
        }

        is GridItemData.Widget -> {
            val showResize = data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

            WidgetGridItemMenu(
                modifier = modifier,
                showResize = showResize,
                onResize = {
                    onResize()

                    onDismissRequest()
                },
                onDelete = {
                    onDeleteGridItem(gridItem)

                    onDismissRequest()
                },
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.ApplicationInfoGridItemMenu(
    modifier: Modifier = Modifier,
    eblanShortcutInfosByPackageName: List<EblanShortcutInfo>?,
    hasShortcutHostPermission: Boolean,
    currentPage: Int,
    drag: Drag,
    icon: String?,
    gridItemSettings: GridItemSettings,
    eblanAppWidgetProviderInfosByPackageName: List<EblanAppWidgetProviderInfo>?,
    onEdit: () -> Unit,
    onResize: () -> Unit,
    onInfo: () -> Unit,
    onDelete: () -> Unit,
    onWidgets: () -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (hasShortcutHostPermission &&
                    !eblanShortcutInfosByPackageName.isNullOrEmpty()
                ) {
                    ShortcutInfoMenu(
                        modifier = modifier,
                        currentPage = currentPage,
                        drag = drag,
                        icon = icon,
                        eblanShortcutInfosByPackageName = eblanShortcutInfosByPackageName,
                        gridItemSettings = gridItemSettings,
                        onResetOverlay = onResetOverlay,
                        onTapShortcutInfo = onTapShortcutInfo,
                        onLongPressGridItem = onLongPressGridItem,
                        onUpdateGridItemOffset = onUpdateGridItemOffset,
                        onDraggingGridItem = onDraggingGridItem,
                        onUpdateSharedElementKey = onUpdateSharedElementKey,
                    )

                    Spacer(modifier = Modifier.height(5.dp))
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                    ) {
                        Icon(imageVector = EblanLauncherIcons.Edit, contentDescription = null)
                    }

                    IconButton(
                        onClick = onResize,
                    ) {
                        Icon(imageVector = EblanLauncherIcons.Resize, contentDescription = null)
                    }

                    IconButton(
                        onClick = onInfo,
                    ) {
                        Icon(imageVector = EblanLauncherIcons.Info, contentDescription = null)
                    }

                    IconButton(
                        onClick = onDelete,
                    ) {
                        Icon(imageVector = EblanLauncherIcons.Delete, contentDescription = null)
                    }

                    if (!eblanAppWidgetProviderInfosByPackageName.isNullOrEmpty()) {
                        IconButton(
                            onClick = onWidgets,
                        ) {
                            Icon(
                                imageVector = EblanLauncherIcons.Widgets,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun GridItemMenu(
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onResize: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Row {
                IconButton(
                    onClick = onEdit,
                ) {
                    Icon(imageVector = EblanLauncherIcons.Edit, contentDescription = null)
                }

                IconButton(
                    onClick = onResize,
                ) {
                    Icon(imageVector = EblanLauncherIcons.Resize, contentDescription = null)
                }

                IconButton(
                    onClick = onDelete,
                ) {
                    Icon(imageVector = EblanLauncherIcons.Delete, contentDescription = null)
                }
            }
        },
    )
}

@Composable
private fun WidgetGridItemMenu(
    modifier: Modifier = Modifier,
    showResize: Boolean,
    onResize: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Row {
                if (showResize) {
                    IconButton(
                        onClick = onResize,
                    ) {
                        Icon(
                            imageVector = EblanLauncherIcons.Resize,
                            contentDescription = null,
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                ) {
                    Icon(
                        imageVector = EblanLauncherIcons.Delete,
                        contentDescription = null,
                    )
                }
            }
        },
    )
}
