package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.resizeGridItemWithPixels
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import javax.inject.Inject

class ResizeGridItemUseCase @Inject constructor(
    private val shiftAlgorithmUseCase: ShiftAlgorithmUseCase,
) {
    suspend operator fun invoke(
        page: Int,
        gridItem: GridItem,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) {
        val resizingGridItem = resizeGridItemWithPixels(
            gridItem = gridItem,
            width = width,
            height = height,
            gridCellWidth = cellWidth,
            gridCellHeight = cellHeight,
            anchor = anchor,
        ).copy(page = page)

        shiftAlgorithmUseCase(movingGridItem = resizingGridItem)
    }
}