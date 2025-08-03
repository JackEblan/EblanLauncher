package com.eblan.launcher.data.repository

import com.eblan.launcher.data.repository.mapper.asGridItem
import com.eblan.launcher.data.room.dao.FolderGridItemDao
import com.eblan.launcher.data.room.entity.FolderGridItemWrapperEntity
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultFolderGridItemRepository @Inject constructor(folderGridItemDao: FolderGridItemDao) :
    FolderGridItemRepository {
    override val folderGridItems =
        folderGridItemDao.getFolderGridItemWrapperEntities().map { entities ->
            entities.map { entity ->
                entity.asModel()
            }
        }

    private fun FolderGridItemWrapperEntity.asModel(): GridItem {
        val applicationInfoGridItems = applicationInfos?.map { applicationInfoGridItemEntity ->
            applicationInfoGridItemEntity.asGridItem()
        } ?: emptyList()

        val widgetGridItems = widgets?.map { widgetGridItemEntity ->
            widgetGridItemEntity.asGridItem()
        } ?: emptyList()

        val shortcutInfos = shortcutInfos?.map { shortcutGridItemEntity ->
            shortcutGridItemEntity.asGridItem()
        } ?: emptyList()

        val data =
            GridItemData.Folder(gridItems = applicationInfoGridItems + widgetGridItems + shortcutInfos)

        return GridItem(
            id = folderGridItemEntity.id,
            folderId = null,
            page = folderGridItemEntity.page,
            startRow = folderGridItemEntity.startRow,
            startColumn = folderGridItemEntity.startColumn,
            rowSpan = folderGridItemEntity.rowSpan,
            columnSpan = folderGridItemEntity.columnSpan,
            data = data,
            associate = folderGridItemEntity.associate,
        )
    }
}