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

import com.eblan.launcher.data.room.dao.EblanShortcutInfoDao
import com.eblan.launcher.data.room.entity.EblanShortcutInfoEntity
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultEblanShortcutInfoRepository @Inject constructor(private val eblanShortcutInfoDao: EblanShortcutInfoDao) :
    EblanShortcutInfoRepository {
    override val eblanShortcutInfos =
        eblanShortcutInfoDao.getEblanShortcutInfoEntities().map { entities ->
            entities.map { entity ->
                entity.asModel()
            }
        }

    override suspend fun upsertEblanShortcutInfos(eblanShortcutInfos: List<EblanShortcutInfo>) {
        val entities = eblanShortcutInfos.map { eblanShortcutInfo ->
            eblanShortcutInfo.asEntity()
        }

        eblanShortcutInfoDao.upsertEblanShortcutInfoEntities(entities = entities)
    }

    override suspend fun upsertEblanShortcutInfo(eblanShortcutInfo: EblanShortcutInfo) {
        eblanShortcutInfoDao.upsertEblanShortcutInfoEntity(entity = eblanShortcutInfo.asEntity())
    }

    override suspend fun deleteEblanShortcutInfos(eblanShortcutInfos: List<EblanShortcutInfo>) {
        val entities = eblanShortcutInfos.map { eblanShortcutInfo ->
            eblanShortcutInfo.asEntity()
        }

        eblanShortcutInfoDao.deleteEblanShortcutInfoEntities(entities = entities)
    }

    override suspend fun getEblanShortcutInfo(id: String): EblanShortcutInfo? {
        return eblanShortcutInfoDao.getEblanShortcutInfoEntity(id = id)?.asModel()
    }

    override suspend fun getEblanShortcutInfoByPackageName(packageName: String): List<EblanShortcutInfo> {
        return eblanShortcutInfoDao.getEblanShortcutInfoEntitiesByPackageName(packageName = packageName)
            .map { entity ->
                entity.asModel()
            }
    }

    override suspend fun deleteEblanShortcutInfoByPackageName(packageName: String) {
        eblanShortcutInfoDao.deleteEblanShortcutInfoEntityByPackageName(packageName = packageName)
    }

    private fun EblanShortcutInfo.asEntity(): EblanShortcutInfoEntity {
        return EblanShortcutInfoEntity(
            shortcutId = shortcutId,
            serialNumber = serialNumber,
            packageName = packageName,
            shortLabel = shortLabel,
            longLabel = longLabel,
            icon = icon,
            shortcutQueryFlag = shortcutQueryFlag,
            isEnabled = isEnabled,
        )
    }

    private fun EblanShortcutInfoEntity.asModel(): EblanShortcutInfo {
        return EblanShortcutInfo(
            shortcutId = shortcutId,
            serialNumber = serialNumber,
            packageName = packageName,
            shortLabel = shortLabel,
            longLabel = longLabel,
            icon = icon,
            shortcutQueryFlag = shortcutQueryFlag,
            isEnabled = isEnabled,
        )
    }
}
