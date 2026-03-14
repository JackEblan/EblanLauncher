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
package com.eblan.launcher.data.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration12To13 : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create the new table with the 3-column Primary Key
        db.execSQL(
            """
            CREATE TABLE EblanShortcutInfoEntity_new (
                shortcutId TEXT NOT NULL,
                serialNumber INTEGER NOT NULL,
                packageName TEXT NOT NULL,
                shortLabel TEXT NOT NULL,
                longLabel TEXT NOT NULL,
                icon TEXT,
                shortcutQueryFlag TEXT NOT NULL,
                isEnabled INTEGER NOT NULL,
                lastUpdateTime INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(shortcutId, serialNumber, packageName)
            )
        """.trimIndent(),
        )

        // 2. Copy the data from the old table
        // Note: Make sure the column names match your old schema exactly
        db.execSQL(
            """
            INSERT INTO EblanShortcutInfoEntity_new (
                shortcutId, serialNumber, packageName, shortLabel, 
                longLabel, icon, shortcutQueryFlag, isEnabled, lastUpdateTime
            )
            SELECT shortcutId, serialNumber, packageName, shortLabel, 
                   longLabel, icon, shortcutQueryFlag, isEnabled, lastUpdateTime 
            FROM EblanShortcutInfoEntity
        """.trimIndent(),
        )

        // 3. Drop the old table
        db.execSQL("DROP TABLE EblanShortcutInfoEntity")

        // 4. Rename the new table to the original name
        db.execSQL("ALTER TABLE EblanShortcutInfoEntity_new RENAME TO EblanShortcutInfoEntity")
    }
}
