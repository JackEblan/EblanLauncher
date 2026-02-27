package com.eblan.launcher.feature.home.screen.folderdrag

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.model.Drag

internal fun handleDragGridItem(
    drag: Drag,
    dragIntOffset: IntOffset,
    gridItemDataFolder: GridItemData.Folder?,
    folderGridIntOffset: IntOffset,
    folderGridIntSize: IntSize,
) {
    if (drag == Drag.None ||
        drag == Drag.End ||
        drag == Drag.Cancel
    ) {
        return
    }

    val folderX = dragIntOffset.x - folderGridIntOffset.x

    val folderY = dragIntOffset.y - folderGridIntOffset.y

    val isInsideFolder = folderX in 0..folderGridIntSize.width &&
            folderY in 0..folderGridIntSize.height

    if (gridItemDataFolder != null && isInsideFolder) {
        println("Inside Folder")
    }
}
