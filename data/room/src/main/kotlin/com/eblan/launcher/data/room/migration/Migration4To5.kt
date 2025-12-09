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

class Migration4To5 : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        updateEblanApplicationInfoEntities(db = db)

        updateEblanAppWidgetProviderInfoEntities(db = db)

        updateApplicationInfoGridItemEntities(db = db)

        updateWidgetGridItemEntities(db = db)

        updateShortcutInfoGridItemEntities(db = db)

        updateFolderGridItemEntities(db = db)

        updateShortcutConfigGridItemEntities(db = db)
    }

    private fun updateEblanApplicationInfoEntities(db: SupportSQLiteDatabase) {
        // Step 1: Create the new table with the exact version 5 schema
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `EblanApplicationInfoEntity_new` (
                componentName TEXT NOT NULL,
                serialNumber INTEGER NOT NULL,
                packageName TEXT NOT NULL,
                icon TEXT,
                label TEXT NOT NULL DEFAULT '',
                customIcon TEXT,
                customLabel TEXT,
                PRIMARY KEY(componentName, serialNumber)
            )
            """.trimIndent(),
        )

        // Step 2: Copy all data from old table to new table
        // We set label = COALESCE(label, '') to satisfy NOT NULL constraint
        db.execSQL(
            """
            INSERT INTO `EblanApplicationInfoEntity_new` (
                componentName, serialNumber, packageName,
                icon, label, customIcon, customLabel
            )
            SELECT 
                componentName, 
                serialNumber, 
                packageName,
                icon, 
                COALESCE(label, '') AS label,
                NULL AS customIcon,
                NULL AS customLabel
            FROM EblanApplicationInfoEntity
            """.trimIndent(),
        )

        // Step 3: Drop the old table
        db.execSQL("DROP TABLE `EblanApplicationInfoEntity`")

        // Step 4: Rename new table to final name
        db.execSQL("ALTER TABLE `EblanApplicationInfoEntity_new` RENAME TO `EblanApplicationInfoEntity`")
    }

    private fun updateEblanAppWidgetProviderInfoEntities(db: SupportSQLiteDatabase) {
// Step 1: Add the label column again as NOT NULL with default empty string
        // SQLite does not allow modifying a column directly to NOT NULL if it contains NULLs,
        // so we use the standard safe pattern: rename → create new → copy → drop old → rename back

        db.execSQL("ALTER TABLE `EblanAppWidgetProviderInfoEntity` RENAME TO `EblanAppWidgetProviderInfoEntity_old`")

        // Step 2: Create the new table matching exactly the version 5 schema
        db.execSQL(
            """
            CREATE TABLE `EblanAppWidgetProviderInfoEntity` (
                componentName TEXT NOT NULL,
                serialNumber INTEGER NOT NULL,
                configure TEXT,
                packageName TEXT NOT NULL,
                targetCellWidth INTEGER NOT NULL,
                targetCellHeight INTEGER NOT NULL,
                minWidth INTEGER NOT NULL,
                minHeight INTEGER NOT NULL,
                resizeMode INTEGER NOT NULL,
                minResizeWidth INTEGER NOT NULL,
                minResizeHeight INTEGER NOT NULL,
                maxResizeWidth INTEGER NOT NULL,
                maxResizeHeight INTEGER NOT NULL,
                preview TEXT,
                label TEXT NOT NULL DEFAULT '',
                icon TEXT,
                PRIMARY KEY(componentName)
            )
            """.trimIndent(),
        )

        // Step 3: Copy all data — NULL labels become empty string
        db.execSQL(
            """
            INSERT INTO `EblanAppWidgetProviderInfoEntity` (
                componentName, serialNumber, configure, packageName,
                targetCellWidth, targetCellHeight, minWidth, minHeight,
                resizeMode, minResizeWidth, minResizeHeight,
                maxResizeWidth, maxResizeHeight, preview, label, icon
            )
            SELECT 
                componentName, serialNumber, configure, packageName,
                targetCellWidth, targetCellHeight, minWidth, minHeight,
                resizeMode, minResizeWidth, minResizeHeight,
                maxResizeWidth, maxResizeHeight, preview,
                COALESCE(label, '') AS label,
                icon
            FROM `EblanAppWidgetProviderInfoEntity_old`
            """.trimIndent(),
        )

        // Step 4: Drop the old table
        db.execSQL("DROP TABLE `EblanAppWidgetProviderInfoEntity_old`")
    }

    private fun updateApplicationInfoGridItemEntities(db: SupportSQLiteDatabase) {
// Step 1: Create new table matching EXACTLY the version 5 schema
        db.execSQL(
            """
            CREATE TABLE `ApplicationInfoGridItemEntity_new` (
                id TEXT NOT NULL,
                folderId TEXT,
                page INTEGER NOT NULL,
                startColumn INTEGER NOT NULL,
                startRow INTEGER NOT NULL,
                columnSpan INTEGER NOT NULL,
                rowSpan INTEGER NOT NULL,
                associate TEXT NOT NULL,
                componentName TEXT NOT NULL,
                packageName TEXT NOT NULL,
                icon TEXT,
                label TEXT NOT NULL DEFAULT '',
                override INTEGER NOT NULL,
                serialNumber INTEGER NOT NULL,
                customIcon TEXT,
                customLabel TEXT,
                iconSize INTEGER NOT NULL,
                textColor TEXT NOT NULL,
                textSize INTEGER NOT NULL,
                showLabel INTEGER NOT NULL,
                singleLineLabel INTEGER NOT NULL,
                horizontalAlignment TEXT NOT NULL,
                verticalArrangement TEXT NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )

        // Step 2: Copy all data — NULL labels become empty string, new columns = NULL
        db.execSQL(
            """
            INSERT INTO `ApplicationInfoGridItemEntity_new` (
                id, folderId, page, startColumn, startRow, columnSpan, rowSpan,
                associate, componentName, packageName, icon,
                label, override, serialNumber,
                customIcon, customLabel,
                iconSize, textColor, textSize, showLabel, singleLineLabel,
                horizontalAlignment, verticalArrangement
            )
            SELECT 
                id, folderId, page, startColumn, startRow, columnSpan, rowSpan,
                associate, componentName, packageName, icon,
                COALESCE(label, '') AS label,
                override, serialNumber,
                NULL AS customIcon,
                NULL AS customLabel,
                iconSize, textColor, textSize, showLabel, singleLineLabel,
                horizontalAlignment, verticalArrangement
            FROM `ApplicationInfoGridItemEntity`
            """.trimIndent(),
        )

        // Step 3: Drop old table and rename new one
        db.execSQL("DROP TABLE `ApplicationInfoGridItemEntity`")
        db.execSQL("ALTER TABLE `ApplicationInfoGridItemEntity_new` RENAME TO `ApplicationInfoGridItemEntity`")
    }

    private fun updateWidgetGridItemEntities(db: SupportSQLiteDatabase) {
// Step 1: Create the new table with the exact version 5 schema
        db.execSQL(
            """
            CREATE TABLE `WidgetGridItemEntity_new` (
                id TEXT NOT NULL,
                folderId TEXT,
                page INTEGER NOT NULL,
                startColumn INTEGER NOT NULL,
                startRow INTEGER NOT NULL,
                columnSpan INTEGER NOT NULL,
                rowSpan INTEGER NOT NULL,
                associate TEXT NOT NULL,
                appWidgetId INTEGER NOT NULL,
                packageName TEXT NOT NULL,
                componentName TEXT NOT NULL,
                configure TEXT,
                minWidth INTEGER NOT NULL,
                minHeight INTEGER NOT NULL,
                resizeMode INTEGER NOT NULL,
                minResizeWidth INTEGER NOT NULL,
                minResizeHeight INTEGER NOT NULL,
                maxResizeWidth INTEGER NOT NULL,
                maxResizeHeight INTEGER NOT NULL,
                targetCellHeight INTEGER NOT NULL,
                targetCellWidth INTEGER NOT NULL,
                preview TEXT,
                label TEXT NOT NULL DEFAULT '',
                icon TEXT,
                override INTEGER NOT NULL,
                serialNumber INTEGER NOT NULL,
                iconSize INTEGER NOT NULL,
                textColor TEXT NOT NULL,
                textSize INTEGER NOT NULL,
                showLabel INTEGER NOT NULL,
                singleLineLabel INTEGER NOT NULL,
                horizontalAlignment TEXT NOT NULL,
                verticalArrangement TEXT NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )

        // Step 2: Copy all existing data – convert any NULL label → empty string
        db.execSQL(
            """
            INSERT INTO `WidgetGridItemEntity_new`
            SELECT 
                id,
                folderId,
                page,
                startColumn,
                startRow,
                columnSpan,
                rowSpan,
                associate,
                appWidgetId,
                packageName,
                componentName,
                configure,
                minWidth,
                minHeight,
                resizeMode,
                minResizeWidth,
                minResizeHeight,
                maxResizeWidth,
                maxResizeHeight,
                targetCellHeight,
                targetCellWidth,
                preview,
                COALESCE(label, '') AS label,
                icon,
                override,
                serialNumber,
                iconSize,
                textColor,
                textSize,
                showLabel,
                singleLineLabel,
                horizontalAlignment,
                verticalArrangement
            FROM `WidgetGridItemEntity`
            """.trimIndent(),
        )

        // Step 3: Replace old table
        db.execSQL("DROP TABLE `WidgetGridItemEntity`")
        db.execSQL("ALTER TABLE `WidgetGridItemEntity_new` RENAME TO `WidgetGridItemEntity`")
    }

    private fun updateShortcutInfoGridItemEntities(db: SupportSQLiteDatabase) {
        // Add the two new columns — both nullable, so safe to add directly
        db.execSQL("ALTER TABLE `ShortcutInfoGridItemEntity` ADD COLUMN `customIcon` TEXT")
        db.execSQL("ALTER TABLE `ShortcutInfoGridItemEntity` ADD COLUMN `customShortLabel` TEXT")
    }

    private fun updateFolderGridItemEntities(db: SupportSQLiteDatabase) {
        // 1. Create new table with exact version 5 schema
        db.execSQL(
            """
            CREATE TABLE `FolderGridItemEntity_new` (
                id TEXT NOT NULL,
                folderId TEXT,
                page INTEGER NOT NULL,
                startColumn INTEGER NOT NULL,
                startRow INTEGER NOT NULL,
                columnSpan INTEGER NOT NULL,
                rowSpan INTEGER NOT NULL,
                associate TEXT NOT NULL,
                label TEXT NOT NULL,
                override INTEGER NOT NULL,
                pageCount INTEGER NOT NULL,
                icon TEXT,
                iconSize INTEGER NOT NULL,
                textColor TEXT NOT NULL,
                textSize INTEGER NOT NULL,
                showLabel INTEGER NOT NULL,
                singleLineLabel INTEGER NOT NULL,
                horizontalAlignment TEXT NOT NULL,
                verticalArrangement TEXT NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )

        // 2. Copy all data — new `icon` column will be NULL
        db.execSQL(
            """
            INSERT INTO `FolderGridItemEntity_new` (
                id, folderId, page, startColumn, startRow, columnSpan, rowSpan,
                associate, label, override, pageCount,
                icon,
                iconSize, textColor, textSize, showLabel, singleLineLabel,
                horizontalAlignment, verticalArrangement
            )
            SELECT 
                id, folderId, page, startColumn, startRow, columnSpan, rowSpan,
                associate, label, override, pageCount,
                NULL AS icon,
                iconSize, textColor, textSize, showLabel, singleLineLabel,
                horizontalAlignment, verticalArrangement
            FROM `FolderGridItemEntity`
            """.trimIndent(),
        )

        // 3. Replace old table
        db.execSQL("DROP TABLE `FolderGridItemEntity`")
        db.execSQL("ALTER TABLE `FolderGridItemEntity_new` RENAME TO `FolderGridItemEntity`")
    }

    private fun updateShortcutConfigGridItemEntities(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `ShortcutConfigGridItemEntity` ADD COLUMN `customIcon` TEXT")
        db.execSQL("ALTER TABLE `ShortcutConfigGridItemEntity` ADD COLUMN `customLabel` TEXT")
    }
}
