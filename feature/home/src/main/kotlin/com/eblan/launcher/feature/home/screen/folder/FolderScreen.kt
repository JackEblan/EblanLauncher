package com.eblan.launcher.feature.home.screen.folder

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
    onUpdateScreen: (Screen) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
    onResetTargetPage: () -> Unit,
    onLongPressGridItem: (
        targetPage: Int,
        gridItemSource: GridItemSource,
    ) -> Unit,
    onDraggingGridItem: (List<GridItem>) -> Unit,
) {
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
            Column(modifier = Modifier.fillMaxSize()) {

                Text(text = folderDataById.label)

                HorizontalPager(state = horizontalPagerState) { index ->
                    GridLayout(
                        modifier = Modifier.fillMaxSize(),
                        rows = folderRows,
                        columns = folderColumns,
                    ) {
                        folderDataById.gridItemsByPage[index]?.forEach { gridItem ->
                            key(gridItem.id) {
                                val currentGridItemSettings by remember(key1 = gridItem) {
                                    val currentGridItemSettings = if (gridItem.override) {
                                        gridItem.gridItemSettings
                                    } else {
                                        gridItemSettings
                                    }

                                    mutableStateOf(currentGridItemSettings)
                                }

                                val currentTextColor by remember(key1 = gridItem) {
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

                                    mutableLongStateOf(currentTextColor)
                                }

                                when (val data = gridItem.data) {
                                    is GridItemData.ApplicationInfo -> {
                                        InteractiveApplicationInfoGridItem(
                                            textColor = currentTextColor,
                                            gridItemSettings = currentGridItemSettings,
                                            gridItem = gridItem,
                                            data = data,
                                            onTap = {

                                            },
                                            onLongPress = {
                                                onLongPressGridItem(
                                                    index,
                                                    GridItemSource.Existing(gridItem = gridItem),
                                                )
                                            },
                                        )
                                    }

                                    is GridItemData.Widget -> {
                                        InteractiveWidgetGridItem(
                                            gridItem = gridItem,
                                            gridItemData = data,
                                            onLongPress = {
                                                onLongPressGridItem(
                                                    index,
                                                    GridItemSource.Existing(gridItem = gridItem),
                                                )
                                            },
                                        )
                                    }

                                    is GridItemData.ShortcutInfo -> {
                                        InteractiveShortcutInfoGridItem(
                                            textColor = currentTextColor,
                                            gridItemSettings = currentGridItemSettings,
                                            gridItem = gridItem,
                                            data = data,
                                            onTap = {

                                            },
                                            onLongPress = {
                                                onLongPressGridItem(
                                                    index,
                                                    GridItemSource.Existing(gridItem = gridItem),
                                                )
                                            },
                                        )
                                    }

                                    is GridItemData.Folder -> {
                                        InteractiveNestedFolderGridItem(
                                            textColor = currentTextColor,
                                            gridItemSettings = currentGridItemSettings,
                                            gridItem = gridItem,
                                            data = data,
                                            onTap = {
                                                onResetTargetPage()

                                                onAddFolder(gridItem.id)
                                            },
                                            onLongPress = {
                                                onLongPressGridItem(
                                                    index,
                                                    GridItemSource.Existing(gridItem = gridItem),
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
        }
    }
}