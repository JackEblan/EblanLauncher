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
import com.eblan.launcher.data.room.dao.ApplicationInfoFolderGridItemDao
import com.eblan.launcher.domain.model.ApplicationInfoFolderGridItem
import com.eblan.launcher.domain.model.UpdateApplicationInfoFolderGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoFolderGridItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultApplicationInfoFolderGridItemRepository @Inject constructor(private val applicationInfoFolderGridItemDao: ApplicationInfoFolderGridItemDao) :
    ApplicationInfoFolderGridItemRepository {
    override val applicationInfoFolderGridItems: Flow<List<ApplicationInfoFolderGridItem>> =
        applicationInfoFolderGridItemDao.getApplicationInfoFolderGridItemEntities().map { entities ->
            entities.map { entity ->
                entity.asGridItem()
            }
        }

    override suspend fun upsertApplicationInfoFolderGridItems(applicationInfoFolderGridItems: List<ApplicationInfoFolderGridItem>) {
        val entities = applicationInfoFolderGridItems.map { applicationInfoFolderGridItem ->
            applicationInfoFolderGridItem.asEntity()
        }

        applicationInfoFolderGridItemDao.upsertApplicationInfoFolderGridItemEntities(entities = entities)
    }

    override suspend fun updateApplicationInfoFolderGridItem(applicationInfoFolderGridItem: ApplicationInfoFolderGridItem) {
        applicationInfoFolderGridItemDao.updateApplicationInfoFolderGridItemEntity(
            applicationInfoFolderGridItem.asEntity(),
        )
    }

    override suspend fun deleteApplicationInfoFolderGridItems(applicationInfoFolderGridItems: List<ApplicationInfoFolderGridItem>) {
        val entities = applicationInfoFolderGridItems.map { applicationInfoFolderGridItem ->
            applicationInfoFolderGridItem.asEntity()
        }

        applicationInfoFolderGridItemDao.deleteApplicationInfoFolderGridItemEntities(entities = entities)
    }

    override suspend fun deleteApplicationInfoFolderGridItem(applicationInfoFolderGridItem: ApplicationInfoFolderGridItem) {
        applicationInfoFolderGridItemDao.deleteApplicationInfoFolderGridItemEntity(entity = applicationInfoFolderGridItem.asEntity())
    }

    override suspend fun getApplicationInfoFolderGridItemsByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<ApplicationInfoFolderGridItem> =
        applicationInfoFolderGridItemDao.getApplicationInfoFolderGridItemEntitiesByPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        ).map { entity ->
            entity.asGridItem()
        }

    override suspend fun deleteApplicationInfoFolderGridItem(
        serialNumber: Long,
        packageName: String,
    ) {
        applicationInfoFolderGridItemDao.deleteApplicationInfoFolderGridItemEntity(
            serialNumber = serialNumber,
            packageName = packageName,
        )
    }

    override suspend fun updateApplicationInfoFolderGridItems(updateApplicationInfoFolderGridItems: List<UpdateApplicationInfoFolderGridItem>) {
        applicationInfoFolderGridItemDao.updateApplicationInfoFolderGridItemEntities(
            updateApplicationInfoFolderGridItems = updateApplicationInfoFolderGridItems,
        )
    }

    override suspend fun insertApplicationInfoFolderGridItem(applicationInfoFolderGridItem: ApplicationInfoFolderGridItem) {
        applicationInfoFolderGridItemDao.insertApplicationInfoFolderGridItemEntity(entity = applicationInfoFolderGridItem.asEntity())
    }
}
