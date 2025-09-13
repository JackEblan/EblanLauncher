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
package com.eblan.launcher.data.repository.mapper

import com.eblan.launcher.data.room.entity.ShortcutInfoGridItemEntity
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ShortcutInfoGridItem

internal fun ShortcutInfoGridItemEntity.asGridItem(): GridItem {
    return GridItem(
        id = id,
        folderId = folderId,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        data = GridItemData.ShortcutInfo(
            shortcutId = shortcutId,
            packageName = packageName,
            shortLabel = shortLabel,
            longLabel = longLabel,
            icon = icon,
            eblanApplicationInfo = eblanApplicationInfo,
        ),
        associate = associate,
        override = override,
        gridItemSettings = gridItemSettings,
    )
}

internal fun ShortcutInfoGridItemEntity.asModel(): ShortcutInfoGridItem {
    return ShortcutInfoGridItem(
        id = id,
        folderId = folderId,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        associate = associate,
        shortcutId = shortcutId,
        packageName = packageName,
        shortLabel = shortLabel,
        longLabel = longLabel,
        icon = icon,
        override = override,
        gridItemSettings = gridItemSettings,
        eblanApplicationInfo = eblanApplicationInfo,
    )
}

internal fun ShortcutInfoGridItem.asEntity(): ShortcutInfoGridItemEntity {
    return ShortcutInfoGridItemEntity(
        id = id,
        folderId = folderId,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        associate = associate,
        shortcutId = shortcutId,
        packageName = packageName,
        shortLabel = shortLabel,
        longLabel = longLabel,
        icon = icon,
        override = override,
        gridItemSettings = gridItemSettings,
        eblanApplicationInfo = eblanApplicationInfo,
    )
}
