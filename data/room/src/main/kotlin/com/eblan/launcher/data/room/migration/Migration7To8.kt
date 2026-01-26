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

class Migration7To8 : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add action columns to ApplicationInfoGridItemEntity
        db.execSQL(
            """
            ALTER TABLE ApplicationInfoGridItemEntity 
            ADD COLUMN doubleTap_eblanActionType TEXT NOT NULL DEFAULT 'None'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ApplicationInfoGridItemEntity 
            ADD COLUMN doubleTap_componentName TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ApplicationInfoGridItemEntity 
            ADD COLUMN swipeUp_eblanActionType TEXT NOT NULL DEFAULT 'None'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ApplicationInfoGridItemEntity 
            ADD COLUMN swipeUp_componentName TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ApplicationInfoGridItemEntity 
            ADD COLUMN swipeDown_eblanActionType TEXT NOT NULL DEFAULT 'None'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ApplicationInfoGridItemEntity 
            ADD COLUMN swipeDown_componentName TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )

        // Add action columns to ShortcutInfoGridItemEntity
        db.execSQL(
            """
            ALTER TABLE ShortcutInfoGridItemEntity 
            ADD COLUMN doubleTap_eblanActionType TEXT NOT NULL DEFAULT 'None'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ShortcutInfoGridItemEntity 
            ADD COLUMN doubleTap_componentName TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ShortcutInfoGridItemEntity 
            ADD COLUMN swipeUp_eblanActionType TEXT NOT NULL DEFAULT 'None'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ShortcutInfoGridItemEntity 
            ADD COLUMN swipeUp_componentName TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ShortcutInfoGridItemEntity 
            ADD COLUMN swipeDown_eblanActionType TEXT NOT NULL DEFAULT 'None'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ShortcutInfoGridItemEntity 
            ADD COLUMN swipeDown_componentName TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )

        // Add action columns to FolderGridItemEntity
        db.execSQL(
            """
            ALTER TABLE FolderGridItemEntity 
            ADD COLUMN doubleTap_eblanActionType TEXT NOT NULL DEFAULT 'None'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE FolderGridItemEntity 
            ADD COLUMN doubleTap_componentName TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE FolderGridItemEntity 
            ADD COLUMN swipeUp_eblanActionType TEXT NOT NULL DEFAULT 'None'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE FolderGridItemEntity 
            ADD COLUMN swipeUp_componentName TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE FolderGridItemEntity 
            ADD COLUMN swipeDown_eblanActionType TEXT NOT NULL DEFAULT 'None'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE FolderGridItemEntity 
            ADD COLUMN swipeDown_componentName TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )

        // Add action columns to ShortcutConfigGridItemEntity
        db.execSQL(
            """
            ALTER TABLE ShortcutConfigGridItemEntity 
            ADD COLUMN doubleTap_eblanActionType TEXT NOT NULL DEFAULT 'None'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ShortcutConfigGridItemEntity 
            ADD COLUMN doubleTap_componentName TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ShortcutConfigGridItemEntity 
            ADD COLUMN swipeUp_eblanActionType TEXT NOT NULL DEFAULT 'None'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ShortcutConfigGridItemEntity 
            ADD COLUMN swipeUp_componentName TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ShortcutConfigGridItemEntity 
            ADD COLUMN swipeDown_eblanActionType TEXT NOT NULL DEFAULT 'None'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE ShortcutConfigGridItemEntity 
            ADD COLUMN swipeDown_componentName TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
    }
}
