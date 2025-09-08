package com.eblan.launcher.feature.home.screen.folder

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.local.LocalLauncherApps
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

@Composable
fun FolderScreen(
    modifier: Modifier = Modifier,
    startCurrentPage: Int,
    foldersDataById: ArrayDeque<FolderDataById>,
    drag: Drag,
    gridItemSource: GridItemSource?,
    paddingValues: PaddingValues,
    hasShortcutHostPermission: Boolean,
    screenWidth: Int,
    screenHeight: Int,
    textColor: TextColor,
    homeSettings: HomeSettings,
    onUpdateScreen: (Screen) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
    onResetTargetPage: () -> Unit,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
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
            onResetTargetPage()

            onUpdateScreen(Screen.Pager)
        }
    }

    BackHandler(foldersDataById.isNotEmpty()) {
        onRemoveLastFolder()
    }

    LaunchedEffect(key1 = drag, key2 = gridItemSource) {
        if (drag == Drag.Dragging && gridItemSource != null) {
            onDraggingGridItem(foldersDataById.last().gridItems)
        }
    }

    val pageIndicatorHeight = 30.dp

    val pageIndicatorHeightPx = with(density) {
        pageIndicatorHeight.roundToPx()
    }

    if (folderDataById != null) {
        AnimatedContent(targetState = folderDataById) { targetState ->
            val horizontalPagerState = rememberPagerState(
                initialPage = startCurrentPage,
                pageCount = {
                    folderDataById.pageCount
                },
            )

            Column(
                modifier = modifier
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding(),
                    )
                    .fillMaxSize(),
            ) {
                Text(
                    text = targetState.label,
                    color = getSystemTextColor(textColor = textColor),
                    style = MaterialTheme.typography.titleLarge,
                    onTextLayout = { textLayoutResult ->
                        titleHeight = textLayoutResult.size.height
                    }
                )

                HorizontalPager(
                    state = horizontalPagerState,
                    modifier = Modifier.weight(1f),
                ) { index ->
                    GridLayout(
                        modifier = Modifier
                            .padding(
                                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                            )
                            .fillMaxSize(),
                        rows = homeSettings.folderRows,
                        columns = homeSettings.folderColumns,
                    ) {
                        targetState.gridItemsByPage[index]?.forEach { gridItem ->
                            val cellWidth = gridWidth / homeSettings.folderColumns

                            val cellHeight =
                                (gridHeight - pageIndicatorHeightPx - titleHeight) / homeSettings.folderRows

                            val x = gridItem.startColumn * cellWidth

                            val y = gridItem.startRow * cellHeight

                            InteractiveGridItemContent(
                                gridItem = gridItem,
                                hasShortcutHostPermission = hasShortcutHostPermission,
                                drag = drag,
                                gridItemSettings = homeSettings.gridItemSettings,
                                textColor = textColor,
                                onTapApplicationInfo = launcherApps::startMainActivity,
                                onTapShortcutInfo = launcherApps::startShortcut,
                                onTapFolderGridItem = {
                                    onResetTargetPage()

                                    onAddFolder(gridItem.id)
                                },
                                onLongPress = {
                                    onUpdateGridItemOffset(
                                        IntOffset(
                                            x = x + leftPadding,
                                            y = y + (topPadding + titleHeight),
                                        ),
                                    )
                                },
                                onUpdateImageBitmap = { imageBitmap ->
                                    onLongPressGridItem(
                                        index,
                                        GridItemSource.Existing(gridItem = gridItem),
                                        imageBitmap,
                                    )
                                },
                            )
                        }
                    }
                }

                PageIndicator(
                    modifier = Modifier
                        .height(pageIndicatorHeight)
                        .fillMaxWidth(),
                    pageCount = horizontalPagerState.pageCount,
                    currentPage = horizontalPagerState.currentPage,
                )
            }
        }
    }
}