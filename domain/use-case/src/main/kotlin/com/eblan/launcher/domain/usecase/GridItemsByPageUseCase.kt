package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.areValidCells
import com.eblan.launcher.domain.grid.calculateBoundingBox
import com.eblan.launcher.domain.grid.calculateCoordinates
import com.eblan.launcher.domain.model.GridItemPixel
import com.eblan.launcher.domain.model.ScreenDimension
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GridItemsByPageUseCase @Inject constructor(
    private val gridRepository: GridRepository,
) {
    operator fun invoke(
        screenDimension: ScreenDimension,
        rows: Int,
        columns: Int,
    ): Flow<Map<Int, List<GridItemPixel>>> {
        return gridRepository.gridItems.map { gridItems ->
                gridItems.filter { gridItem ->
                    areValidCells(
                        gridCells = gridItem.cells, rows = rows, columns = columns,
                    )
                }.map { gridItem ->
                    val boundingBox = calculateBoundingBox(
                        gridCells = gridItem.cells,
                        rows = rows,
                        columns = columns,
                        screenWidth = screenDimension.screenWidth,
                        screenHeight = screenDimension.screenHeight,
                    )

                    val coordinates = calculateCoordinates(
                        gridCells = gridItem.cells,
                        rows = rows,
                        columns = columns,
                        screenWidth = screenDimension.screenWidth,
                        screenHeight = screenDimension.screenHeight,
                    )

                    GridItemPixel(
                        gridItem = gridItem, boundingBox = boundingBox, coordinates = coordinates,
                    )
                }
            }.map { gridItemPixels ->
                gridItemPixels.groupBy { gridItemPixel -> gridItemPixel.gridItem.page }
            }.flowOn(Dispatchers.Default)
    }
}