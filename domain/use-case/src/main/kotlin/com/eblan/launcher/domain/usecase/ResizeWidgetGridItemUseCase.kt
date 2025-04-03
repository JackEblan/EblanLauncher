package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.resizeWidgetGridItemWithPixels
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.SideAnchor
import javax.inject.Inject

class ResizeWidgetGridItemUseCase @Inject constructor(
    private val shiftAlgorithmUseCase: ShiftAlgorithmUseCase,
) {
    suspend operator fun invoke(
        page: Int,
        gridItem: GridItem,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: SideAnchor,
    ) {
        val resizingGridItem = resizeWidgetGridItemWithPixels(
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