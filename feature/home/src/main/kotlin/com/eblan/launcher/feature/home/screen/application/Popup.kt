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
package com.eblan.launcher.feature.home.screen.application

import android.graphics.Rect
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.component.popup.ShortcutInfoMenu
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.ui.local.LocalLauncherApps

@Composable
internal fun PopupApplicationInfoMenu(
    currentPage: Int,
    drag: Drag,
    eblanAppWidgetProviderInfos: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    gridItem: GridItem?,
    gridItemSettings: GridItemSettings,
    gridItems: List<GridItem>,
    hasShortcutHostPermission: Boolean,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    popupIntOffset: IntOffset,
    popupIntSize: IntSize,
    onDismissRequest: () -> Unit,
    onDraggingGridItem: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onWidgets: (EblanApplicationInfoGroup) -> Unit,
) {
    val applicationInfo = gridItem?.data as? GridItemData.ApplicationInfo ?: return

    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

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
            ApplicationInfoMenu(
                currentPage = currentPage,
                drag = drag,
                eblanAppWidgetProviderInfosByPackageName = eblanAppWidgetProviderInfos[applicationInfo.packageName],
                eblanShortcutInfosGroup = eblanShortcutInfosGroup[
                    EblanShortcutInfoByGroup(
                        serialNumber = applicationInfo.serialNumber,
                        packageName = applicationInfo.packageName,
                    ),
                ],
                gridItemSettings = gridItemSettings,
                gridItems = gridItems,
                hasShortcutHostPermission = hasShortcutHostPermission,
                icon = applicationInfo.icon,
                onApplicationInfo = {
                    launcherApps.startAppDetailsActivity(
                        serialNumber = applicationInfo.serialNumber,
                        componentName = applicationInfo.componentName,
                        sourceBounds = Rect(
                            x,
                            y,
                            x + popupIntSize.width,
                            y + popupIntSize.height,
                        ),
                    )

                    onDismissRequest()
                },
                onDraggingGridItem = onDraggingGridItem,
                onEdit = {
                    onDismissRequest()

                    onEditApplicationInfo(
                        applicationInfo.serialNumber,
                        applicationInfo.componentName,
                    )
                },
                onLongPressGridItem = onLongPressGridItem,
                onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                    onTapShortcutInfo(
                        serialNumber,
                        packageName,
                        shortcutId,
                    )

                    onDismissRequest()
                },
                onUpdateGridItemOffset = onUpdateGridItemOffset,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onWidgets = {
                    onWidgets(
                        EblanApplicationInfoGroup(
                            serialNumber = applicationInfo.serialNumber,
                            packageName = applicationInfo.packageName,
                            icon = applicationInfo.icon,
                            label = applicationInfo.label,
                        ),
                    )

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
private fun ApplicationInfoMenu(
    currentPage: Int,
    drag: Drag,
    eblanAppWidgetProviderInfosByPackageName: List<EblanAppWidgetProviderInfo>?,
    eblanShortcutInfosGroup: List<EblanShortcutInfo>?,
    gridItemSettings: GridItemSettings,
    gridItems: List<GridItem>,
    hasShortcutHostPermission: Boolean,
    icon: String?,
    modifier: Modifier = Modifier,
    onApplicationInfo: () -> Unit,
    onDraggingGridItem: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onEdit: () -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onWidgets: () -> Unit,
) {
    Surface(
        modifier = modifier.padding(5.dp),
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (hasShortcutHostPermission &&
                    !eblanShortcutInfosGroup.isNullOrEmpty()
                ) {
                    ShortcutInfoMenu(
                        currentPage = currentPage,
                        drag = drag,
                        eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                        gridItemSettings = gridItemSettings,
                        icon = icon,
                        modifier = modifier,
                        onDraggingGridItem = {
                            onDraggingGridItem(
                                Screen.Drag,
                                gridItems,
                            )
                        },
                        onLongPressGridItem = onLongPressGridItem,
                        onTapShortcutInfo = onTapShortcutInfo,
                        onUpdateGridItemOffset = onUpdateGridItemOffset,
                        onUpdateSharedElementKey = onUpdateSharedElementKey,
                    )

                    Spacer(modifier = Modifier.height(5.dp))
                }

                Row {
                    IconButton(
                        onClick = onApplicationInfo,
                    ) {
                        Icon(
                            imageVector = EblanLauncherIcons.Info,
                            contentDescription = null,
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                    ) {
                        Icon(
                            imageVector = EblanLauncherIcons.Edit,
                            contentDescription = null,
                        )
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
