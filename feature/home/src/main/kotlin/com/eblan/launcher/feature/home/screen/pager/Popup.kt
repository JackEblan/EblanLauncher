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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.popup.GridItemPopupPositionProvider
import com.eblan.launcher.feature.home.component.popup.SettingsPopupPositionProvider
import com.eblan.launcher.feature.home.model.EblanShortcutInfoByGroup

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

@Composable
internal fun GridItemPopup(
    gridItem: GridItem,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    eblanShortcutInfos: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    hasShortcutHostPermission: Boolean,
    onEdit: (String) -> Unit,
    onResize: () -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onDismissRequest: () -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
) {
    Popup(
        popupPositionProvider = GridItemPopupPositionProvider(
            x = x,
            y = y,
            width = width,
            height = height,
        ),
        onDismissRequest = onDismissRequest,
        content = {
            GridItemPopupContent(
                eblanShortcutInfos = eblanShortcutInfos,
                gridItem = gridItem,
                hasShortcutHostPermission = hasShortcutHostPermission,
                onEdit = onEdit,
                onDismissRequest = onDismissRequest,
                onResize = onResize,
                onInfo = onInfo,
                onDeleteGridItem = onDeleteGridItem,
                onTapShortcutInfo = onTapShortcutInfo,
            )
        },
    )
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

@Composable
private fun GridItemPopupContent(
    modifier: Modifier = Modifier,
    eblanShortcutInfos: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    gridItem: GridItem,
    hasShortcutHostPermission: Boolean,
    onEdit: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onResize: () -> Unit,
    onInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
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
                onEdit = {
                    onEdit(gridItem.id)

                    onDismissRequest()
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
                onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                    onTapShortcutInfo(
                        serialNumber,
                        packageName,
                        shortcutId,
                    )

                    onDismissRequest()
                },
            )
        }

        is GridItemData.Folder -> {
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

        is GridItemData.ShortcutInfo -> {
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

        is GridItemData.ShortcutConfig -> {
            ShortcutConfigGridItemMenu(
                modifier = modifier,
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

@Composable
private fun ApplicationInfoGridItemMenu(
    modifier: Modifier = Modifier,
    eblanShortcutInfosByPackageName: List<EblanShortcutInfo>?,
    hasShortcutHostPermission: Boolean,
    onEdit: () -> Unit,
    onResize: () -> Unit,
    onInfo: () -> Unit,
    onDelete: () -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
) {
    Surface(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .width(IntrinsicSize.Max),
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (hasShortcutHostPermission && !eblanShortcutInfosByPackageName.isNullOrEmpty()) {
                    eblanShortcutInfosByPackageName.forEach { eblanShortcutInfo ->
                        ListItem(
                            modifier = Modifier.clickable {
                                onTapShortcutInfo(
                                    eblanShortcutInfo.serialNumber,
                                    eblanShortcutInfo.packageName,
                                    eblanShortcutInfo.shortcutId,
                                )
                            },
                            headlineContent = {
                                Text(text = eblanShortcutInfo.shortLabel)
                            },
                            leadingContent = {
                                AsyncImage(
                                    model = eblanShortcutInfo.icon,
                                    contentDescription = null,
                                    modifier = Modifier.height(20.dp),
                                )
                            },
                        )
                    }

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

@Composable
private fun ShortcutConfigGridItemMenu(
    modifier: Modifier = Modifier,
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
                    onClick = onResize,
                ) {
                    Icon(
                        imageVector = EblanLauncherIcons.Resize,
                        contentDescription = null,
                    )
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
