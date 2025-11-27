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

import com.eblan.launcher.data.room.entity.ShortcutConfigActivityGridItemEntity
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ShortcutConfigActivityGridItem

internal fun ShortcutConfigActivityGridItemEntity.asGridItem(): GridItem {
    return GridItem(
        id = id,
        folderId = folderId,
        page = page,
        startColumn = startColumn,
        startRow = startRow,
        columnSpan = columnSpan,
        rowSpan = rowSpan,
        data = GridItemData.ShortcutConfigActivity(
            serialNumber = serialNumber,
            componentName = componentName,
            packageName = packageName,
            icon = icon,
            label = label,
            uri = uri,
            uriIcon = uriIcon,
        ),
        associate = associate,
        override = override,
        gridItemSettings = gridItemSettings,
    )
}

internal fun ShortcutConfigActivityGridItemEntity.asModel(): ShortcutConfigActivityGridItem {
    return ShortcutConfigActivityGridItem(
        id = id,
        folderId = folderId,
        page = page,
        startColumn = startColumn,
        startRow = startRow,
        columnSpan = columnSpan,
        rowSpan = rowSpan,
        associate = associate,
        componentName = componentName,
        packageName = packageName,
        icon = icon,
        label = label,
        override = override,
        serialNumber = serialNumber,
        uri = uri,
        uriIcon = uriIcon,
        gridItemSettings = gridItemSettings,
    )
}

internal fun ShortcutConfigActivityGridItem.asEntity(): ShortcutConfigActivityGridItemEntity {
    return ShortcutConfigActivityGridItemEntity(
        id = id,
        folderId = folderId,
        page = page,
        startColumn = startColumn,
        startRow = startRow,
        columnSpan = columnSpan,
        rowSpan = rowSpan,
        associate = associate,
        componentName = componentName,
        packageName = packageName,
        icon = icon,
        label = label,
        override = override,
        serialNumber = serialNumber,
        uri = uri,
        uriIcon = uriIcon,
        gridItemSettings = gridItemSettings,
    )
}
