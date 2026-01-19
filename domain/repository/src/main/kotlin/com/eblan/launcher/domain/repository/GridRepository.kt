package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.GridItem

interface GridRepository {
    suspend fun updateGridItem(gridItem: GridItem)

    suspend fun restoreGridItem(gridItem: GridItem): GridItem

    suspend fun updateGridItems(gridItems: List<GridItem>)

    suspend fun deleteGridItems(gridItems: List<GridItem>)

    suspend fun deleteGridItem(gridItem: GridItem)
}