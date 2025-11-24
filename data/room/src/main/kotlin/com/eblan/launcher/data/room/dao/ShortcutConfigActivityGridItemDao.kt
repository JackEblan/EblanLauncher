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
import com.eblan.launcher.data.room.entity.ShortcutConfigActivityGridItemEntity
import com.eblan.launcher.domain.model.UpdateApplicationInfoGridItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutConfigActivityGridItemDao {
    @Query("SELECT * FROM ShortcutConfigActivityGridItemEntity")
    fun getShortcutConfigActivityGridItemEntities(): Flow<List<ShortcutConfigActivityGridItemEntity>>

    @Upsert
    suspend fun upsertShortcutConfigActivityGridItemEntities(entities: List<ShortcutConfigActivityGridItemEntity>)

    @Update
    suspend fun updateShortcutConfigActivityGridItemEntity(entity: ShortcutConfigActivityGridItemEntity)

    @Delete
    suspend fun deleteShortcutConfigActivityGridItemEntities(entities: List<ShortcutConfigActivityGridItemEntity>)

    @Delete
    suspend fun deleteShortcutConfigActivityGridItemEntity(entity: ShortcutConfigActivityGridItemEntity)

    @Query("SELECT * FROM ShortcutConfigActivityGridItemEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun getShortcutConfigActivityGridItemEntities(
        serialNumber: Long,
        packageName: String,
    ): List<ShortcutConfigActivityGridItemEntity>

    @Query("DELETE FROM ShortcutConfigActivityGridItemEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun deleteShortcutConfigActivityGridItemEntity(
        serialNumber: Long,
        packageName: String,
    )

    @Update(entity = ShortcutConfigActivityGridItemEntity::class)
    suspend fun updateShortcutConfigActivityGridItemEntities(
        updateApplicationInfoGridItems: List<UpdateApplicationInfoGridItem>,
    )
}
