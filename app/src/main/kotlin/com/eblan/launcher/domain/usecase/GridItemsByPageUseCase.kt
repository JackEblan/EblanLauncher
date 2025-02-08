package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.areValidCells
import com.eblan.launcher.domain.grid.calculateBoundingBox
import com.eblan.launcher.domain.grid.calculateCoordinates
import com.eblan.launcher.domain.model.GridItemPixel
import com.eblan.launcher.domain.model.ScreenDimension
import com.eblan.launcher.repository.GridRepository
import com.eblan.launcher.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class GridItemsByPageUseCase(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(
        screenDimension: ScreenDimension,
    ): Flow<Map<Int, List<GridItemPixel>>> {
        return gridRepository.gridItems.onStart { gridRepository.insertGridItems() }
            .map { gridItems ->
                val userData = userDataRepository.userData.first()

                gridItems.filter { gridItem ->
                    areValidCells(
                        gridCells = gridItem.cells, rows = userData.rows, columns = userData.columns
                    )
                }.map { gridItem ->
                    val boundingBox = calculateBoundingBox(
                        gridCells = gridItem.cells,
                        rows = userData.rows,
                        columns = userData.columns,
                        screenWidth = screenDimension.screenWidth,
                        screenHeight = screenDimension.screenHeight,
                    )

                    val coordinates = calculateCoordinates(
                        gridCells = gridItem.cells,
                        rows = userData.rows,
                        columns = userData.columns,
                        screenWidth = screenDimension.screenWidth,
                        screenHeight = screenDimension.screenHeight,
                    )

                    GridItemPixel(
                        gridItem = gridItem, boundingBox = boundingBox, coordinates = coordinates
                    )
                }
            }.map { gridItemPixels ->
                gridItemPixels.groupBy { gridItemPixel -> gridItemPixel.gridItem.page }
            }.flowOn(Dispatchers.Default)
    }
}