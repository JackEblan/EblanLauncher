package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.moveGridItemWithCoordinates
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemMovement
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MoveGridItemUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val aStarGridAlgorithmUseCase: AStarGridAlgorithmUseCase,
) {
    suspend operator fun invoke(
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        width: Int,
        screenWidth: Int,
        screenHeight: Int,
    ): GridItemMovement? {
        val userData = userDataRepository.userData.first()

        return if ((x + width / 2) <= 0) {
            GridItemMovement.Left
        } else if ((x + width / 2) >= screenWidth) {
            GridItemMovement.Right
        } else {
            val movingGridItem = moveGridItemWithCoordinates(
                gridItem = gridItem,
                x = x,
                y = y,
                rows = userData.rows,
                columns = userData.columns,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
            ).copy(page = page)

            if (aStarGridAlgorithmUseCase(gridItem = movingGridItem) != null) {
                GridItemMovement.Inside
            } else {
                null
            }
        }
    }
}