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
package com.eblan.launcher.domain.model

data class ShortcutInfoGridItem(
    val id: String,
    val folderId: String?,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val associate: Associate,
    val shortcutId: String,
    val packageName: String,
    val shortLabel: String,
    val longLabel: String,
    val icon: String?,
    val override: Boolean,
    val gridItemSettings: GridItemSettings,
    val eblanApplicationInfo: EblanApplicationInfo,
)
