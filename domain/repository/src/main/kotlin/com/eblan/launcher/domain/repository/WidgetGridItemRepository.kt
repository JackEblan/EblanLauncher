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
import com.eblan.launcher.domain.model.WidgetGridItem
import kotlinx.coroutines.flow.Flow

interface WidgetGridItemRepository {
    val widgetGridItems: Flow<List<GridItem>>

    suspend fun upsertWidgetGridItems(widgetGridItems: List<WidgetGridItem>)

    suspend fun upsertWidgetGridItem(widgetGridItem: WidgetGridItem): Long

    suspend fun updateWidgetGridItem(widgetGridItem: WidgetGridItem)

    suspend fun getWidgetGridItem(id: String): WidgetGridItem?

    suspend fun deleteWidgetGridItems(widgetGridItems: List<WidgetGridItem>)

    suspend fun deleteWidgetGridItem(widgetGridItem: WidgetGridItem)
}
