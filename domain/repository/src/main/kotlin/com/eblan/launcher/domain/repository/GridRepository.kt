package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.flow.Flow

interface GridRepository {
    val gridItems: Flow<List<GridItem>>

    suspend fun upsertGridItems(gridItems: List<GridItem>)

    suspend fun upsertGridItem(gridItem: GridItem)

    suspend fun updateGridItemData(id: Int, data: GridItemData): Int

    suspend fun getGridItem(id: Int): GridItem?
}