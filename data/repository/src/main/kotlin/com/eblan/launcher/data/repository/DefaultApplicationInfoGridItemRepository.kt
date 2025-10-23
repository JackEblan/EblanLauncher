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
import com.eblan.launcher.data.room.dao.ApplicationInfoGridItemDao
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultApplicationInfoGridItemRepository @Inject constructor(private val applicationInfoGridItemDao: ApplicationInfoGridItemDao) :
    ApplicationInfoGridItemRepository {
    override val gridItems =
        applicationInfoGridItemDao.getApplicationInfoGridItemEntities().map { entities ->
            entities.map { entity ->
                entity.asGridItem()
            }
        }

    override val applicationInfoGridItems: Flow<List<ApplicationInfoGridItem>> =
        applicationInfoGridItemDao.getApplicationInfoGridItemEntities().map { entities ->
            entities.map { entity ->
                entity.asModel()
            }
        }

    override suspend fun upsertApplicationInfoGridItems(applicationInfoGridItems: List<ApplicationInfoGridItem>) {
        val entities = applicationInfoGridItems.map { applicationInfoGridItem ->
            applicationInfoGridItem.asEntity()
        }

        applicationInfoGridItemDao.upsertApplicationInfoGridItemEntities(entities = entities)
    }

    override suspend fun updateApplicationInfoGridItem(applicationInfoGridItem: ApplicationInfoGridItem) {
        applicationInfoGridItemDao.updateApplicationInfoGridItemEntity(
            applicationInfoGridItem.asEntity(),
        )
    }

    override suspend fun deleteApplicationInfoGridItems(applicationInfoGridItems: List<ApplicationInfoGridItem>) {
        val entities = applicationInfoGridItems.map { applicationInfoGridItem ->
            applicationInfoGridItem.asEntity()
        }

        applicationInfoGridItemDao.deleteApplicationInfoGridItemEntities(entities = entities)
    }

    override suspend fun deleteApplicationInfoGridItem(applicationInfoGridItem: ApplicationInfoGridItem) {
        applicationInfoGridItemDao.deleteApplicationInfoGridItemEntity(entity = applicationInfoGridItem.asEntity())
    }

    override suspend fun getApplicationInfoGridItems(
        serialNumber: Long,
        packageName: String,
    ): List<ApplicationInfoGridItem> {
        return applicationInfoGridItemDao.getApplicationInfoGridItemEntities(
            serialNumber = serialNumber,
            packageName = packageName,
        ).map { entity ->
            entity.asModel()
        }
    }

    override suspend fun updateApplicationInfoGridItems(applicationInfoGridItems: List<ApplicationInfoGridItem>) {
        val entities = applicationInfoGridItems.map { applicationInfoGridItem ->
            applicationInfoGridItem.asEntity()
        }

        applicationInfoGridItemDao.updateApplicationInfoGridItemEntities(entities = entities)
    }
}
