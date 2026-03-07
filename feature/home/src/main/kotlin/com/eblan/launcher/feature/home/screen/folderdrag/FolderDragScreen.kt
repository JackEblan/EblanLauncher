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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest.Builder
import coil3.request.addLastModifiedToFileCacheKey
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.FolderGridLayout
import com.eblan.launcher.feature.home.component.grid.WhiteBox
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.FolderScreen
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.folder.FolderTitle
import com.eblan.launcher.feature.home.screen.folder.getFolderScreenOffset
import com.eblan.launcher.feature.home.util.FOLDER_GRID_PADDING
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.ui.local.LocalSettings

@Composable
internal fun SharedTransitionScope.FolderDragScreen(
    drag: Drag,
    folderGridHorizontalPagerState: PagerState,
    folderGridItem: GridItem,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    gridItemSettings: GridItemSettings,
    gridItemSource: GridItemSource,
    homeSettings: HomeSettings,
    iconPackFilePaths: Map<String, String>,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    screenHeight: Int,
    screenWidth: Int,
    statusBarNotifications: Map<String, Int>,
    textColor: TextColor,
    onUpdateFolderTitleHeight: (Int) -> Unit,
) {
    val data = folderGridItem.data as? GridItemData.Folder ?: return

    val gridItemSourceFolder =
        gridItemSource as? GridItemSource.Folder ?: return

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
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        Surface(
            modifier = Modifier
                .offset {
                    getFolderScreenOffset(
                        folderGridHeightPx = folderGridHeightPx,
                        folderGridWidthPx = folderGridWidthPx,
                        folderPopupIntOffset = folderPopupIntOffset,
                        folderPopupIntSize = folderPopupIntSize,
                        safeDrawingHeight = safeDrawingHeight,
                        safeDrawingWidth = safeDrawingWidth,
                    )
                }
                .size(
                    width = folderGridWidthDp,
                    height = folderGridHeightDp,
                )
                .padding(FOLDER_GRID_PADDING),
            shape = RoundedCornerShape(5.dp),
            shadowElevation = 2.dp,
            content = {
                Column(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        modifier = Modifier.weight(1f),
                        state = folderGridHorizontalPagerState,
                    ) { index ->
                        FolderGridLayout(
                            columns = data.columns,
                            gridItems = data.gridItemsByPage[index],
                            modifier = Modifier.fillMaxSize(),
                            rows = data.rows,
                            { applicationInfoGridItem ->
                                FolderGridItemContent(
                                    applicationInfoGridItem = applicationInfoGridItem,
                                    drag = drag,
                                    gridItemSettings = gridItemSettings,
                                    gridItemSourceFolder = gridItemSourceFolder,
                                    iconPackFilePaths = iconPackFilePaths,
                                    statusBarNotifications = statusBarNotifications,
                                    textColor = textColor,
                                )
                            },
                        )
                    }

                    FolderTitle(
                        data = data,
                        folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                        homeSettings = homeSettings,
                        modifier = Modifier.onSizeChanged { intSize ->
                            onUpdateFolderTitleHeight(intSize.height)
                        },
                        textColor = textColor,
                    )
                }
            },
        )
    }
}

@Composable
private fun SharedTransitionScope.FolderGridItemContent(
    applicationInfoGridItem: ApplicationInfoGridItem,
    drag: Drag,
    gridItemSettings: GridItemSettings,
    gridItemSourceFolder: GridItemSource.Folder,
    iconPackFilePaths: Map<String, String>,
    modifier: Modifier = Modifier,
    statusBarNotifications: Map<String, Int>,
    textColor: TextColor,
) {
    val currentGridItemSettings = if (applicationInfoGridItem.override) {
        applicationInfoGridItem.gridItemSettings
    } else {
        gridItemSettings
    }

    val currentTextColor = if (applicationInfoGridItem.override) {
        getGridItemTextColor(
            gridItemCustomTextColor = applicationInfoGridItem.gridItemSettings.customTextColor,
            gridItemTextColor = applicationInfoGridItem.gridItemSettings.textColor,
            systemCustomTextColor = gridItemSettings.customTextColor,
            systemTextColor = textColor,
        )
    } else {
        getSystemTextColor(
            systemCustomTextColor = gridItemSettings.customTextColor,
            systemTextColor = textColor,
        )
    }
    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = currentGridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = currentGridItemSettings.verticalArrangement)

    val isDragging =
        (drag == Drag.Start || drag == Drag.Dragging) && gridItemSourceFolder.applicationInfoGridItem.id == applicationInfoGridItem.id

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
            val settings = LocalSettings.current
            val maxLines = if (currentGridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE
            val icon = iconPackFilePaths[applicationInfoGridItem.componentName]
                ?: applicationInfoGridItem.icon
            val hasNotifications =
                statusBarNotifications[applicationInfoGridItem.packageName] != null && (
                    statusBarNotifications[applicationInfoGridItem.packageName]
                        ?: 0
                    ) > 0
            Box(modifier = Modifier.size(currentGridItemSettings.iconSize.dp)) {
                AsyncImage(
                    model = Builder(LocalContext.current).data(
                        applicationInfoGridItem.customIcon ?: icon,
                    )
                        .addLastModifiedToFileCacheKey(true).build(),
                    contentDescription = null,
                    modifier = modifier
                        .sharedElementWithCallerManagedVisibility(
                            rememberSharedContentState(
                                key = SharedElementKey(
                                    id = applicationInfoGridItem.id,
                                    screen = FolderScreen.Drag,
                                ),
                            ),
                            visible = drag == Drag.Cancel || drag == Drag.End,
                        )
                        .fillMaxSize().matchParentSize(),
                )

                if (settings.isNotificationAccessGranted() && hasNotifications) {
                    Box(
                        modifier = Modifier
                            .size((currentGridItemSettings.iconSize * 0.3).dp)
                            .align(Alignment.TopEnd)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                    )
                }

                if (applicationInfoGridItem.serialNumber != 0L) {
                    ElevatedCard(
                        modifier = Modifier
                            .size((currentGridItemSettings.iconSize * 0.4).dp)
                            .align(Alignment.BottomEnd),
                    ) {
                        Icon(
                            imageVector = EblanLauncherIcons.Work,
                            contentDescription = null,
                            modifier = Modifier.padding(2.dp),
                        )
                    }
                }
            }
            if (currentGridItemSettings.showLabel) {
                Text(
                    text = applicationInfoGridItem.customLabel ?: applicationInfoGridItem.label,
                    color = currentTextColor,
                    textAlign = TextAlign.Center,
                    maxLines = maxLines,
                    fontSize = currentGridItemSettings.textSize.sp,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
