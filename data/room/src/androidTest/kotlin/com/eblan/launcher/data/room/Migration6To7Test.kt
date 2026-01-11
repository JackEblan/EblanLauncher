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

            close()
        }

        // Run migration and validate version 7
        val dbV5 = helper.runMigrationsAndValidate(
            testDatabase,
            7,
            true,
            Migration6To7(),
        )

        // EblanApplicationInfoEntity
        dbV5.query(
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
            assertNull(cursor.getString(4)) // customIcon
            assertNull(cursor.getString(5)) // customLabel
            assertEquals(0, cursor.getInt(6))
        }
    }
}
