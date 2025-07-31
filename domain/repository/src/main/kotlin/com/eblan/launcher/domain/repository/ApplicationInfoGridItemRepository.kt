package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import kotlinx.coroutines.flow.Flow

interface ApplicationInfoGridItemRepository {
    val applicationInfoGridItems: Flow<List<ApplicationInfoGridItem>>

    suspend fun upsertApplicationInfoGridItems(applicationInfoGridItems: List<ApplicationInfoGridItem>)

    suspend fun upsertApplicationInfoGridItem(applicationInfoGridItem: ApplicationInfoGridItem): Long

    suspend fun updateApplicationInfoGridItem(applicationInfoGridItem: ApplicationInfoGridItem)

    suspend fun getApplicationInfoGridItem(id: String): ApplicationInfoGridItem?

    suspend fun deleteApplicationInfoGridItems(applicationInfoGridItems: List<ApplicationInfoGridItem>)

    suspend fun deleteApplicationInfoGridItem(applicationInfoGridItem: ApplicationInfoGridItem)
}