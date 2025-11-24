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
package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ShortcutConfigActivityGridItem
import com.eblan.launcher.domain.model.UpdateApplicationInfoGridItem
import kotlinx.coroutines.flow.Flow

interface ShortcutConfigActivityGridItemRepository {
    val gridItems: Flow<List<GridItem>>

    val shortcutConfigActivityGridItems: Flow<List<ShortcutConfigActivityGridItem>>

    suspend fun upsertShortcutConfigActivityGridItems(shortcutConfigActivityGridItems: List<ShortcutConfigActivityGridItem>)

    suspend fun updateShortcutConfigActivityGridItem(shortcutConfigActivityGridItem: ShortcutConfigActivityGridItem)

    suspend fun deleteShortcutConfigActivityGridItems(shortcutConfigActivityGridItems: List<ShortcutConfigActivityGridItem>)

    suspend fun deleteShortcutConfigActivityGridItem(shortcutConfigActivityGridItem: ShortcutConfigActivityGridItem)

    suspend fun getShortcutConfigActivityGridItems(
        serialNumber: Long,
        packageName: String,
    ): List<ShortcutConfigActivityGridItem>

    suspend fun deleteShortcutConfigActivityGridItem(
        serialNumber: Long,
        packageName: String,
    )

    suspend fun updateShortcutConfigActivityGridItems(updateApplicationInfoGridItems: List<UpdateApplicationInfoGridItem>)
}
