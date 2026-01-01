/*
 *
 *Copyright 2023 Einstein Blanco
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
package com.eblan.launcher.feature.home.screen.folder

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemCache
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.InteractiveGridItemContent
import com.eblan.launcher.feature.home.component.indicator.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.FolderPopupType
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.folderdrag.FolderDragScreen
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.ui.local.LocalLauncherApps

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.FolderScreen(
    modifier: Modifier = Modifier,
    foldersDataById: ArrayDeque<FolderDataById>,
    gridItemCache: GridItemCache,
    gridItemSource: GridItemSource?,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    textColor: TextColor,
    homeSettings: HomeSettings,
    folderGridHorizontalPagerState: PagerState,
    statusBarNotifications: Map<String, Int>,
    iconPackFilePaths: Map<String, String>,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    folderPopupType: FolderPopupType,
    dragIntOffset: IntOffset,
    popupIntOffset: IntOffset,
    moveGridItemResult: MoveGridItemResult?,
    lockMovement: Boolean,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onDraggingGridItem: (List<GridItem>) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onMoveFolderGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
        lockMovement: Boolean,
    ) -> Unit,
    onDragEndFolder: () -> Unit,
    onDragCancelFolder: () -> Unit,
    onMoveGridItemOutsideFolder: (
        gridItemSource: GridItemSource,
        folderId: String,
        movingGridItem: GridItem,
    ) -> Unit,
    onResetOverlay: () -> Unit,
) {
    val density = LocalDensity.current

    val context = LocalContext.current

    val launcherApps = LocalLauncherApps.current

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateEndPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val gridWidth = screenWidth - horizontalPadding

    val gridHeight = screenHeight - verticalPadding

    val folderDataById = foldersDataById.lastOrNull()

    val pageIndicatorHeight = 30.dp

    val cellWidth = gridWidth / homeSettings.columns

    val cellHeight = gridHeight / homeSettings.rows

    Surface(modifier = modifier) {
        if (folderDataById != null) {
            AnimatedContent(targetState = folderPopupType) { targetState ->
                when (targetState) {
                    FolderPopupType.Folder -> {
                        val folderWidth =
                            with(density) {
                                (cellWidth * homeSettings.folderColumns).toDp()
                            }

                        val folderHeight =
                            with(density) {
                                (cellHeight * homeSettings.folderRows).toDp() + pageIndicatorHeight
                            }

                        Column(modifier = Modifier.size(folderWidth, folderHeight)) {
                            HorizontalPager(
                                state = folderGridHorizontalPagerState,
                                modifier = Modifier.weight(1f),
                            ) { index ->
                                GridLayout(
                                    modifier = Modifier.fillMaxSize(),
                                    gridItems = folderDataById.gridItemsByPage[index],
                                    columns = homeSettings.folderColumns,
                                    rows = homeSettings.folderRows,
                                    { gridItem ->
                                        val x = gridItem.startColumn * cellWidth

                                        val y = gridItem.startRow * cellHeight

                                        val width = gridItem.columnSpan * cellWidth

                                        val height = gridItem.rowSpan * cellHeight

                                        InteractiveGridItemContent(
                                            gridItem = gridItem,
                                            hasShortcutHostPermission = hasShortcutHostPermission,
                                            drag = drag,
                                            gridItemSettings = homeSettings.gridItemSettings,
                                            textColor = textColor,
                                            statusBarNotifications = statusBarNotifications,
                                            isScrollInProgress = folderGridHorizontalPagerState.isScrollInProgress,
                                            iconPackFilePaths = iconPackFilePaths,
                                            onTapApplicationInfo = { serialNumber, componentName ->
                                                launcherApps.startMainActivity(
                                                    serialNumber = serialNumber,
                                                    componentName = componentName,
                                                    sourceBounds = Rect(
                                                        x,
                                                        y,
                                                        x + width,
                                                        y + height,
                                                    ),
                                                )
                                            },
                                            onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                                    launcherApps.startShortcut(
                                                        serialNumber = serialNumber,
                                                        packageName = packageName,
                                                        id = shortcutId,
                                                        sourceBounds = Rect(
                                                            x,
                                                            y,
                                                            x + width,
                                                            y + height,
                                                        ),
                                                    )
                                                }
                                            },
                                            onTapShortcutConfig = { uri ->
                                                context.startActivity(Intent.parseUri(uri, 0))
                                            },
                                            onTapFolderGridItem = {
                                            },
                                            onUpdateGridItemOffset = onUpdateGridItemOffset,
                                            onUpdateImageBitmap = { imageBitmap ->
                                                onLongPressGridItem(
                                                    GridItemSource.Existing(gridItem = gridItem),
                                                    imageBitmap,
                                                )
                                            },
                                            onDraggingGridItem = {
                                                onDraggingGridItem(foldersDataById.last().gridItems)
                                            },
                                            onUpdateSharedElementKey = onUpdateSharedElementKey,
                                        )
                                    },
                                )
                            }

                            PageIndicator(
                                modifier = Modifier
                                    .height(pageIndicatorHeight)
                                    .fillMaxWidth(),
                                pageCount = folderGridHorizontalPagerState.pageCount,
                                currentPage = folderGridHorizontalPagerState.currentPage,
                                pageOffset = folderGridHorizontalPagerState.currentPageOffsetFraction,
                                color = getSystemTextColor(textColor = textColor),
                            )
                        }
                    }

                    FolderPopupType.Drag -> {
                        FolderDragScreen(
                            gridItemCache = gridItemCache,
                            folderDataById = folderDataById,
                            gridItemSource = gridItemSource,
                            textColor = textColor,
                            drag = drag,
                            dragIntOffset = dragIntOffset,
                            popupIntOffset = popupIntOffset,
                            screenWidth = screenWidth,
                            screenHeight = screenHeight,
                            paddingValues = paddingValues,
                            homeSettings = homeSettings,
                            moveGridItemResult = moveGridItemResult,
                            folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                            statusBarNotifications = statusBarNotifications,
                            hasShortcutHostPermission = hasShortcutHostPermission,
                            iconPackFilePaths = iconPackFilePaths,
                            lockMovement = lockMovement,
                            onMoveFolderGridItem = onMoveFolderGridItem,
                            onDragEndFolder = onDragEndFolder,
                            onDragCancelFolder = onDragCancelFolder,
                            onMoveGridItemOutsideFolder = onMoveGridItemOutsideFolder,
                            onResetOverlay = onResetOverlay,
                        )
                    }
                }
            }
        }
    }
}
