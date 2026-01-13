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
import com.eblan.launcher.data.room.migration.Migration6To7
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class Migration6To7Test {
    private val testDatabase = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EblanDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate6To7() {
        // Create database at version 6
        helper.createDatabase(
            testDatabase,
            6,
        ).apply {
            // EblanApplicationInfoEntity
            execSQL(
                """
                INSERT INTO `EblanApplicationInfoEntity`
                (packageName, serialNumber, componentName, icon, label) 
                VALUES ('com.example.app', 1, 'com.example.app/.MainActivity', '/path/icon.png', 'Original App')
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
                ('com.example.clock', 100, 'com.example.app', 4, 2, 4, 2, 1, 2, 2, 8, 8, 'Clock')
                """.trimIndent(),
            )

            // EblanShortcutConfigEntity
            execSQL(
                """
                INSERT INTO `EblanShortcutConfigEntity` (
                    componentName, packageName, serialNumber,
                    activityIcon, activityLabel, applicationIcon, applicationLabel
              ) VALUES (
                   'com.example.clock',
                    'com.example.app',
                      100,
                    NULL,        
                    'Clock',     
                     NULL,        
                     'Example App'
    )
                """.trimIndent(),
            )

            // EblanShortcutInfoEntity
            execSQL(
                """
    INSERT INTO `EblanShortcutInfoEntity` (
        shortcutId, serialNumber, packageName,
        shortLabel, longLabel, icon,
        shortcutQueryFlag, isEnabled
    ) VALUES (
        'shortcut_clock',
        100,
        'com.example.app',
        'Clock',
        'Clock Shortcut',
        NULL,
        0,
        1,
    )
    """.trimIndent(),
            )

            close()
        }

        // Run migration and validate version 7
        val dbV7 = helper.runMigrationsAndValidate(
            testDatabase,
            7,
            true,
            Migration6To7(),
        )

        // EblanApplicationInfoEntity
        dbV7.query(
            """
            SELECT componentName, serialNumber, packageName, label, customIcon, customLabel, isHidden
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
            assertNull(cursor.getString(4))
            assertNull(cursor.getString(5))
            assertEquals(0, cursor.getInt(6))
            assertEquals(0, cursor.getLong(7))
        }

        // EblanAppWidgetProviderInfoEntity
        dbV7.query(
            """
    SELECT componentName, serialNumber, packageName, targetCellWidth, targetCellHeight,
           minWidth, minHeight, resizeMode, minResizeWidth, minResizeHeight,
           maxResizeWidth, maxResizeHeight, label, lastUpdateTime
    FROM `EblanAppWidgetProviderInfoEntity`
    ORDER BY serialNumber
    """.trimIndent(),
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())

            assertEquals("com.example.clock", cursor.getString(0))
            assertEquals(100, cursor.getLong(1))
            assertEquals("com.example.app", cursor.getString(2))
            assertEquals(4, cursor.getInt(3))
            assertEquals(2, cursor.getInt(4))
            assertEquals(4, cursor.getInt(5))
            assertEquals(2, cursor.getInt(6))
            assertEquals(1, cursor.getInt(7))
            assertEquals(2, cursor.getInt(8))
            assertEquals(2, cursor.getInt(9))
            assertEquals(8, cursor.getInt(10))
            assertEquals(8, cursor.getInt(11))
            assertEquals("Clock", cursor.getString(12))
            assertEquals(0, cursor.getLong(13))
        }

// EblanShortcutConfigEntity
        dbV7.query(
            """
    SELECT componentName, packageName, serialNumber,
           activityIcon, activityLabel, applicationIcon, applicationLabel, lastUpdateTime
    FROM `EblanShortcutConfigEntity`
    ORDER BY serialNumber
    """.trimIndent(),
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())

            assertEquals("com.example.clock", cursor.getString(0))
            assertEquals("com.example.app", cursor.getString(1))
            assertEquals(100, cursor.getLong(2))
            assertNull(cursor.getString(3))
            assertEquals("Clock", cursor.getString(4))
            assertNull(cursor.getString(5))
            assertEquals("Example App", cursor.getString(6))
            assertEquals(0, cursor.getLong(7))
        }

// EblanShortcutInfoEntity
        dbV7.query(
            """
    SELECT shortcutId, serialNumber, packageName,
           shortLabel, longLabel, icon,
           shortcutQueryFlag, isEnabled, lastUpdateTime
    FROM `EblanShortcutInfoEntity`
    ORDER BY serialNumber
    """.trimIndent(),
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())

            assertEquals("shortcut_clock", cursor.getString(0))
            assertEquals(100, cursor.getLong(1))
            assertEquals("com.example.app", cursor.getString(2))
            assertEquals("Clock", cursor.getString(3))
            assertEquals("Clock Shortcut", cursor.getString(4))
            assertNull(cursor.getString(5))
            assertEquals(0, cursor.getInt(6))
            assertEquals(1, cursor.getInt(7))
            assertEquals(0, cursor.getLong(8))
        }
    }
}
