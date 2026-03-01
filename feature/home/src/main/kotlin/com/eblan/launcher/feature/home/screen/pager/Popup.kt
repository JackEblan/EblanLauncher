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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.feature.home.component.popup.SettingsPopupPositionProvider
import com.eblan.launcher.feature.home.model.GridItemSource

@Composable
internal fun SettingsPopup(
    popupSettingsIntOffset: IntOffset,
    gridItems: List<GridItem>,
    hasSystemFeatureAppWidgets: Boolean,
    onSettings: () -> Unit,
    onEditPage: (
        gridItems: List<GridItem>,
        associate: Associate,
    ) -> Unit,
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
                onEditPage(
                    gridItems,
                    Associate.Grid,
                )

                onDismissRequest()
            },
            onEditDockPage = {
                onEditPage(
                    gridItems,
                    Associate.Dock,
                )

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
internal fun FolderGridItemPopup(
    modifier: Modifier = Modifier,
    gridItemSource: GridItemSource,
    popupIntOffset: IntOffset,
    popupIntSize: IntSize,
    paddingValues: PaddingValues,
    onEdit: (String) -> Unit,
    onDeleteApplicationInfoGridItem: (ApplicationInfoGridItem) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val gridItemSourceFolder = gridItemSource as? GridItemSource.Folder ?: return

    val density = LocalDensity.current

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }
    val x = popupIntOffset.x - leftPadding

    val y = popupIntOffset.y - topPadding

    Layout(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        awaitRelease()

                        onDismissRequest()
                    },
                )
            }
            .fillMaxSize()
            .padding(paddingValues),
        content = {
            FolderGridItemPopupContent(
                onEdit = {
                    onEdit(gridItemSourceFolder.applicationInfoGridItem.id)

                    onDismissRequest()
                },
                onDelete = {
                    onDeleteApplicationInfoGridItem(gridItemSource.applicationInfoGridItem)

                    onDismissRequest()
                },
            )
        },
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(
            constraints.copy(
                minWidth = 0,
                minHeight = 0,
            ),
        )

        val parentCenterX = x + popupIntSize.width / 2

        val childX = (parentCenterX - placeable.width / 2)
            .coerceIn(0, constraints.maxWidth - placeable.width)

        val topY = y - placeable.height
        val bottomY = y + popupIntSize.height

        val childY = if (topY < 0) bottomY else topY

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(
                x = childX,
                y = childY,
            )
        }
    }
}

@Composable
private fun FolderGridItemPopupContent(
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = modifier.width(IntrinsicSize.Max),
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Row(modifier = modifier) {
                IconButton(
                    onClick = onEdit,
                ) {
                    Icon(imageVector = EblanLauncherIcons.Edit, contentDescription = null)
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
private fun SettingsMenu(
    modifier: Modifier = Modifier,
    hasSystemFeatureAppWidgets: Boolean,
    onSettings: () -> Unit,
    onEditPage: () -> Unit,
    onEditDockPage: () -> Unit,
    onWidgets: () -> Unit,
    onShortcutConfigActivities: () -> Unit,
    onWallpaper: () -> Unit,
) {
    Surface(
        modifier = modifier.width(IntrinsicSize.Max),
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

                Spacer(modifier = Modifier.height(5.dp))

                PopupMenuRow(
                    imageVector = EblanLauncherIcons.Pages,
                    title = "Edit Dock Pages",
                    onClick = onEditDockPage,
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
            .fillMaxWidth()
            .padding(10.dp),
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
