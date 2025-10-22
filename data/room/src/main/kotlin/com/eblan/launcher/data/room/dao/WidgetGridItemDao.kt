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
import com.eblan.launcher.data.room.entity.WidgetGridItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetGridItemDao {
    @Query("SELECT * FROM WidgetGridItemEntity")
    fun getWidgetGridItemEntities(): Flow<List<WidgetGridItemEntity>>

    @Upsert
    suspend fun upsertWidgetGridItemEntities(entities: List<WidgetGridItemEntity>)

    @Upsert
    suspend fun upsertWidgetGridItemEntity(entity: WidgetGridItemEntity): Long

    @Update
    suspend fun updateWidgetGridItemEntity(entity: WidgetGridItemEntity)

    @Query("SELECT * FROM WidgetGridItemEntity WHERE id = :id")
    suspend fun getWidgetGridItemEntity(id: String): WidgetGridItemEntity?

    @Delete
    suspend fun deleteWidgetGridItemEntities(entities: List<WidgetGridItemEntity>)

    @Delete
    suspend fun deleteWidgetGridItemEntity(entity: WidgetGridItemEntity)

    @Query("SELECT * FROM WidgetGridItemEntity WHERE packageName = :packageName")
    suspend fun getWidgetGridItemEntities(packageName: String): List<WidgetGridItemEntity>
}
