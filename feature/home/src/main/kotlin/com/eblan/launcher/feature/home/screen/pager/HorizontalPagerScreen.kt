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

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.InteractiveGridItemContent
import com.eblan.launcher.feature.home.component.indicator.Chevron
import com.eblan.launcher.feature.home.component.indicator.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanShortcutInfoByGroup
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.handleWallpaperScroll
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalWallpaperManager

@Composable
internal fun HorizontalPagerScreen(
    modifier: Modifier = Modifier,
    gridHorizontalPagerState: PagerState,
    currentPage: Int,
    isApplicationComponentVisible: Boolean,
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    gridWidth: Int,
    gridHeight: Int,
    paddingValues: PaddingValues,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    gridItemSource: GridItemSource?,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    hasSystemFeatureAppWidgets: Boolean,
    homeSettings: HomeSettings,
    iconPackInfoPackageName: String,
    statusBarNotifications: Map<String, Int>,
    eblanShortcutInfos: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    gestureSettings: GestureSettings,
    swipeY: Float,
    screenHeight: Int,
    onTapFolderGridItem: (String) -> Unit,
    onEdit: (String) -> Unit,
    onResize: () -> Unit,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onWidgets: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onResetOverlay: () -> Unit,
) {
    val density = LocalDensity.current

    val dockHeight = homeSettings.dockHeight.dp

    val dockHeightPx = with(density) {
        dockHeight.roundToPx()
    }

    var showPopupGridItemMenu by remember { mutableStateOf(false) }

    var showPopupSettingsMenu by remember { mutableStateOf(false) }

    var popupSettingsMenuIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    val launcherApps = LocalLauncherApps.current

    val wallpaperManagerWrapper = LocalWallpaperManager.current

    val context = LocalContext.current

    val view = LocalView.current

    var popupMenuIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupGridItemMenuIntSize by remember { mutableStateOf(IntSize.Zero) }

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val pageIndicatorHeight = 30.dp

    val pageIndicatorHeightPx = with(density) {
        pageIndicatorHeight.roundToPx()
    }

    var editGridItemId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = drag) {
        if (!isApplicationComponentVisible && drag == Drag.Dragging) {
            showPopupGridItemMenu = false

            onDraggingGridItem()
        }
    }

    LaunchedEffect(key1 = gridHorizontalPagerState) {
        handleWallpaperScroll(
            horizontalPagerState = gridHorizontalPagerState,
            wallpaperScroll = homeSettings.wallpaperScroll,
            wallpaperManagerWrapper = wallpaperManagerWrapper,
            pageCount = homeSettings.pageCount,
            infiniteScroll = homeSettings.infiniteScroll,
            windowToken = view.windowToken,
        )
    }

    LaunchedEffect(key1 = editGridItemId) {
        editGridItemId?.let { id ->
            showPopupGridItemMenu = false

            editGridItemId = null

            onEdit(id)
        }
    }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTap()
                    },
                    onLongPress = { offset ->
                        popupSettingsMenuIntOffset = offset.round()

                        showPopupSettingsMenu = true
                    },
                )
            }
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            ),
    ) {
        HorizontalPager(
            state = gridHorizontalPagerState,
            modifier = Modifier.weight(1f),
        ) { index ->
            val page = calculatePage(
                index = index,
                infiniteScroll = homeSettings.infiniteScroll,
                pageCount = homeSettings.pageCount,
            )

            GridLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    ),
                gridItems = gridItemsByPage[page],
                columns = homeSettings.columns,
                rows = homeSettings.rows,
                { gridItem ->
                    val cellWidth = gridWidth / homeSettings.columns

                    val cellHeight =
                        (gridHeight - pageIndicatorHeightPx - dockHeightPx) / homeSettings.rows

                    val x = gridItem.startColumn * cellWidth

                    val y = gridItem.startRow * cellHeight

                    val width = gridItem.columnSpan * cellWidth

                    val height = gridItem.rowSpan * cellHeight

                    InteractiveGridItemContent(
                        gridItem = gridItem,
                        gridItemSettings = homeSettings.gridItemSettings,
                        textColor = textColor,
                        hasShortcutHostPermission = hasShortcutHostPermission,
                        drag = drag,
                        iconPackInfoPackageName = iconPackInfoPackageName,
                        statusBarNotifications = statusBarNotifications,
                        onTapApplicationInfo = { serialNumber, componentName ->
                            val sourceBoundsX = x + leftPadding

                            val sourceBoundsY = y + topPadding

                            launcherApps.startMainActivity(
                                serialNumber = serialNumber,
                                componentName = componentName,
                                sourceBounds = Rect(
                                    sourceBoundsX,
                                    sourceBoundsY,
                                    sourceBoundsX + width,
                                    sourceBoundsY + height,
                                ),
                            )
                        },
                        onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                            val sourceBoundsX = x + leftPadding

                            val sourceBoundsY = y + topPadding

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                launcherApps.startShortcut(
                                    serialNumber = serialNumber,
                                    packageName = packageName,
                                    id = shortcutId,
                                    sourceBounds = Rect(
                                        sourceBoundsX,
                                        sourceBoundsY,
                                        sourceBoundsX + width,
                                        sourceBoundsY + height,
                                    ),
                                )
                            }
                        },
                        onTapFolderGridItem = {
                            onTapFolderGridItem(gridItem.id)
                        },
                        onLongPress = { data ->
                            val intOffset = IntOffset(x = x + leftPadding, y = y + topPadding)

                            val intSize = IntSize(width = width, height = height)

                            popupMenuIntOffset = intOffset

                            popupGridItemMenuIntSize = IntSize(width = width, height = height)

                            onUpdateGridItemOffset(intOffset, intSize)

                            showPopupGridItemMenu = true
                        },
                        onUpdateImageBitmap = { imageBitmap ->
                            onLongPressGridItem(
                                GridItemSource.Existing(gridItem = gridItem),
                                imageBitmap,
                            )
                        },
                        onResetOverlay = onResetOverlay,
                    )
                },
            )
        }

        PageIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(pageIndicatorHeight),
            pageCount = homeSettings.pageCount,
            currentPage = currentPage,
            pageOffset = gridHorizontalPagerState.currentPageOffsetFraction,
            color = getSystemTextColor(textColor = textColor),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeight)
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                ),
        ) {
            GridLayout(
                modifier = Modifier.matchParentSize(),
                gridItems = dockGridItems,
                columns = homeSettings.dockColumns,
                rows = homeSettings.dockRows,
            ) { gridItem ->
                val cellWidth = gridWidth / homeSettings.dockColumns

                val cellHeight = dockHeightPx / homeSettings.dockRows

                val x = gridItem.startColumn * cellWidth

                val y = gridItem.startRow * cellHeight

                val width = gridItem.columnSpan * cellWidth

                val height = gridItem.rowSpan * cellHeight

                InteractiveGridItemContent(
                    gridItem = gridItem,
                    gridItemSettings = homeSettings.gridItemSettings,
                    textColor = textColor,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    drag = drag,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    statusBarNotifications = statusBarNotifications,
                    onTapApplicationInfo = { serialNumber, componentName ->
                        val sourceBoundsX = x + leftPadding

                        val sourceBoundsY = y + topPadding

                        launcherApps.startMainActivity(
                            serialNumber = serialNumber,
                            componentName = componentName,
                            sourceBounds = Rect(
                                sourceBoundsX,
                                sourceBoundsY,
                                sourceBoundsX + width,
                                sourceBoundsY + height,
                            ),
                        )
                    },
                    onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                        val sourceBoundsX = x + leftPadding

                        val sourceBoundsY = y + topPadding

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                            launcherApps.startShortcut(
                                serialNumber = serialNumber,
                                packageName = packageName,
                                id = shortcutId,
                                sourceBounds = Rect(
                                    sourceBoundsX,
                                    sourceBoundsY,
                                    sourceBoundsX + width,
                                    sourceBoundsY + height,
                                ),
                            )
                        }
                    },
                    onTapFolderGridItem = {
                        onTapFolderGridItem(gridItem.id)
                    },
                    onLongPress = { data ->
                        val dockY = y + (gridHeight - dockHeightPx)

                        val intOffset = IntOffset(x = x + leftPadding, y = dockY + topPadding)

                        val intSize = IntSize(width = width, height = height)

                        popupMenuIntOffset = intOffset

                        popupGridItemMenuIntSize = IntSize(width = width, height = height)

                        onUpdateGridItemOffset(intOffset, intSize)

                        showPopupGridItemMenu = true
                    },
                    onUpdateImageBitmap = { imageBitmap ->
                        onLongPressGridItem(
                            GridItemSource.Existing(gridItem = gridItem),
                            imageBitmap,
                        )
                    },
                    onResetOverlay = onResetOverlay,
                )
            }

            Chevron(
                gestureSettings = gestureSettings,
                gridItems = gridItems,
                dockGridItems = dockGridItems,
                swipeY = swipeY,
                screenHeight = screenHeight,
                textColor = textColor,
            )
        }
    }

    if (showPopupGridItemMenu && gridItemSource?.gridItem != null) {
        PopupGridItemMenu(
            gridItem = gridItemSource.gridItem,
            x = popupMenuIntOffset.x,
            y = popupMenuIntOffset.y,
            width = popupGridItemMenuIntSize.width,
            height = popupGridItemMenuIntSize.height,
            eblanShortcutInfos = eblanShortcutInfos,
            hasShortcutHostPermission = hasShortcutHostPermission,
            onEdit = { id ->
                editGridItemId = id
            },
            onResize = onResize,
            onDeleteGridItem = onDeleteGridItem,
            onInfo = { serialNumber, componentName ->
                launcherApps.startAppDetailsActivity(
                    serialNumber = serialNumber,
                    componentName = componentName,
                    sourceBounds = Rect(
                        popupMenuIntOffset.x,
                        popupMenuIntOffset.y,
                        popupMenuIntOffset.x + popupGridItemMenuIntSize.width,
                        popupMenuIntOffset.y + popupGridItemMenuIntSize.height,
                    ),
                )
            },
            onDismissRequest = {
                showPopupGridItemMenu = false
            },
            onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                val sourceBoundsX = popupMenuIntOffset.x + leftPadding

                val sourceBoundsY = popupMenuIntOffset.y + topPadding

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    launcherApps.startShortcut(
                        serialNumber = serialNumber,
                        packageName = packageName,
                        id = shortcutId,
                        sourceBounds = Rect(
                            sourceBoundsX,
                            sourceBoundsY,
                            sourceBoundsX + popupGridItemMenuIntSize.width,
                            sourceBoundsY + popupGridItemMenuIntSize.height,
                        ),
                    )
                }
            },
        )
    }

    if (showPopupSettingsMenu) {
        PopupSettingsMenu(
            popupSettingsMenuIntOffset = popupSettingsMenuIntOffset,
            gridItems = gridItems,
            hasSystemFeatureAppWidgets = hasSystemFeatureAppWidgets,
            onSettings = onSettings,
            onEditPage = onEditPage,
            onWidgets = onWidgets,
            onWallpaper = {
                val intent = Intent(Intent.ACTION_SET_WALLPAPER)

                val chooser = Intent.createChooser(intent, "Set Wallpaper")

                context.startActivity(chooser)
            },
            onDismissRequest = {
                showPopupSettingsMenu = false
            },
        )
    }
}

@Composable
private fun BoxScope.Chevron(
    modifier: Modifier = Modifier,
    gestureSettings: GestureSettings,
    gridItems: List<GridItem>,
    dockGridItems: List<GridItem>,
    swipeY: Float,
    screenHeight: Int,
    textColor: TextColor,
) {
    val visible =
        (gestureSettings.swipeUp is GestureAction.OpenAppDrawer || gestureSettings.swipeDown is GestureAction.OpenAppDrawer) &&
            gridItems.isEmpty() &&
            dockGridItems.isEmpty() &&
            swipeY == screenHeight.toFloat()

    AnimatedVisibility(
        modifier = modifier
            .matchParentSize()
            .align(Alignment.Center),
        visible = visible,
    ) {
        Chevron(
            color = getSystemTextColor(textColor = textColor),
        )
    }
}
