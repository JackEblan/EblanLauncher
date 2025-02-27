package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.BoundingBox
import com.eblan.launcher.domain.model.Coordinates
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
            gridItems.map { gridItem ->
                val cellWidth = screenDimension.screenWidth / columns
                val cellHeight = screenDimension.screenHeight / rows

                val boundingBox = BoundingBox(
                    width = gridItem.columnSpan * cellWidth,
                    height = gridItem.rowSpan * cellHeight,
                )

                val coordinates = Coordinates(
                    x = gridItem.startColumn * cellWidth,
                    y = gridItem.startRow * cellHeight,
                )

                GridItemPixel(
                    gridItem = gridItem,
                    boundingBox = boundingBox,
                    coordinates = coordinates,
                )
            }
        }.map { gridItemPixels ->
            gridItemPixels.groupBy { gridItemPixel -> gridItemPixel.gridItem.page }
        }.flowOn(Dispatchers.Default)
    }
}