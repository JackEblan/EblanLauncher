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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoFolderGridItemContent
import com.eblan.launcher.feature.home.component.grid.FolderGridLayout
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement

@Composable
internal fun SharedTransitionScope.FolderDragScreen(
    modifier: Modifier = Modifier,
    gridItemDataFolder: GridItemData.Folder,
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
) {
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

    val x = folderPopupIntOffset.x - leftPadding

    val y = folderPopupIntOffset.y - topPadding

    val cellWidth = safeDrawingWidth / homeSettings.columns

    val cellHeight = safeDrawingHeight / homeSettings.rows

    val gridPaddingDp = 10.dp

    val gridWidthDp = with(density) {
        (cellWidth * gridItemDataFolder.columns).toDp()
    }

    val gridHeightDp = with(density) {
        (cellHeight * gridItemDataFolder.rows).toDp()
    }

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
            Surface(
                modifier = Modifier
                    .size(
                        width = gridWidthDp,
                        height = gridHeightDp,
                    )
                    .padding(gridPaddingDp),
                shape = RoundedCornerShape(5.dp),
                shadowElevation = 2.dp,
                content = {
                    HorizontalPager(
                        state = folderGridHorizontalPagerState,
                    ) { index ->
                        FolderGridLayout(
                            modifier = Modifier.fillMaxSize(),
                            gridItems = gridItemDataFolder.gridItemsByPage[index],
                            columns = gridItemDataFolder.columns,
                            rows = gridItemDataFolder.rows,
                            { gridItem ->
                                FolderGridItemContent(
                                    gridItem = gridItem,
                                    textColor = textColor,
                                    gridItemSettings = gridItemSettings,
                                    statusBarNotifications = statusBarNotifications,
                                    iconPackFilePaths = iconPackFilePaths,
                                    drag = drag,
                                    screen = screen,
                                )
                            },
                        )
                    }
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

        val parentCenterX = x + folderPopupIntSize.width / 2

        val childX = (parentCenterX - placeable.width / 2).coerceIn(
            0,
            constraints.maxWidth - placeable.width,
        )

        val topY = y - placeable.height
        val bottomY = y + folderPopupIntSize.height

        val childY = (if (topY < 0) bottomY else topY).coerceIn(
            0,
            constraints.maxHeight - placeable.height,
        )

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(
                x = childX,
                y = childY,
            )
        }
    }
}

@Composable
private fun SharedTransitionScope.FolderGridItemContent(
    modifier: Modifier = Modifier,
    gridItem: ApplicationInfoGridItem,
    textColor: TextColor,
    gridItemSettings: GridItemSettings,
    statusBarNotifications: Map<String, Int>,
    iconPackFilePaths: Map<String, String>,
    drag: Drag,
    screen: Screen,
) {
    val currentGridItemSettings = if (gridItem.override) {
        gridItem.gridItemSettings
    } else {
        gridItemSettings
    }

    val currentTextColor = if (gridItem.override) {
        getGridItemTextColor(
            systemTextColor = textColor,
            systemCustomTextColor = gridItemSettings.customTextColor,
            gridItemTextColor = gridItem.gridItemSettings.textColor,
            gridItemCustomTextColor = gridItem.gridItemSettings.customTextColor,
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
                            id = gridItem.id,
                            screen = screen,
                        ),
                    ),
                    visible = drag == Drag.Cancel || drag == Drag.End,
                )
                .fillMaxSize(),
            gridItem = gridItem,
            textColor = currentTextColor,
            gridItemSettings = currentGridItemSettings,
            statusBarNotifications = statusBarNotifications,
            iconPackFilePaths = iconPackFilePaths,
        )
    }
}