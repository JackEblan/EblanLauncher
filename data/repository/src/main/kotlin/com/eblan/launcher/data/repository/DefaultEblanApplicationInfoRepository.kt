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
import com.eblan.launcher.data.repository.mapper.asModel
import com.eblan.launcher.data.room.dao.EblanApplicationInfoDao
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultEblanApplicationInfoRepository @Inject constructor(private val eblanApplicationInfoDao: EblanApplicationInfoDao) :
    EblanApplicationInfoRepository {
    override val eblanApplicationInfos =
        eblanApplicationInfoDao.getEblanApplicationInfoEntities().map { entities ->
            entities.map { entity ->
                entity.asModel()
            }
        }

    override suspend fun upsertEblanApplicationInfos(eblanApplicationInfos: List<EblanApplicationInfo>) {
        val entities = eblanApplicationInfos.map { eblanApplicationInfo ->
            eblanApplicationInfo.asEntity()
        }

        eblanApplicationInfoDao.upsertEblanApplicationInfoEntities(entities = entities)
    }

    override suspend fun upsertEblanApplicationInfo(eblanApplicationInfo: EblanApplicationInfo) {
        eblanApplicationInfoDao.upsertEblanApplicationInfoEntity(entity = eblanApplicationInfo.asEntity())
    }

    override suspend fun getEblanApplicationInfo(packageName: String): EblanApplicationInfo? {
        return eblanApplicationInfoDao.getEblanApplicationInfoEntity(packageName = packageName)
            ?.asModel()
    }

    override suspend fun deleteEblanApplicationInfo(
        serialNumber: Long,
        packageName: String,
    ) {
        eblanApplicationInfoDao.deleteEblanApplicationInfoEntity(
            serialNumber = serialNumber,
            packageName = packageName,
        )
    }

    override suspend fun deleteEblanApplicationInfos(eblanApplicationInfos: List<EblanApplicationInfo>) {
        val entities = eblanApplicationInfos.map { eblanApplicationInfo ->
            eblanApplicationInfo.asEntity()
        }

        eblanApplicationInfoDao.deleteEblanApplicationInfoEntities(entities = entities)
    }
}
