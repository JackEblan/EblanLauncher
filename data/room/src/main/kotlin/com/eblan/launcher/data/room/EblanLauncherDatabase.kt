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
package com.eblan.launcher.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eblan.launcher.data.room.converter.EblanLauncherTypeConverters
import com.eblan.launcher.data.room.dao.ApplicationInfoDao
import com.eblan.launcher.data.room.dao.GridDao
import com.eblan.launcher.data.room.entity.EblanLauncherApplicationInfoEntity
import com.eblan.launcher.data.room.entity.GridItemEntity

@Database(
    entities = [GridItemEntity::class, EblanLauncherApplicationInfoEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(EblanLauncherTypeConverters::class)
internal abstract class EblanLauncherDatabase : RoomDatabase() {
    abstract fun gridDao(): GridDao

    abstract fun applicationInfoDao(): ApplicationInfoDao

    companion object {
        const val DATABASE_NAME = "EblanLauncher.db"
    }
}
