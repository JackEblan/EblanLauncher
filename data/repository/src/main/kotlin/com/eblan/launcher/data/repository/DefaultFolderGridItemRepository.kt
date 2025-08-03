package com.eblan.launcher.data.repository

import com.eblan.launcher.data.repository.mapper.asModels
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
        val applicationInfoGridItems = applicationInfos.asModels()

        val widgetGridItems = widgets.asModels()

        val shortcutInfos = shortcutInfos.asModels()

        val data =
            GridItemData.Folder(gridItems = applicationInfoGridItems + widgetGridItems + shortcutInfos)

        return GridItem(
            id = folderGridItemEntity.id,
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