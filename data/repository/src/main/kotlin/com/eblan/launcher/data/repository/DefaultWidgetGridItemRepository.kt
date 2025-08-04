package com.eblan.launcher.data.repository

import com.eblan.launcher.data.repository.mapper.asEntity
import com.eblan.launcher.data.repository.mapper.asGridItem
import com.eblan.launcher.data.repository.mapper.asModel
import com.eblan.launcher.data.room.dao.WidgetGridItemDao
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultWidgetGridItemRepository @Inject constructor(private val widgetGridItemDao: WidgetGridItemDao) :
    WidgetGridItemRepository {
    override val widgetGridItems =
        widgetGridItemDao.getWidgetGridItemEntities().map { entities ->
            entities.filter { entity -> entity.folderId == null }
                .map { entity ->
                    entity.asGridItem()
                }
        }

    override suspend fun upsertWidgetGridItems(widgetGridItems: List<WidgetGridItem>) {
        val entities = widgetGridItems.map { widgetGridItem ->
            widgetGridItem.asEntity()
        }

        widgetGridItemDao.upsertWidgetGridItemEntities(entities = entities)
    }

    override suspend fun upsertWidgetGridItem(widgetGridItem: WidgetGridItem): Long {
        return widgetGridItemDao.upsertWidgetGridItemEntity(
            widgetGridItem.asEntity(),
        )
    }

    override suspend fun updateWidgetGridItem(widgetGridItem: WidgetGridItem) {
        widgetGridItemDao.updateWidgetGridItemEntity(
            widgetGridItem.asEntity(),
        )
    }

    override suspend fun getWidgetGridItem(id: String): WidgetGridItem? {
        return widgetGridItemDao.getWidgetGridItemEntity(id = id)?.asModel()
    }

    override suspend fun deleteWidgetGridItems(widgetGridItems: List<WidgetGridItem>) {
        val entities = widgetGridItems.map { widgetGridItem ->
            widgetGridItem.asEntity()
        }

        widgetGridItemDao.deleteWidgetGridItemEntities(entities = entities)
    }

    override suspend fun deleteWidgetGridItem(widgetGridItem: WidgetGridItem) {
        widgetGridItemDao.deleteWidgetGridItemEntity(entity = widgetGridItem.asEntity())
    }
}