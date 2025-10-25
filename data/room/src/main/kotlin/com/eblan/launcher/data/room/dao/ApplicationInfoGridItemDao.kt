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
import androidx.room.Update
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.ApplicationInfoGridItemEntity
import com.eblan.launcher.domain.model.UpdateApplicationInfoGridItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationInfoGridItemDao {
    @Query("SELECT * FROM ApplicationInfoGridItemEntity")
    fun getApplicationInfoGridItemEntities(): Flow<List<ApplicationInfoGridItemEntity>>

    @Upsert
    suspend fun upsertApplicationInfoGridItemEntities(entities: List<ApplicationInfoGridItemEntity>)

    @Update
    suspend fun updateApplicationInfoGridItemEntity(entity: ApplicationInfoGridItemEntity)

    @Delete
    suspend fun deleteApplicationInfoGridItemEntities(entities: List<ApplicationInfoGridItemEntity>)

    @Delete
    suspend fun deleteApplicationInfoGridItemEntity(entity: ApplicationInfoGridItemEntity)

    @Query("SELECT * FROM ApplicationInfoGridItemEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun getApplicationInfoGridItemEntities(
        serialNumber: Long,
        packageName: String,
    ): List<ApplicationInfoGridItemEntity>

    @Query("DELETE FROM ApplicationInfoGridItemEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun deleteApplicationInfoGridItemEntity(
        serialNumber: Long,
        packageName: String,
    )

    @Update(entity = ApplicationInfoGridItemEntity::class)
    suspend fun updateApplicationInfoGridItemEntities(updateApplicationInfoGridItems: List<UpdateApplicationInfoGridItem>)
}
