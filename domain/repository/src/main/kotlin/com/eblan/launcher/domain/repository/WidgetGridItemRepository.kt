package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.WidgetGridItem
import kotlinx.coroutines.flow.Flow

interface WidgetGridItemRepository {
    val widgetGridItems: Flow<List<WidgetGridItem>>

    suspend fun upsertWidgetGridItems(widgetGridItems: List<WidgetGridItem>)

    suspend fun upsertWidgetGridItem(widgetGridItem: WidgetGridItem): Long

    suspend fun updateWidgetGridItem(widgetGridItem: WidgetGridItem)

    suspend fun getWidgetGridItem(id: String): WidgetGridItem?

    suspend fun deleteWidgetGridItems(widgetGridItems: List<WidgetGridItem>)

    suspend fun deleteWidgetGridItem(widgetGridItem: WidgetGridItem)
}