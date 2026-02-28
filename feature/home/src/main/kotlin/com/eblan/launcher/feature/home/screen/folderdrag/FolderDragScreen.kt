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
package com.eblan.launcher.feature.home.screen.folderdrag

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoFolderGridItemContent
import com.eblan.launcher.feature.home.component.grid.FolderGridLayout
import com.eblan.launcher.feature.home.component.grid.WhiteBox
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement

@Composable
internal fun SharedTransitionScope.FolderDragScreen(
    modifier: Modifier = Modifier,
    folderGridItem: GridItem,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    paddingValues: PaddingValues,
    folderGridHorizontalPagerState: PagerState,
    screenWidth: Int,
    screenHeight: Int,
    homeSettings: HomeSettings,
    textColor: TextColor,
    gridItemSettings: GridItemSettings,
    statusBarNotifications: Map<String, Int>,
    iconPackFilePaths: Map<String, String>,
    onDismissRequest: () -> Unit,
    drag: Drag,
    screen: Screen,
    gridItemSource: GridItemSource,
) {
    val data = folderGridItem.data as? GridItemData.Folder ?: error("Expected GridItemData.Folder")

    val gridItemSourceFolder =
        gridItemSource as? GridItemSource.Folder ?: error("Expected GridItemSource.Folder")

    val density = LocalDensity.current

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

    val safeDrawingWidth = screenWidth - horizontalPadding

    val safeDrawingHeight = screenHeight - verticalPadding

    val folderCellWidth = safeDrawingWidth / homeSettings.columns

    val folderCellHeight = safeDrawingHeight / homeSettings.rows

    val folderGridPaddingDp = 10.dp

    val folderGridWidthDp = with(density) {
        (folderCellWidth * data.columns).toDp()
    }

    val folderGridHeightDp = with(density) {
        (folderCellHeight * data.rows).toDp()
    }

    val folderGridWidthPx = with(density) {
        folderGridWidthDp.roundToPx()
    }

    val folderGridHeightPx = with(density) {
        folderGridHeightDp.roundToPx()
    }

    Box(
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
    ) {
        Surface(
            modifier = Modifier
                .offset {
                    val centeredX =
                        folderPopupIntOffset.x + (folderPopupIntSize.width / 2) - (folderGridWidthPx / 2)

                    val centeredY =
                        folderPopupIntOffset.y + (folderPopupIntSize.height / 2) - (folderGridHeightPx / 2)

                    val popupX = centeredX.coerceIn(0, safeDrawingWidth - folderGridWidthPx)
                    val popupY = centeredY.coerceIn(0, safeDrawingHeight - folderGridHeightPx)

                    IntOffset(
                        x = popupX,
                        y = popupY,
                    )
                }
                .size(
                    width = folderGridWidthDp,
                    height = folderGridHeightDp,
                )
                .padding(folderGridPaddingDp),
            shape = RoundedCornerShape(5.dp),
            shadowElevation = 2.dp,
            content = {
                HorizontalPager(
                    state = folderGridHorizontalPagerState,
                ) { index ->
                    FolderGridLayout(
                        modifier = Modifier.fillMaxSize(),
                        gridItems = data.gridItemsByPage[index],
                        columns = data.columns,
                        rows = data.rows,
                        { applicationInfoGridItem ->
                            FolderGridItemContent(
                                applicationInfoGridItem = applicationInfoGridItem,
                                textColor = textColor,
                                gridItemSettings = gridItemSettings,
                                statusBarNotifications = statusBarNotifications,
                                iconPackFilePaths = iconPackFilePaths,
                                drag = drag,
                                screen = screen,
                                gridItemSourceFolder = gridItemSourceFolder,
                            )
                        },
                    )
                }
            },
        )
    }
}

@Composable
private fun SharedTransitionScope.FolderGridItemContent(
    modifier: Modifier = Modifier,
    applicationInfoGridItem: ApplicationInfoGridItem,
    textColor: TextColor,
    gridItemSettings: GridItemSettings,
    statusBarNotifications: Map<String, Int>,
    iconPackFilePaths: Map<String, String>,
    drag: Drag,
    screen: Screen,
    gridItemSourceFolder: GridItemSource.Folder,
) {
    val currentGridItemSettings = if (applicationInfoGridItem.override) {
        applicationInfoGridItem.gridItemSettings
    } else {
        gridItemSettings
    }

    val currentTextColor = if (applicationInfoGridItem.override) {
        getGridItemTextColor(
            systemTextColor = textColor,
            systemCustomTextColor = gridItemSettings.customTextColor,
            gridItemTextColor = applicationInfoGridItem.gridItemSettings.textColor,
            gridItemCustomTextColor = applicationInfoGridItem.gridItemSettings.customTextColor,
        )
    } else {
        getSystemTextColor(
            systemTextColor = textColor,
            systemCustomTextColor = gridItemSettings.customTextColor,
        )
    }
    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = currentGridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = currentGridItemSettings.verticalArrangement)

    val isDragging =
        (drag == Drag.Start || drag == Drag.Dragging) &&
                gridItemSourceFolder.applicationInfoGridItem.id == applicationInfoGridItem.id

    if (isDragging) {
        WhiteBox(
            modifier = modifier,
            textColor = currentTextColor,
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(currentGridItemSettings.padding.dp)
                .background(
                    color = Color(currentGridItemSettings.customBackgroundColor),
                    shape = RoundedCornerShape(size = currentGridItemSettings.cornerRadius.dp),
                ),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
        ) {
            ApplicationInfoFolderGridItemContent(
                modifier = modifier
                    .sharedElementWithCallerManagedVisibility(
                        rememberSharedContentState(
                            key = SharedElementKey(
                                id = applicationInfoGridItem.id,
                                screen = screen,
                            ),
                        ),
                        visible = drag == Drag.Cancel || drag == Drag.End,
                    )
                    .fillMaxSize(),
                gridItem = applicationInfoGridItem,
                textColor = currentTextColor,
                gridItemSettings = currentGridItemSettings,
                statusBarNotifications = statusBarNotifications,
                iconPackFilePaths = iconPackFilePaths,
            )
        }
    }
}