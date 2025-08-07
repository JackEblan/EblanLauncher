package com.eblan.launcher.feature.home.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.InteractiveApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.InteractiveFolderGridItem
import com.eblan.launcher.feature.home.component.grid.InteractiveShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.InteractiveWidgetGridItem

@Composable
fun FolderDialog(
    modifier: Modifier = Modifier,
    folderRows: Int = 5,
    folderColumns: Int = 5,
    textColor: Long,
    folderData: GridItemData.Folder?,
    onDismissRequest: () -> Unit,
) {
    if (folderData != null) {
        Dialog(onDismissRequest = onDismissRequest) {
            Surface {
                Column(modifier = modifier.fillMaxWidth()) {
                    Text(text = folderData.label)

                    Spacer(modifier = Modifier.height(10.dp))

                    GridLayout(
                        modifier = Modifier.weight(1f),
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
                                    InteractiveFolderGridItem(
                                        textColor = textColor,
                                        gridItem = gridItem,
                                        data = data,
                                        onTap = {

                                        },
                                        onLongPress = {

                                        },
                                    )
                                }
                            }
                        }
                    }

                    TextButton(
                        modifier = Modifier.align(Alignment.End),
                        onClick = onDismissRequest,
                    ) {
                        Text(text = "Dismiss")
                    }
                }
            }
        }
    }
}