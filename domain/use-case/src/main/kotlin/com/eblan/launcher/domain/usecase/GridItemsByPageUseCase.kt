package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.areValidCells
import com.eblan.launcher.domain.grid.calculateBoundingBox
import com.eblan.launcher.domain.grid.calculateCoordinates
import com.eblan.launcher.domain.grid.isGridItemWithinBounds
import com.eblan.launcher.domain.grid.resizeGridItemWithPixels
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemPixel
import com.eblan.launcher.domain.model.ScreenDimension
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class GridItemsByPageUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
    private val aStarGridAlgorithmUseCase: AStarGridAlgorithmUseCase,
) {
    operator fun invoke(
        screenDimension: ScreenDimension,
        rows: Int,
        columns: Int,
    ): Flow<Map<Int, List<GridItemPixel>>> {
        return gridRepository.gridItems.mapLatest { gridItems ->
            gridItems.filter { gridItem ->
                areValidCells(
                    gridCells = gridItem.cells, rows = rows, columns = columns,
                )
            }.map { gridItem ->
                when (val data = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
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
                            gridItem = gridItem,
                            boundingBox = boundingBox,
                            coordinates = coordinates,
                        )
                    }

                    is GridItemData.Widget -> {
                        val userData = userDataRepository.userData.first()

                        val gridCellWidth = screenDimension.screenWidth / userData.rows

                        val gridCellHeight = screenDimension.screenHeight / userData.columns

                        val resizedGridItem = resizeGridItemWithPixels(
                            gridItem = gridItem,
                            width = data.minWidth,
                            height = data.minHeight,
                            gridCellWidth = gridCellWidth,
                            gridCellHeight = gridCellHeight,
                            anchor = Anchor.TOP_START,
                        )

                        val boundingBox = calculateBoundingBox(
                            gridCells = resizedGridItem.cells,
                            rows = rows,
                            columns = columns,
                            screenWidth = screenDimension.screenWidth,
                            screenHeight = screenDimension.screenHeight,
                        )

                        val coordinates = calculateCoordinates(
                            gridCells = resizedGridItem.cells,
                            rows = rows,
                            columns = columns,
                            screenWidth = screenDimension.screenWidth,
                            screenHeight = screenDimension.screenHeight,
                        )

                        if (isGridItemWithinBounds(
                                x = coordinates.x,
                                y = coordinates.y,
                                boundingBoxWidth = boundingBox.width,
                                boundingBoxHeight = boundingBox.height,
                                screenWidth = screenDimension.screenWidth,
                                screenHeight = screenDimension.screenHeight,
                            ).not()
                        ) {
                            aStarGridAlgorithmUseCase(gridItem = resizedGridItem)
                        }

                        GridItemPixel(
                            gridItem = resizedGridItem,
                            boundingBox = boundingBox,
                            coordinates = coordinates,
                        )
                    }

                    is GridItemData.WidgetAndroidTwelve -> {
                        val userData = userDataRepository.userData.first()

                        val gridCellWidth = screenDimension.screenWidth / userData.rows

                        val gridCellHeight = screenDimension.screenHeight / userData.columns

                        val width = if (data.targetCellWidth != 0) {
                            gridCellWidth * data.targetCellWidth
                        } else {
                            data.minWidth
                        }

                        val height = if (data.targetCellHeight != 0) {
                            gridCellHeight * data.targetCellHeight
                        } else {
                            data.minHeight
                        }

                        val resizedGridItem = resizeGridItemWithPixels(
                            gridItem = gridItem,
                            width = width,
                            height = height,
                            gridCellWidth = gridCellWidth,
                            gridCellHeight = gridCellHeight,
                            anchor = Anchor.TOP_START,
                        )

                        val boundingBox = calculateBoundingBox(
                            gridCells = resizedGridItem.cells,
                            rows = rows,
                            columns = columns,
                            screenWidth = screenDimension.screenWidth,
                            screenHeight = screenDimension.screenHeight,
                        )

                        val coordinates = calculateCoordinates(
                            gridCells = resizedGridItem.cells,
                            rows = rows,
                            columns = columns,
                            screenWidth = screenDimension.screenWidth,
                            screenHeight = screenDimension.screenHeight,
                        )

                        if (isGridItemWithinBounds(
                                x = coordinates.x,
                                y = coordinates.y,
                                boundingBoxWidth = boundingBox.width,
                                boundingBoxHeight = boundingBox.height,
                                screenWidth = screenDimension.screenWidth,
                                screenHeight = screenDimension.screenHeight,
                            ).not()
                        ) {
                            aStarGridAlgorithmUseCase(gridItem = resizedGridItem)
                        }

                        GridItemPixel(
                            gridItem = resizedGridItem,
                            boundingBox = boundingBox,
                            coordinates = coordinates,
                        )
                    }

                    null -> {
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
                            gridItem = gridItem,
                            boundingBox = boundingBox,
                            coordinates = coordinates,
                        )
                    }
                }
            }
        }.map { gridItemPixels ->
            gridItemPixels.groupBy { gridItemPixel -> gridItemPixel.gridItem.page }
        }.flowOn(Dispatchers.Default)
    }
}