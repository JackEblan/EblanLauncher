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
package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.EblanApplicationInfoEntity
import com.eblan.launcher.data.room.entity.EblanApplicationInfoEntityWithTags
import com.eblan.launcher.domain.model.SyncEblanApplicationInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface EblanApplicationInfoDao {
    @Query("SELECT * FROM EblanApplicationInfoEntity")
    fun getEblanApplicationInfoEntities(): Flow<List<EblanApplicationInfoEntity>>

    @Query("SELECT * FROM EblanApplicationInfoEntity")
    fun getEblanApplicationInfoEntityList(): List<EblanApplicationInfoEntity>

    @Upsert
    suspend fun upsertEblanApplicationInfoEntities(entities: List<EblanApplicationInfoEntity>)

    @Upsert
    suspend fun upsertEblanApplicationInfoEntity(entity: EblanApplicationInfoEntity)

    @Query("DELETE FROM EblanApplicationInfoEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun deleteEblanApplicationInfoEntityByPackageName(
        serialNumber: Long,
        packageName: String,
    )

    @Delete
    suspend fun deleteEblanApplicationInfoEntities(entities: List<EblanApplicationInfoEntity>)

    @Upsert(entity = EblanApplicationInfoEntity::class)
    suspend fun upsertSyncEblanApplicationInfoEntities(syncEblanApplicationInfos: List<SyncEblanApplicationInfo>)

    @Delete(entity = EblanApplicationInfoEntity::class)
    suspend fun deleteSyncEblanApplicationInfoEntities(syncEblanApplicationInfos: List<SyncEblanApplicationInfo>)

    @Update
    suspend fun updateEblanApplicationInfoEntity(entity: EblanApplicationInfoEntity)

    @Query("SELECT * FROM EblanApplicationInfoEntity WHERE serialNumber = :serialNumber AND componentName = :componentName")
    suspend fun getEblanApplicationInfoEntityByComponentName(
        serialNumber: Long,
        componentName: String,
    ): EblanApplicationInfoEntity?

    @Query("SELECT * FROM EblanApplicationInfoEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun getEblanApplicationInfoEntitiesByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<EblanApplicationInfoEntity>

    @Query(
        """
        SELECT app.*
        FROM EblanApplicationInfoEntity AS app
        INNER JOIN EblanApplicationInfoTagCrossRefEntity AS ref
            ON app.componentName = ref.componentName
           AND app.serialNumber = ref.serialNumber
        WHERE ref.id = :tagId
    """,
    )
    suspend fun getEblanApplicationInfoEntitiesByTagId(tagId: Long): List<EblanApplicationInfoEntity>

    @Transaction
    @Query(
        """
        SELECT * 
        FROM EblanApplicationInfoEntity
        WHERE componentName = :componentName AND serialNumber = :serialNumber
    """,
    )
    suspend fun getEblanApplicationInfoEntityTags(
        componentName: String,
        serialNumber: Long,
    ): EblanApplicationInfoEntityWithTags
}