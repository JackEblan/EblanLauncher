package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.moveGridItemWithCoordinates
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MoveGridItemUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val shiftAlgorithmUseCase: ShiftAlgorithmUseCase,
) {
    suspend operator fun invoke(
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) {
        val userData = userDataRepository.userData.first()

        if (y > gridHeight - userData.dockHeight) {
            val dockY = y - (gridHeight - userData.dockHeight)

            val movingGridItem = moveGridItemWithCoordinates(
                gridItem = gridItem,
                x = x,
                y = dockY,
                rows = userData.dockRows,
                columns = userData.dockColumns,
                gridWidth = gridWidth,
                gridHeight = userData.dockHeight,
            ).copy(
                page = page,
                associate = Associate.Dock,
            )

            shiftAlgorithmUseCase(
                rows = userData.dockRows,
                columns = userData.dockColumns,
                movingGridItem = movingGridItem,
            )
        } else {
            val movingGridItem = moveGridItemWithCoordinates(
                gridItem = gridItem,
                x = x,
                y = y,
                rows = userData.rows,
                columns = userData.columns,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
            ).copy(
                page = page,
                associate = Associate.Grid,
            )

            shiftAlgorithmUseCase(
                rows = userData.rows,
                columns = userData.columns,
                movingGridItem = movingGridItem,
            )
        }
    }
}