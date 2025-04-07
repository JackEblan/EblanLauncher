package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.moveGridItemWithCoordinates
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
        screenWidth: Int,
        screenHeight: Int,
    ) {
        val userData = userDataRepository.userData.first()

        val movingGridItem = moveGridItemWithCoordinates(
            gridItem = gridItem,
            x = x,
            y = y,
            rows = userData.rows,
            columns = userData.columns,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
        ).copy(page = page)

        shiftAlgorithmUseCase(movingGridItem = movingGridItem)
    }
}