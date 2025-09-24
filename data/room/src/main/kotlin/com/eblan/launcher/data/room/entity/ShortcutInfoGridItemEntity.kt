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
package com.eblan.launcher.data.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItemSettings

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = EblanApplicationInfoEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["packageName"])],
)
data class ShortcutInfoGridItemEntity(
    @PrimaryKey
    val id: String,
    val folderId: String?,
    val page: Int,
    val startColumn: Int,
    val startRow: Int,
    val columnSpan: Int,
    val rowSpan: Int,
    val associate: Associate,
    val shortcutId: String,
    val packageName: String,
    val shortLabel: String,
    val longLabel: String,
    val icon: String?,
    val override: Boolean,
    @Embedded val gridItemSettings: GridItemSettings,
    @Embedded(prefix = "applicationInfo_") val eblanApplicationInfo: EblanApplicationInfo,
)
