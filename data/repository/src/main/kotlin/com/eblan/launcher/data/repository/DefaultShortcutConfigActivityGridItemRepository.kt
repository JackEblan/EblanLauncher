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
import com.eblan.launcher.data.repository.mapper.asGridItem
import com.eblan.launcher.data.repository.mapper.asModel
import com.eblan.launcher.data.room.dao.ShortcutConfigActivityGridItemDao
import com.eblan.launcher.domain.model.ShortcutConfigActivityGridItem
import com.eblan.launcher.domain.model.UpdateApplicationInfoGridItem
import com.eblan.launcher.domain.repository.ShortcutConfigActivityGridItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultShortcutConfigActivityGridItemRepository @Inject constructor(private val shortcutConfigActivityGridItemDao: ShortcutConfigActivityGridItemDao) :
    ShortcutConfigActivityGridItemRepository {
    override val gridItems =
        shortcutConfigActivityGridItemDao.getShortcutConfigActivityGridItemEntities()
            .map { entities ->
                entities.map { entity ->
                    entity.asGridItem()
                }
            }

    override val shortcutConfigActivityGridItems: Flow<List<ShortcutConfigActivityGridItem>> =
        shortcutConfigActivityGridItemDao.getShortcutConfigActivityGridItemEntities()
            .map { entities ->
                entities.map { entity ->
                    entity.asModel()
                }
            }

    override suspend fun upsertShortcutConfigActivityGridItems(shortcutConfigActivityGridItems: List<ShortcutConfigActivityGridItem>) {
        val entities = shortcutConfigActivityGridItems.map { shortcutConfigActivityGridItem ->
            shortcutConfigActivityGridItem.asEntity()
        }

        shortcutConfigActivityGridItemDao.upsertShortcutConfigActivityGridItemEntities(entities = entities)
    }

    override suspend fun updateShortcutConfigActivityGridItem(shortcutConfigActivityGridItem: ShortcutConfigActivityGridItem) {
        shortcutConfigActivityGridItemDao.updateShortcutConfigActivityGridItemEntity(
            shortcutConfigActivityGridItem.asEntity(),
        )
    }

    override suspend fun deleteShortcutConfigActivityGridItems(shortcutConfigActivityGridItems: List<ShortcutConfigActivityGridItem>) {
        val entities = shortcutConfigActivityGridItems.map { shortcutConfigActivityGridItem ->
            shortcutConfigActivityGridItem.asEntity()
        }

        shortcutConfigActivityGridItemDao.deleteShortcutConfigActivityGridItemEntities(entities = entities)
    }

    override suspend fun deleteShortcutConfigActivityGridItem(shortcutConfigActivityGridItem: ShortcutConfigActivityGridItem) {
        shortcutConfigActivityGridItemDao.deleteShortcutConfigActivityGridItemEntity(entity = shortcutConfigActivityGridItem.asEntity())
    }

    override suspend fun getShortcutConfigActivityGridItems(
        serialNumber: Long,
        packageName: String,
    ): List<ShortcutConfigActivityGridItem> {
        return shortcutConfigActivityGridItemDao.getShortcutConfigActivityGridItemEntities(
            serialNumber = serialNumber,
            packageName = packageName,
        ).map { entity ->
            entity.asModel()
        }
    }

    override suspend fun deleteShortcutConfigActivityGridItem(
        serialNumber: Long,
        packageName: String,
    ) {
        shortcutConfigActivityGridItemDao.deleteShortcutConfigActivityGridItemEntity(
            serialNumber = serialNumber,
            packageName = packageName,
        )
    }

    override suspend fun updateShortcutConfigActivityGridItems(
        updateApplicationInfoGridItems: List<UpdateApplicationInfoGridItem>,
    ) {
        shortcutConfigActivityGridItemDao.updateShortcutConfigActivityGridItemEntities(
            updateApplicationInfoGridItems = updateApplicationInfoGridItems,
        )
    }
}
