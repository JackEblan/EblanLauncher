package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.areValidCells
import com.eblan.launcher.domain.grid.calculateBoundingBox
import com.eblan.launcher.domain.grid.calculateCoordinates
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemPixel
import com.eblan.launcher.domain.model.ScreenDimension
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.InMemoryApplicationInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GridItemsByPageUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val inMemoryApplicationInfoRepository: InMemoryApplicationInfoRepository,
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

                val data = when (val gridItemData = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        getApplicationInfoIcon(gridItemData = gridItemData)
                    }

                    is GridItemData.Widget -> null

                    null -> null
                }

                GridItemPixel(
                    gridItem = gridItem, boundingBox = boundingBox, coordinates = coordinates,
                    data = data,
                )
            }
        }.map { gridItemPixels ->
            gridItemPixels.groupBy { gridItemPixel -> gridItemPixel.gridItem.page }
        }.flowOn(Dispatchers.Default)
    }

    private suspend fun getApplicationInfoIcon(gridItemData: GridItemData.ApplicationInfo): GridItemData.ApplicationInfo {
        val icon = inMemoryApplicationInfoRepository.applicationInfos.first()
            .find { inMemoryApplicationInfo ->
                inMemoryApplicationInfo.packageName == gridItemData.packageName
            }?.icon ?: ByteArray(0)

        return gridItemData.copy(icon = icon)
    }
}