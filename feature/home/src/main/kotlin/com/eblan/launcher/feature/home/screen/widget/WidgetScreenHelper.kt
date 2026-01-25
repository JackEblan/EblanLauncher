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
package com.eblan.launcher.feature.home.screen.widget

import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemAction
import com.eblan.launcher.domain.model.GridItemActionType
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings

internal fun getWidgetGridItem(
    id: String,
    page: Int,
    componentName: String,
    configure: String?,
    packageName: String,
    serialNumber: Long,
    targetCellHeight: Int,
    targetCellWidth: Int,
    minWidth: Int,
    minHeight: Int,
    resizeMode: Int,
    minResizeWidth: Int,
    minResizeHeight: Int,
    maxResizeWidth: Int,
    maxResizeHeight: Int,
    preview: String?,
    label: String,
    icon: String?,
    gridItemSettings: GridItemSettings,
): GridItem {
    val data = GridItemData.Widget(
        appWidgetId = 0,
        componentName = componentName,
        packageName = packageName,
        serialNumber = serialNumber,
        configure = configure,
        minWidth = minWidth,
        minHeight = minHeight,
        resizeMode = resizeMode,
        minResizeWidth = minResizeWidth,
        minResizeHeight = minResizeHeight,
        maxResizeWidth = maxResizeWidth,
        maxResizeHeight = maxResizeHeight,
        targetCellHeight = targetCellHeight,
        targetCellWidth = targetCellWidth,
        preview = preview,
        label = label,
        icon = icon,
    )

    return GridItem(
        id = id,
        folderId = null,
        page = page,
        startColumn = -1,
        startRow = -1,
        columnSpan = 1,
        rowSpan = 1,
        data = data,
        associate = Associate.Grid,
        override = false,
        gridItemSettings = gridItemSettings,
        doubleTap = GridItemAction(
            gridItemActionType = GridItemActionType.None,
            componentName = "",
        ),
        swipeUp = GridItemAction(
            gridItemActionType = GridItemActionType.None,
            componentName = "",
        ),
        swipeDown = GridItemAction(
            gridItemActionType = GridItemActionType.None,
            componentName = "",
        ),
    )
}
