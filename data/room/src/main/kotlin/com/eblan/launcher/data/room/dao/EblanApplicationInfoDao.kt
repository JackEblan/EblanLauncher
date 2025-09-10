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
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.EblanApplicationInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EblanApplicationInfoDao {
    @Query("SELECT * FROM EblanApplicationInfoEntity")
    fun getEblanApplicationInfoEntities(): Flow<List<EblanApplicationInfoEntity>>

    @Upsert
    suspend fun upsertEblanApplicationInfoEntities(entities: List<EblanApplicationInfoEntity>)

    @Upsert
    suspend fun upsertEblanApplicationInfoEntity(entity: EblanApplicationInfoEntity)

    @Query("SELECT * FROM EblanApplicationInfoEntity WHERE packageName = :packageName")
    suspend fun getEblanApplicationInfoEntity(packageName: String): EblanApplicationInfoEntity?

    @Query("DELETE FROM EblanApplicationInfoEntity WHERE packageName = :packageName")
    suspend fun deleteEblanApplicationInfoEntityByPackageName(packageName: String)

    @Delete
    suspend fun deleteEblanApplicationInfoEntities(entities: List<EblanApplicationInfoEntity>)
}
