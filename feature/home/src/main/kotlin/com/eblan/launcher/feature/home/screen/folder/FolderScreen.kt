package com.eblan.launcher.feature.home.screen.folder

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.InteractiveApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.InteractiveNestedFolderGridItem
import com.eblan.launcher.feature.home.component.grid.InteractiveShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.InteractiveWidgetGridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen

@Composable
fun FolderScreen(
    modifier: Modifier = Modifier,
    startCurrentPage: Int,
    foldersDataById: ArrayDeque<FolderDataById>,
    folderRows: Int,
    folderColumns: Int,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    drag: Drag,
    gridItemSource: GridItemSource?,
    paddingValues: PaddingValues,
    hasShortcutHostPermission: Boolean,
    gridWidth: Int,
    gridHeight: Int,
    onUpdateScreen: (Screen) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
    onResetTargetPage: () -> Unit,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
        intOffset: IntOffset,
    ) -> Unit,
    onDraggingGridItem: (List<GridItem>) -> Unit,
) {
    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val titleHeightDp = 30.dp

    val titleHeightPx = with(density) {
        titleHeightDp.roundToPx()
    }

    LaunchedEffect(key1 = foldersDataById) {
        if (foldersDataById.isEmpty()) {
            onResetTargetPage()

            onUpdateScreen(Screen.Pager)
        }
    }

    BackHandler(foldersDataById.isNotEmpty()) {
        onRemoveLastFolder()
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && gridItemSource != null) {
            onDraggingGridItem(foldersDataById.last().gridItems)
        }
    }

    foldersDataById.forEach { folderDataById ->
        val horizontalPagerState = rememberPagerState(
            initialPage = startCurrentPage,
            pageCount = {
                folderDataById.pageCount
            },
        )

        Surface(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding(),
                    )
                    .fillMaxSize(),
            ) {
                Text(
                    modifier = Modifier.height(titleHeightDp),
                    text = folderDataById.label,
                )

                HorizontalPager(state = horizontalPagerState) { index ->
                    GridLayout(
                        modifier = Modifier
                            .padding(
                                start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
                                end = paddingValues.calculateRightPadding(LayoutDirection.Ltr),
                            )
                            .fillMaxSize(),
                        rows = folderRows,
                        columns = folderColumns,
                    ) {
                        folderDataById.gridItemsByPage[index]?.forEach { gridItem ->
                            val cellWidth = gridWidth / folderColumns

                            val cellHeight = (gridHeight - titleHeightPx) / folderRows

                            val x = gridItem.startColumn * cellWidth

                            val y = gridItem.startRow * cellHeight

                            GridItemContent(
                                gridItem = gridItem,
                                gridItemSettings = gridItemSettings,
                                textColor = textColor,
                                hasShortcutHostPermission = hasShortcutHostPermission,
                                onTapApplicationInfo = launcherApps::startMainActivity,
                                onTapShortcutInfo = launcherApps::startShortcut,
                                onTapFolderGridItem = {
                                    onResetTargetPage()

                                    onAddFolder(gridItem.id)
                                },
                                onLongPress = { imageBitmap ->
                                    val intOffset =
                                        IntOffset(
                                            x = x + leftPadding,
                                            y = y + (topPadding + titleHeightPx),
                                        )

                                    onLongPressGridItem(
                                        index,
                                        GridItemSource.Existing(gridItem = gridItem),
                                        imageBitmap,
                                        intOffset,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridItemContent(
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    textColor: Long,
    hasShortcutHostPermission: Boolean,
    onTapApplicationInfo: (String?) -> Unit,
    onTapShortcutInfo: (
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onTapFolderGridItem: () -> Unit,
    onLongPress: (ImageBitmap?) -> Unit,
) {
    val currentGridItemSettings = if (gridItem.override) {
        gridItem.gridItemSettings
    } else {
        gridItemSettings
    }

    val currentTextColor = if (gridItem.override) {
        when (gridItem.gridItemSettings.textColor) {
            TextColor.System -> {
                textColor
            }

            TextColor.Light -> {
                0xFFFFFFFF
            }

            TextColor.Dark -> {
                0xFF000000
            }
        }
    } else {
        textColor
    }

    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> {
            InteractiveApplicationInfoGridItem(
                textColor = currentTextColor,
                gridItemSettings = currentGridItemSettings,
                gridItem = gridItem,
                data = data,
                onTap = {
                    onTapApplicationInfo(data.componentName)
                },
                onLongPress = onLongPress,
            )
        }

        is GridItemData.Widget -> {
            InteractiveWidgetGridItem(
                gridItem = gridItem,
                data = data,
                onLongPress = onLongPress,
            )
        }

        is GridItemData.ShortcutInfo -> {
            InteractiveShortcutInfoGridItem(
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                gridItem = gridItem,
                data = data,
                onTap = {
                    if (hasShortcutHostPermission) {
                        onTapShortcutInfo(
                            data.packageName,
                            data.shortcutId,
                        )
                    }
                },
                onLongPress = onLongPress,
            )
        }

        is GridItemData.Folder -> {
            InteractiveNestedFolderGridItem(
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                gridItem = gridItem,
                data = data,
                onTap = onTapFolderGridItem,
                onLongPress = onLongPress,
            )
        }
    }
}