package com.eblan.launcher.feature.home.screen.folder

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
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
    folders: ArrayDeque<GridItemData.Folder>,
    folderRows: Int,
    folderColumns: Int,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    drag: Drag,
    onUpdateScreen: (Screen) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
    onLongPressGridItem: (GridItemSource) -> Unit,
    onDraggingGridItem: (List<GridItem>) -> Unit,
) {
    LaunchedEffect(key1 = folders) {
        if (folders.isEmpty()) {
            onUpdateScreen(Screen.Pager)
        }
    }

    BackHandler(folders.isNotEmpty()) {
        onRemoveLastFolder()
    }

    folders.forEach { folderData ->
        Surface(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {

                Text(text = folderData.label)

                GridLayout(
                    modifier = Modifier.fillMaxSize(),
                    rows = folderRows,
                    columns = folderColumns,
                ) {
                    folderData.gridItems.forEach { gridItem ->
                        when (val data = gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                InteractiveApplicationInfoGridItem(
                                    textColor = textColor,
                                    gridItemSettings = gridItem.gridItemSettings
                                        ?: gridItemSettings,
                                    gridItem = gridItem,
                                    data = data,
                                    onTap = {

                                    },
                                    onLongPress = {
                                        onLongPressGridItem(
                                            GridItemSource.Existing(gridItem = gridItem),
                                        )
                                    },
                                    onDragging = {
                                        onDraggingGridItem(folders.last().gridItems)
                                    },
                                )
                            }

                            is GridItemData.Widget -> {
                                InteractiveWidgetGridItem(
                                    gridItem = gridItem,
                                    gridItemData = data,
                                    drag = drag,
                                    onLongPress = {
                                        onLongPressGridItem(
                                            GridItemSource.Existing(gridItem = gridItem),
                                        )
                                    },
                                    onDragging = {
                                        onDraggingGridItem(folders.last().gridItems)
                                    },
                                )
                            }

                            is GridItemData.ShortcutInfo -> {
                                InteractiveShortcutInfoGridItem(
                                    textColor = textColor,
                                    gridItemSettings = gridItem.gridItemSettings
                                        ?: gridItemSettings,
                                    gridItem = gridItem,
                                    data = data,
                                    onTap = {

                                    },
                                    onLongPress = {
                                        onLongPressGridItem(
                                            GridItemSource.Existing(gridItem = gridItem),
                                        )
                                    },
                                    onDragging = {
                                        onDraggingGridItem(folders.last().gridItems)
                                    },
                                )
                            }

                            is GridItemData.Folder -> {
                                InteractiveNestedFolderGridItem(
                                    textColor = textColor,
                                    gridItemSettings = gridItem.gridItemSettings
                                        ?: gridItemSettings,
                                    gridItem = gridItem,
                                    data = data,
                                    onTap = {
                                        onAddFolder(gridItem.id)
                                    },
                                    onLongPress = {
                                        onLongPressGridItem(
                                            GridItemSource.Existing(gridItem = gridItem),
                                        )
                                    },
                                    onDragging = {
                                        onDraggingGridItem(folders.last().gridItems)
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