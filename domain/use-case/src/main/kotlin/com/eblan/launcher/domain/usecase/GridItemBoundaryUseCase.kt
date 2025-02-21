package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.getGridItemBoundary
import com.eblan.launcher.domain.model.GridItemBoundary
import com.eblan.launcher.domain.model.GridItemPixel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GridItemBoundaryUseCase @Inject constructor() {
    suspend operator fun invoke(
        x: Int,
        screenWidth: Int,
        gridItemPixel: GridItemPixel?,
    ): GridItemBoundary? {
        if (gridItemPixel == null) return null

        return withContext(Dispatchers.Default) {
            getGridItemBoundary(
                x = x,
                boundingBoxWidth = gridItemPixel.boundingBox.width,
                screenWidth = screenWidth,
            )
        }
    }
}