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

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class Migration4To5Test {
    private val testDatabase = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EblanDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        // Create database at version 4
        helper.createDatabase(
            testDatabase,
            4,
        ).apply {
            // EblanApplicationInfoEntity
            execSQL(
                """
                INSERT INTO `EblanApplicationInfoEntity`
                (packageName, serialNumber, componentName, icon, label) 
                VALUES ('com.example.app', 1, 'com.example.app/.MainActivity', '/path/icon.png', 'Original App'),
                ('com.test.app2',   2, 'com.test.app2/.Settings',     NULL,           NULL)
                """.trimIndent(),
            )

            // EblanAppWidgetProviderInfoEntity
            execSQL(
                """
                INSERT INTO `EblanAppWidgetProviderInfoEntity` (
                    componentName, serialNumber, packageName,
                    targetCellWidth, targetCellHeight, minWidth, minHeight,
                    resizeMode, minResizeWidth, minResizeHeight,
                    maxResizeWidth, maxResizeHeight, label
                ) VALUES 
                ('com.example.clock', 100, 'com.example.app', 4, 2, 4, 2, 1, 2, 2, 8, 8, 'Clock'),
                ('com.example.weather', 101, 'com.example.app', 4, 4, 4, 4, 1, 2, 2, 8, 8, NULL),
                ('com.example.notes', 102, 'com.example.app', 2, 2, 2, 2, 0, 2, 2, 4, 4, 'My Notes')
                """.trimIndent(),
            )

            // ApplicationInfoGridItemEntity
            execSQL(
                """
                INSERT INTO `ApplicationInfoGridItemEntity` (id, page, startColumn, startRow, columnSpan, rowSpan,
                    associate, componentName, packageName, override, serialNumber,
                    iconSize, textColor, textSize, showLabel, singleLineLabel,
                    horizontalAlignment, verticalArrangement, label)
                VALUES 
                ('item1', 0, 0, 0, 1, 1, 'none', 'com.app/.Main', 'com.app', 0, 100, 48, 
                 '#FFFFFF', 14, 1, 0, 'center', 'top', 'Browser'),
                ('item2', 0, 1, 1, 1, 1, 'none', 'com.app/.Notes', 'com.app', 0, 101, 48,
                 '#000000', 12, 0, 1, 'start', 'bottom', NULL)
                """.trimIndent(),
            )

            // WidgetGridItemEntity
            execSQL(
                """
                INSERT INTO `WidgetGridItemEntity` (
                id, folderId, page, startColumn, startRow, columnSpan, rowSpan,
                associate, appWidgetId, packageName, componentName, configure,
                minWidth, minHeight,
                resizeMode, minResizeWidth, minResizeHeight, maxResizeWidth, maxResizeHeight,
                targetCellWidth, targetCellHeight,
                preview, label, icon,
                override, serialNumber,
                iconSize, textColor, textSize, showLabel, singleLineLabel,
                horizontalAlignment, verticalArrangement
                ) VALUES 
                ('widget_1', NULL, 0, 0, 0, 4, 2,
                'none', 101, 'com.example', 'com.example.Clock', NULL,
                 200, 100,
                1, 2, 2, 8, 8,           
                4, 2,
                NULL, 'Clock', NULL,
                0, 500,
                48, '#FFFFFF', 14, 1, 0,
                'center', 'top'),
     
                ('widget_2', NULL, 1, 0, 0, 2, 2,
                'none', 102, 'com.weather', 'com.weather.Widget', NULL,
                100, 100,
                0, 1, 1, 8, 8,          
                2, 2,
                NULL, '', NULL,          -- label was NULL → will become '' in v5
                0, 501,
                32, '#000000', 12, 0, 1,
                'start', 'bottom')
                """.trimIndent(),
            )

            // FolderGridItemEntity
            execSQL(
                """
                INSERT INTO `FolderGridItemEntity` (
                    id, page, startColumn, startRow, columnSpan, rowSpan,
                    associate, label, override, pageCount,
                    iconSize, textColor, textSize, showLabel, singleLineLabel,
                    horizontalAlignment, verticalArrangement
                ) VALUES (
                    'folder_001', 0, 0, 0, 2, 2,
                    'folder', 'Work Apps', 0, 3,
                    64, '#FFFFFF', 16, 1, 0,
                    'center', 'top'
                )
                """.trimIndent(),
            )

            // ShortcutConfigGridItemEntity
            execSQL(
                """
                INSERT INTO `ShortcutConfigGridItemEntity` (
                    id, page, startColumn, startRow, columnSpan, rowSpan,
                    associate, componentName, packageName, activityLabel,
                    override, serialNumber, iconSize, textColor, textSize,
                    showLabel, singleLineLabel, horizontalAlignment, verticalArrangement
                ) VALUES (
                    'item_001', 0, 1, 2, 1, 1,
                    'app', 'com.browser/.Main', 'com.browser', 'Browser',
                    0, 300, 56, '#FFFFFF', 14, 1, 0, 'center', 'bottom'
                )
                """.trimIndent(),
            )

            close()
        }

        // Run migration and validate version 5
        val dbV5 = helper.runMigrationsAndValidate(
            testDatabase,
            5,
            true,
        )

        // EblanApplicationInfoEntity
        dbV5.query(
            """
            SELECT componentName, serialNumber, packageName, label, customIcon, customLabel
            FROM `EblanApplicationInfoEntity`
            ORDER BY serialNumber
            """.trimIndent(),
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())

            // Row 1
            assertEquals("com.example.app/.MainActivity", cursor.getString(0))
            assertEquals(1, cursor.getInt(1))
            assertEquals("com.example.app", cursor.getString(2))
            assertEquals("Original App", cursor.getString(3))
            assertNull(cursor.getString(4)) // customIcon
            assertNull(cursor.getString(5)) // customLabel

            assertTrue(cursor.moveToNext())

            // Row 2 — label was NULL → defaulted to ''
            assertEquals("com.test.app2/.Settings", cursor.getString(0))
            assertEquals("", cursor.getString(3)) // label NOT NULL enforced
            assertNull(cursor.getString(4))
            assertNull(cursor.getString(5))
        }

        // EblanAppWidgetProviderInfoEntity
        dbV5.query("SELECT componentName, label, serialNumber FROM `EblanAppWidgetProviderInfoEntity` ORDER BY serialNumber")
            .use { cursor ->
                assertTrue(cursor.moveToFirst())

                // Row 1
                assertEquals("com.example.clock", cursor.getString(0))
                assertEquals("Clock", cursor.getString(1))

                cursor.moveToNext()
                // Row 2 — previously NULL label → now empty string
                assertEquals("com.example.weather", cursor.getString(0))
                assertEquals("", cursor.getString(1))

                cursor.moveToNext()
                // Row 3
                assertEquals("com.example.notes", cursor.getString(0))
                assertEquals("My Notes", cursor.getString(1))
            }

        // ApplicationInfoGridItemEntity
        dbV5.query("SELECT id, label, customIcon, customLabel FROM `ApplicationInfoGridItemEntity` ORDER BY serialNumber")
            .use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("item1", c.getString(0))
                assertEquals("Browser", c.getString(1))
                assertNull(c.getString(2))
                assertNull(c.getString(3))

                assertTrue(c.moveToNext())

                assertEquals("item2", c.getString(0))
                assertEquals("", c.getString(1)) // NULL → ''
                assertNull(c.getString(2))
                assertNull(c.getString(3))
            }

        // WidgetGridItemEntity
        dbV5.query("SELECT id, label FROM `WidgetGridItemEntity` ORDER BY serialNumber").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Clock", c.getString(1))

            assertTrue(c.moveToNext())

            assertEquals("", c.getString(1)) // previously NULL → now empty string
        }

        // FolderGridItemEntity
        dbV5.query("SELECT label, pageCount, icon, iconSize FROM `FolderGridItemEntity`").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Work Apps", c.getString(0))
            assertEquals(3, c.getInt(1))
            assertNull(c.getString(2)) // icon = NULL (new column)
            assertEquals(64, c.getInt(3))
        }

        // ShortcutConfigGridItemEntity
        dbV5.query("SELECT activityLabel, iconSize, customIcon, customLabel FROM `ShortcutConfigGridItemEntity`")
            .use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("Browser", c.getString(0))
                assertEquals(56, c.getInt(1))
                assertNull(c.getString(2)) // customIcon
                assertNull(c.getString(3)) // customLabel
            }
    }
}
