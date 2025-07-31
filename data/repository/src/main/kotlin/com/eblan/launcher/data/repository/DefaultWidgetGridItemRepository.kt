package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.WidgetGridItemDao
import com.eblan.launcher.data.room.entity.WidgetGridItemEntity
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultWidgetGridItemRepository @Inject constructor(private val widgetGridItemDao: WidgetGridItemDao) :
    WidgetGridItemRepository {
    override val widgetGridItems =
        widgetGridItemDao.getWidgetGridItemEntities().map { entities ->
            entities.map { entity ->
                entity.asModel()
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

    private fun WidgetGridItemEntity.asModel(): WidgetGridItem {
        return WidgetGridItem(
            id = id,
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            associate = associate,
            appWidgetId = appWidgetId,
            packageName = packageName,
            componentName = componentName,
            configure = configure,
            minWidth = minWidth,
            minHeight = minHeight,
            resizeMode = resizeMode,
            minResizeWidth = minResizeWidth,
            minResizeHeight = minResizeHeight,
            maxResizeWidth = maxResizeWidth,
            maxResizeHeight = maxResizeHeight,
            targetCellHeight = targetCellHeight,
            targetCellWidth = targetCellWidth,
            preview = preview,
        )
    }

    private fun WidgetGridItem.asEntity(): WidgetGridItemEntity {
        return WidgetGridItemEntity(
            id = id,
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            associate = associate,
            appWidgetId = appWidgetId,
            packageName = packageName,
            componentName = componentName,
            configure = configure,
            minWidth = minWidth,
            minHeight = minHeight,
            resizeMode = resizeMode,
            minResizeWidth = minResizeWidth,
            minResizeHeight = minResizeHeight,
            maxResizeWidth = maxResizeWidth,
            maxResizeHeight = maxResizeHeight,
            targetCellHeight = targetCellHeight,
            targetCellWidth = targetCellWidth,
            preview = preview,
        )
    }
}