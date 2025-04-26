package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.resizeWidgetGridItemWithPixels
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ResizeWidgetGridItemUseCase @Inject constructor(
    private val shiftAlgorithmUseCase: ShiftAlgorithmUseCase,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(
        gridItem: GridItem,
        width: Int,
        height: Int,
        gridWidth: Int,
        gridHeight: Int,
        dockHeight: Int,
        anchor: SideAnchor,
    ) {
        val userData = userDataRepository.userData.first()

        when (gridItem.associate) {
            Associate.Grid -> {
                val resizingGridItem = resizeWidgetGridItemWithPixels(
                    gridItem = gridItem,
                    width = width,
                    height = height,
                    rows = userData.rows,
                    columns = userData.columns,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    anchor = anchor,
                )

                shiftAlgorithmUseCase(
                    rows = userData.rows,
                    columns = userData.columns,
                    movingGridItem = resizingGridItem,
                )
            }

            Associate.Dock -> {
                val resizingGridItem = resizeWidgetGridItemWithPixels(
                    gridItem = gridItem,
                    width = width,
                    height = height,
                    rows = userData.dockRows,
                    columns = userData.dockColumns,
                    gridWidth = gridWidth,
                    gridHeight = dockHeight,
                    anchor = anchor,
                )

                shiftAlgorithmUseCase(
                    rows = userData.rows,
                    columns = userData.columns,
                    movingGridItem = resizingGridItem,
                )
            }
        }
    }
}