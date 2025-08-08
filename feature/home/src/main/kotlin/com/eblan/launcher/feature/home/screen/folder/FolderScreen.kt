package com.eblan.launcher.feature.home.screen.folder

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.InteractiveApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.InteractiveNestedFolderGridItem
import com.eblan.launcher.feature.home.component.grid.InteractiveShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.InteractiveWidgetGridItem
import com.eblan.launcher.feature.home.model.Screen

@Composable
fun FolderScreen(
    modifier: Modifier = Modifier,
    folders: ArrayDeque<GridItemData.Folder>,
    folderRows: Int,
    folderColumns: Int,
    textColor: Long,
    rootHeight: Int,
    onUpdateScreen: (Screen) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
) {
    val density = LocalDensity.current

    val gridPadding = 30.dp

    val gridHeight = with(density) {
        rootHeight.toDp()
    }

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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight)
                        .padding(gridPadding),
                ) {
                    Text(text = folderData.label)

                    GridLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color(textColor).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp),
                            )
                            .border(
                                width = 2.dp,
                                color = Color(textColor),
                                shape = RoundedCornerShape(8.dp),
                            ),
                        rows = folderRows,
                        columns = folderColumns,
                    ) {
                        folderData.gridItems.forEach { gridItem ->
                            when (val data = gridItem.data) {
                                is GridItemData.ApplicationInfo -> {
                                    InteractiveApplicationInfoGridItem(
                                        textColor = textColor,
                                        gridItem = gridItem,
                                        data = data,
                                        onTap = {

                                        },
                                        onLongPress = {

                                        },
                                    )
                                }

                                is GridItemData.Widget -> {
                                    InteractiveWidgetGridItem(
                                        gridItem = gridItem,
                                        gridItemData = data,
                                        onLongPress = {

                                        },
                                    )
                                }

                                is GridItemData.ShortcutInfo -> {
                                    InteractiveShortcutInfoGridItem(
                                        textColor = textColor,
                                        gridItem = gridItem,
                                        data = data,
                                        onTap = {

                                        },
                                        onLongPress = {

                                        },
                                    )
                                }

                                is GridItemData.Folder -> {
                                    InteractiveNestedFolderGridItem(
                                        textColor = textColor,
                                        gridItem = gridItem,
                                        data = data,
                                        onTap = {
                                            onAddFolder(gridItem.id)
                                        },
                                        onLongPress = {

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