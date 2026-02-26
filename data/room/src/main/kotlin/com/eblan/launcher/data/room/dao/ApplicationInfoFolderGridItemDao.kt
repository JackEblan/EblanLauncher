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
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.ApplicationInfoFolderGridItemEntity
import com.eblan.launcher.domain.model.UpdateApplicationInfoFolderGridItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationInfoFolderGridItemDao {
    @Query("SELECT * FROM ApplicationInfoFolderGridItemEntity")
    fun getApplicationInfoFolderGridItemEntities(): Flow<List<ApplicationInfoFolderGridItemEntity>>

    @Upsert
    suspend fun upsertApplicationInfoFolderGridItemEntities(entities: List<ApplicationInfoFolderGridItemEntity>)

    @Update
    suspend fun updateApplicationInfoFolderGridItemEntity(entity: ApplicationInfoFolderGridItemEntity)

    @Delete
    suspend fun deleteApplicationInfoFolderGridItemEntities(entities: List<ApplicationInfoFolderGridItemEntity>)

    @Delete
    suspend fun deleteApplicationInfoFolderGridItemEntity(entity: ApplicationInfoFolderGridItemEntity)

    @Query("SELECT * FROM ApplicationInfoFolderGridItemEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun getApplicationInfoFolderGridItemEntitiesByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<ApplicationInfoFolderGridItemEntity>

    @Query("DELETE FROM ApplicationInfoFolderGridItemEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun deleteApplicationInfoFolderGridItemEntity(
        serialNumber: Long,
        packageName: String,
    )

    @Update(entity = ApplicationInfoFolderGridItemEntity::class)
    suspend fun updateApplicationInfoFolderGridItemEntities(updateApplicationInfoFolderGridItems: List<UpdateApplicationInfoFolderGridItem>)

    @Insert
    suspend fun insertApplicationInfoFolderGridItemEntity(entity: ApplicationInfoFolderGridItemEntity)
}
