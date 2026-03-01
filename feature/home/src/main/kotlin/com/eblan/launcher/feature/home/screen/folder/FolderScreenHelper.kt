package com.eblan.launcher.feature.home.screen.folder

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

internal fun getFolderScreenOffset(
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    folderGridWidthPx: Int,
    folderGridHeightPx: Int,
    safeDrawingWidth: Int,
    safeDrawingHeight: Int,
): IntOffset {
    val centeredX =
        folderPopupIntOffset.x + (folderPopupIntSize.width / 2) - (folderGridWidthPx / 2)

    val centeredY =
        folderPopupIntOffset.y + (folderPopupIntSize.height / 2) - (folderGridHeightPx / 2)

    val popupX = centeredX.coerceIn(0, safeDrawingWidth - folderGridWidthPx)
    val popupY = centeredY.coerceIn(0, safeDrawingHeight - folderGridHeightPx)

    return IntOffset(
        x = popupX,
        y = popupY,
    )
}