package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.GridItem
import kotlinx.coroutines.flow.Flow

interface GridRepository {
    val gridItems: Flow<List<GridItem>>

    suspend fun insertGridItems()

    suspend fun updateGridItems(gridItems: List<GridItem>)
}