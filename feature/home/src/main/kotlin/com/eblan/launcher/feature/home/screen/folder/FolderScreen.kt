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
package com.eblan.launcher.feature.home.screen.folder

import android.graphics.Rect
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.InteractiveGridItemContent
import com.eblan.launcher.feature.home.component.pageindicator.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.ui.local.LocalLauncherApps

@Composable
fun FolderScreen(
    modifier: Modifier = Modifier,
    foldersDataById: ArrayDeque<FolderDataById>,
    drag: Drag,
    paddingValues: PaddingValues,
    hasShortcutHostPermission: Boolean,
    screenWidth: Int,
    screenHeight: Int,
    textColor: TextColor,
    homeSettings: HomeSettings,
    iconPackInfoPackageName: String,
    folderGridHorizontalPagerState: PagerState,
    onUpdateScreen: (Screen) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onDraggingGridItem: (List<GridItem>) -> Unit,
) {
    val density = LocalDensity.current

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

    var titleHeight by remember { mutableIntStateOf(0) }

    val folderDataById = foldersDataById.lastOrNull()

    LaunchedEffect(key1 = foldersDataById) {
        if (foldersDataById.isEmpty()) {
            onUpdateScreen(Screen.Pager)
        }
    }

    BackHandler(foldersDataById.isNotEmpty()) {
        onRemoveLastFolder()
    }

    val pageIndicatorHeight = 30.dp

    val pageIndicatorHeightPx = with(density) {
        pageIndicatorHeight.roundToPx()
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (folderDataById != null) {
            AnimatedContent(
                modifier = Modifier
                    .matchParentSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding(),
                    ),
                targetState = folderDataById,
            ) { targetState ->
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .onSizeChanged {
                                titleHeight = it.height
                            }
                            .padding(5.dp),
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = targetState.label,
                            color = getSystemTextColor(textColor = textColor),
                            style = MaterialTheme.typography.headlineLarge,
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    HorizontalPager(
                        state = folderGridHorizontalPagerState,
                        modifier = Modifier.weight(1f),
                    ) { index ->
                        GridLayout(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                                ),
                            gridItems = targetState.gridItemsByPage[index],
                            columns = homeSettings.folderColumns,
                            rows = homeSettings.folderRows,
                            { gridItem ->
                                val cellWidth = gridWidth / homeSettings.folderColumns

                                val cellHeight =
                                    (gridHeight - pageIndicatorHeightPx - titleHeight) / homeSettings.folderRows

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
                                    iconPackInfoPackageName = iconPackInfoPackageName,
                                    onTapApplicationInfo = { componentName ->
                                        val sourceBoundsX = x + leftPadding

                                        val sourceBoundsY = y + topPadding

                                        launcherApps.startMainActivity(
                                            componentName = componentName,
                                            sourceBounds = Rect(
                                                sourceBoundsX,
                                                sourceBoundsY,
                                                sourceBoundsX + width,
                                                sourceBoundsY + height,
                                            ),
                                        )
                                    },
                                    onTapShortcutInfo = { packageName, shortcutId ->
                                        val sourceBoundsX = x + leftPadding

                                        val sourceBoundsY = y + topPadding

                                        launcherApps.startShortcut(
                                            packageName = packageName,
                                            id = shortcutId,
                                            sourceBounds = Rect(
                                                sourceBoundsX,
                                                sourceBoundsY,
                                                sourceBoundsX + width,
                                                sourceBoundsY + height,
                                            ),
                                        )
                                    },
                                    onTapFolderGridItem = {
                                        onAddFolder(gridItem.id)
                                    },
                                    onLongPress = {
                                        onUpdateGridItemOffset(
                                            IntOffset(
                                                x = x + leftPadding,
                                                y = y + (topPadding + titleHeight),
                                            ),
                                            IntSize(
                                                width = width,
                                                height = height,
                                            ),
                                        )
                                    },
                                    onUpdateImageBitmap = { imageBitmap ->
                                        onLongPressGridItem(
                                            GridItemSource.Existing(gridItem = gridItem),
                                            imageBitmap,
                                        )
                                    },
                                    onDraggingGridItem = {
                                        onDraggingGridItem(foldersDataById.last().gridItems)
                                    },
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
                    )
                }
            }
        }
    }
}
