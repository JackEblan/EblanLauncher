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

import com.eblan.launcher.data.room.dao.EblanShortcutConfigActivityDao
import com.eblan.launcher.data.room.entity.EblanShortcutConfigActivityEntity
import com.eblan.launcher.domain.model.EblanShortcutConfigActivity
import com.eblan.launcher.domain.repository.EblanShortcutConfigActivityRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultEblanShortcutConfigActivityRepository @Inject constructor(private val eblanShortcutConfigActivityDao: EblanShortcutConfigActivityDao) :
    EblanShortcutConfigActivityRepository {
    override val eblanShortcutConfigActivities =
        eblanShortcutConfigActivityDao.getEblanShortcutConfigActivityEntities().map { entities ->
            entities.map { entity ->
                entity.asModel()
            }
        }

    override suspend fun upsertEblanShortcutConfigActivities(eblanShortcutConfigActivities: List<EblanShortcutConfigActivity>) {
        val entities = eblanShortcutConfigActivities.map { eblanShortcutConfigActivity ->
            eblanShortcutConfigActivity.asEntity()
        }

        eblanShortcutConfigActivityDao.upsertEblanShortcutConfigActivityEntities(entities = entities)
    }

    override suspend fun upsertEblanShortcutConfigActivity(eblanShortcutConfigActivity: EblanShortcutConfigActivity) {
        eblanShortcutConfigActivityDao.upsertEblanShortcutConfigActivityEntity(entity = eblanShortcutConfigActivity.asEntity())
    }

    override suspend fun deleteEblanShortcutConfigActivity(
        serialNumber: Long,
        packageName: String,
    ) {
        eblanShortcutConfigActivityDao.deleteEblanShortcutConfigActivityEntity(
            serialNumber = serialNumber,
            packageName = packageName,
        )
    }

    override suspend fun deleteEblanShortcutConfigActivities(eblanShortcutConfigActivities: List<EblanShortcutConfigActivity>) {
        val entities = eblanShortcutConfigActivities.map { eblanShortcutConfigActivity ->
            eblanShortcutConfigActivity.asEntity()
        }

        eblanShortcutConfigActivityDao.deleteEblanShortcutConfigActivityEntities(entities = entities)
    }

    override suspend fun getEblanShortcutConfigActivity(
        serialNumber: Long,
        packageName: String,
    ): List<EblanShortcutConfigActivity> {
        return eblanShortcutConfigActivityDao.getEblanShortcutConfigActivityEntity(
            serialNumber = serialNumber,
            packageName = packageName,
        ).map { entity ->
            entity.asModel()
        }
    }

    private fun EblanShortcutConfigActivity.asEntity(): EblanShortcutConfigActivityEntity {
        return EblanShortcutConfigActivityEntity(
            componentName = componentName,
            serialNumber = serialNumber,
            packageName = packageName,
            icon = icon,
            label = label,
        )
    }

    private fun EblanShortcutConfigActivityEntity.asModel(): EblanShortcutConfigActivity {
        return EblanShortcutConfigActivity(
            componentName = componentName,
            packageName = packageName,
            serialNumber = serialNumber,
            icon = icon,
            label = label,
        )
    }
}
