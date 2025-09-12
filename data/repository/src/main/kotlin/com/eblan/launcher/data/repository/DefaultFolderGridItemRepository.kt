/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.data.repository

import com.eblan.launcher.data.repository.mapper.asEntity
import com.eblan.launcher.data.repository.mapper.asFolderGridItemData
import com.eblan.launcher.data.repository.mapper.asGridItem
import com.eblan.launcher.data.room.dao.FolderGridItemDao
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultFolderGridItemRepository @Inject constructor(private val folderGridItemDao: FolderGridItemDao) :
    FolderGridItemRepository {
    override val folderGridItems =
        folderGridItemDao.getFolderGridItemWrapperEntities().map { entities ->
            entities.map { entity ->
                entity.asGridItem()
            }
        }

    override suspend fun getFolderGridItemData(id: String): GridItemData.Folder? {
        return folderGridItemDao.getFolderGridItemWrapperEntity(id = id)?.asFolderGridItemData()
    }

    override suspend fun upsertFolderGridItems(folderGridItems: List<FolderGridItem>) {
        val entities = folderGridItems.map { folderGridItem ->
            folderGridItem.asEntity()
        }

        folderGridItemDao.upsertFolderGridItemEntities(entities = entities)
    }

    override suspend fun updateFolderGridItem(folderGridItem: FolderGridItem) {
        folderGridItemDao.updateFolderGridItemEntity(entity = folderGridItem.asEntity())
    }

    override suspend fun deleteFolderGridItem(folderGridItem: FolderGridItem) {
        folderGridItemDao.deleteFolderGridItemEntity(entity = folderGridItem.asEntity())
    }

    override suspend fun deleteFolderGridItems(folderGridItems: List<FolderGridItem>) {
        val entities = folderGridItems.map { folderGridItem ->
            folderGridItem.asEntity()
        }

        folderGridItemDao.deleteFolderGridItemEntities(entities = entities)
    }
}
