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

import com.eblan.launcher.data.room.entity.FolderGridItemEntity
import com.eblan.launcher.data.room.entity.FolderGridItemWrapperEntity
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData

internal fun FolderGridItemWrapperEntity.asGridItem(): GridItem = GridItem(
    id = folderGridItemEntity.id,
    folderId = folderGridItemEntity.folderId,
    page = folderGridItemEntity.page,
    startColumn = folderGridItemEntity.startColumn,
    startRow = folderGridItemEntity.startRow,
    columnSpan = folderGridItemEntity.columnSpan,
    rowSpan = folderGridItemEntity.rowSpan,
    data = asFolderGridItemData(),
    associate = folderGridItemEntity.associate,
    override = folderGridItemEntity.override,
    gridItemSettings = folderGridItemEntity.gridItemSettings,
    gridItemAction = folderGridItemEntity.gridItemAction,
)

internal fun FolderGridItemWrapperEntity.asFolderGridItemData(): GridItemData.Folder {
    val applicationInfos = applicationInfos.map { applicationInfoGridItemEntity ->
        applicationInfoGridItemEntity.asGridItem()
    }

    val widgets = widgets.map { widgetGridItemEntity ->
        widgetGridItemEntity.asGridItem()
    }

    val shortcutInfos = shortcutInfos.map { shortcutGridItemEntity ->
        shortcutGridItemEntity.asGridItem()
    }

    val folders = folders.map { folderGridItemEntity ->
        folderGridItemEntity.asGridItem()
    }

    val shortcutConfigs = shortcutConfigs.map { shortcutConfigGridItemEntity ->
        shortcutConfigGridItemEntity.asGridItem()
    }

    return GridItemData.Folder(
        id = folderGridItemEntity.id,
        label = folderGridItemEntity.label,
        gridItems = applicationInfos + widgets + shortcutInfos + folders + shortcutConfigs,
        pageCount = folderGridItemEntity.pageCount,
        icon = folderGridItemEntity.icon,
    )
}

@JvmName("FolderGridItemEntity")
internal fun FolderGridItemEntity.asGridItem(): GridItem = GridItem(
    id = id,
    folderId = folderId,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    data = GridItemData.Folder(
        id = id,
        label = label,
        gridItems = emptyList(),
        pageCount = pageCount,
        icon = icon,
    ),
    associate = associate,
    override = override,
    gridItemSettings = gridItemSettings,
    gridItemAction = gridItemAction,
)

internal fun FolderGridItem.asEntity(): FolderGridItemEntity = FolderGridItemEntity(
    id = id,
    folderId = folderId,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    label = label,
    override = override,
    pageCount = pageCount,
    icon = icon,
    gridItemSettings = gridItemSettings,
    gridItemAction = gridItemAction,
)

internal fun GridItem.asFolderGridItem(data: GridItemData.Folder): FolderGridItem = FolderGridItem(
    id = id,
    folderId = folderId,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    label = data.label,
    override = override,
    pageCount = data.pageCount,
    icon = data.icon,
    gridItemSettings = gridItemSettings,
    gridItemAction = gridItemAction,
)
