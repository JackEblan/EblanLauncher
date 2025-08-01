package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.GridItem
import kotlinx.coroutines.flow.Flow

interface ApplicationInfoGridItemRepository {
    val applicationInfoGridItems: Flow<List<GridItem>>

    suspend fun upsertApplicationInfoGridItems(applicationInfoGridItems: List<ApplicationInfoGridItem>)

    suspend fun upsertApplicationInfoGridItem(applicationInfoGridItem: ApplicationInfoGridItem): Long

    suspend fun updateApplicationInfoGridItem(applicationInfoGridItem: ApplicationInfoGridItem)

    suspend fun getApplicationInfoGridItem(id: String): ApplicationInfoGridItem?

    suspend fun deleteApplicationInfoGridItems(applicationInfoGridItems: List<ApplicationInfoGridItem>)

    suspend fun deleteApplicationInfoGridItem(applicationInfoGridItem: ApplicationInfoGridItem)
}