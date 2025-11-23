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
import com.eblan.launcher.data.room.entity.EblanShortcutConfigActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EblanShortcutConfigActivityDao {
    @Query("SELECT * FROM EblanShortcutConfigActivityEntity")
    fun getEblanShortcutConfigActivityEntities(): Flow<List<EblanShortcutConfigActivityEntity>>

    @Upsert
    suspend fun upsertEblanShortcutConfigActivityEntities(entities: List<EblanShortcutConfigActivityEntity>)

    @Upsert
    suspend fun upsertEblanShortcutConfigActivityEntity(entity: EblanShortcutConfigActivityEntity)

    @Query("SELECT * FROM EblanShortcutConfigActivityEntity WHERE packageName = :packageName")
    suspend fun getEblanShortcutConfigActivityEntity(packageName: String): List<EblanShortcutConfigActivityEntity>

    @Query("DELETE FROM EblanShortcutConfigActivityEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun deleteEblanShortcutConfigActivityEntity(
        serialNumber: Long,
        packageName: String,
    )

    @Delete
    suspend fun deleteEblanShortcutConfigActivityEntities(entities: List<EblanShortcutConfigActivityEntity>)

    @Query("SELECT * FROM EblanShortcutConfigActivityEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun getEblanShortcutConfigActivityEntity(
        serialNumber: Long,
        packageName: String,
    ): List<EblanShortcutConfigActivityEntity>
}
