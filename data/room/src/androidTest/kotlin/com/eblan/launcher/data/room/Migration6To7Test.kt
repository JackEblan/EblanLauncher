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
        INSERT INTO `EblanApplicationInfoEntity` (
        componentName,
        serialNumber,
        packageName,
        icon,
        label,
        customIcon,
        customLabel
    ) 
    VALUES( 
      'componentName', 
      0, 
      'packageName',   
      'icon', 
      'label',
       'customIcon',               
       'customLabel')
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
                ('componentName', 0, 'packageName', 0, 1, 2, 3, 4, 5, 6, 7, 8, 'label')
                """.trimIndent(),
            )

            // EblanShortcutConfigEntity
            execSQL(
                """
                INSERT INTO `EblanShortcutConfigEntity` (
                    componentName, packageName, serialNumber,
                    activityIcon, activityLabel, applicationIcon, applicationLabel
              ) VALUES (
                   'componentName',
                    'packageName',
                      0,
                    'activityIcon',        
                    'activityLabel',     
                     'applicationIcon',        
                     'applicationLabel')
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
        'shortcutId',
        0,
        'packageName',
        'shortLabel',
        'longLabel',
        'icon',
        0,
        1
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
        )

        // EblanApplicationInfoEntity
        dbV7.query(
            """
            SELECT componentName, serialNumber, packageName, icon, label, customIcon, customLabel, isHidden, lastUpdateTime
            FROM `EblanApplicationInfoEntity`
            """.trimIndent(),
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())

            assertEquals("componentName", cursor.getString(0))
            assertEquals(0, cursor.getInt(1))
            assertEquals("packageName", cursor.getString(2))
            assertEquals("icon", cursor.getString(3))
            assertEquals("label", cursor.getString(4))
            assertEquals("customIcon", cursor.getString(5))
            assertEquals("customLabel", cursor.getString(6))
            assertEquals(0, cursor.getInt(7))
            assertEquals(0, cursor.getLong(8))
        }

        // EblanAppWidgetProviderInfoEntity
        dbV7.query(
            """
    SELECT componentName, serialNumber, packageName, targetCellWidth, targetCellHeight,
           minWidth, minHeight, resizeMode, minResizeWidth, minResizeHeight,
           maxResizeWidth, maxResizeHeight, label, lastUpdateTime
    FROM `EblanAppWidgetProviderInfoEntity`
            """.trimIndent(),
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())

            assertEquals("componentName", cursor.getString(0))
            assertEquals(0, cursor.getLong(1))
            assertEquals("packageName", cursor.getString(2))
            assertEquals(0, cursor.getInt(3))
            assertEquals(1, cursor.getInt(4))
            assertEquals(2, cursor.getInt(5))
            assertEquals(3, cursor.getInt(6))
            assertEquals(4, cursor.getInt(7))
            assertEquals(5, cursor.getInt(8))
            assertEquals(6, cursor.getInt(9))
            assertEquals(7, cursor.getInt(10))
            assertEquals(8, cursor.getInt(11))
            assertEquals("label", cursor.getString(12))
            assertEquals(0, cursor.getLong(13))
        }

// EblanShortcutConfigEntity
        dbV7.query(
            """
    SELECT componentName, packageName, serialNumber,
           activityIcon, activityLabel, applicationIcon, applicationLabel, lastUpdateTime
    FROM `EblanShortcutConfigEntity`
            """.trimIndent(),
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())

            assertEquals("componentName", cursor.getString(0))
            assertEquals("packageName", cursor.getString(1))
            assertEquals(0, cursor.getLong(2))
            assertEquals("activityIcon", cursor.getString(3))
            assertEquals("activityLabel", cursor.getString(4))
            assertEquals("applicationIcon", cursor.getString(5))
            assertEquals("applicationLabel", cursor.getString(6))
            assertEquals(0, cursor.getLong(7))
        }

// EblanShortcutInfoEntity
        dbV7.query(
            """
    SELECT shortcutId, serialNumber, packageName,
           shortLabel, longLabel, icon,
           shortcutQueryFlag, isEnabled, lastUpdateTime
    FROM `EblanShortcutInfoEntity`
            """.trimIndent(),
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())

            assertEquals("shortcutId", cursor.getString(0))
            assertEquals(0, cursor.getLong(1))
            assertEquals("packageName", cursor.getString(2))
            assertEquals("shortLabel", cursor.getString(3))
            assertEquals("longLabel", cursor.getString(4))
            assertEquals("icon", cursor.getString(5))
            assertEquals(0, cursor.getInt(6))
            assertEquals(1, cursor.getInt(7))
            assertEquals(0, cursor.getLong(8))
        }
    }
}