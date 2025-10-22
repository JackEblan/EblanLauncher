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
import com.eblan.launcher.data.room.dao.ApplicationInfoGridItemDao
import com.eblan.launcher.data.room.dao.EblanAppWidgetProviderInfoDao
import com.eblan.launcher.data.room.dao.EblanApplicationInfoDao
import com.eblan.launcher.data.room.dao.EblanIconPackInfoDao
import com.eblan.launcher.data.room.dao.EblanShortcutInfoDao
import com.eblan.launcher.data.room.dao.FolderGridItemDao
import com.eblan.launcher.data.room.dao.ShortcutInfoGridItemDao
import com.eblan.launcher.data.room.dao.WidgetGridItemDao
import com.eblan.launcher.data.room.entity.ApplicationInfoGridItemEntity
import com.eblan.launcher.data.room.entity.EblanAppWidgetProviderInfoEntity
import com.eblan.launcher.data.room.entity.EblanApplicationInfoEntity
import com.eblan.launcher.data.room.entity.EblanIconPackInfoEntity
import com.eblan.launcher.data.room.entity.EblanShortcutInfoEntity
import com.eblan.launcher.data.room.entity.FolderGridItemEntity
import com.eblan.launcher.data.room.entity.ShortcutInfoGridItemEntity
import com.eblan.launcher.data.room.entity.WidgetGridItemEntity

@Database(
    entities = [
        EblanApplicationInfoEntity::class,
        EblanAppWidgetProviderInfoEntity::class,
        EblanShortcutInfoEntity::class,
        ApplicationInfoGridItemEntity::class,
        WidgetGridItemEntity::class,
        ShortcutInfoGridItemEntity::class,
        FolderGridItemEntity::class,
        EblanIconPackInfoEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
internal abstract class EblanDatabase : RoomDatabase() {
    abstract fun applicationInfoGridItemDao(): ApplicationInfoGridItemDao

    abstract fun widgetGridItemDao(): WidgetGridItemDao

    abstract fun shortcutInfoGridItemDao(): ShortcutInfoGridItemDao

    abstract fun eblanApplicationInfoDao(): EblanApplicationInfoDao

    abstract fun eblanAppWidgetProviderInfoDao(): EblanAppWidgetProviderInfoDao

    abstract fun eblanShortcutInfoDao(): EblanShortcutInfoDao

    abstract fun folderGridItemDao(): FolderGridItemDao

    abstract fun eblanIconPackInfoDao(): EblanIconPackInfoDao

    companion object {
        const val DATABASE_NAME = "Eblan.db"
    }
}
