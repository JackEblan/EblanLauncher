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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoGridItemMenu
import com.eblan.launcher.feature.home.component.menu.GridItemMenu
import com.eblan.launcher.feature.home.component.menu.MenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.SettingsMenu
import com.eblan.launcher.feature.home.component.menu.SettingsMenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.WidgetGridItemMenu

@Composable
internal fun PopupSettingsMenu(
    popupSettingsMenuIntOffset: IntOffset,
    gridItems: List<GridItem>,
    hasSystemFeatureAppWidgets: Boolean,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onWidgets: () -> Unit,
    onWallpaper: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Popup(
        popupPositionProvider = SettingsMenuPositionProvider(
            x = popupSettingsMenuIntOffset.x,
            y = popupSettingsMenuIntOffset.y,
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
            onWallpaper = {
                onWallpaper()

                onDismissRequest()
            },
        )
    }
}

@Composable
internal fun PopupGridItemMenu(
    gridItem: GridItem,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    eblanShortcutInfosByPackageName: List<EblanShortcutInfo>?,
    onEdit: (String) -> Unit,
    onResize: () -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onInfo: (
        serialNumber: Long,
        componentName: String?,
    ) -> Unit,
    onDismissRequest: () -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
) {
    Popup(
        popupPositionProvider = MenuPositionProvider(
            x = x,
            y = y,
            width = width,
            height = height,
        ),
        onDismissRequest = onDismissRequest,
        content = {
            PopupGridItemMenuContent(
                eblanShortcutInfosByPackageName = eblanShortcutInfosByPackageName,
                gridItem = gridItem,
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
private fun PopupGridItemMenuContent(
    modifier: Modifier = Modifier,
    eblanShortcutInfosByPackageName: List<EblanShortcutInfo>?,
    gridItem: GridItem,
    onEdit: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onResize: () -> Unit,
    onInfo: (Long, String?) -> Unit,
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
                eblanShortcutInfosByPackageName = eblanShortcutInfosByPackageName ?: emptyList(),
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
    }
}
