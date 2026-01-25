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
package com.eblan.launcher.feature.editgriditem

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemAction
import com.eblan.launcher.domain.model.GridItemActionType
import com.eblan.launcher.domain.model.GridItemData

internal fun getGridItem(gridItem: GridItem, customIcon: String?): GridItem = when (val data = gridItem.data) {
    is GridItemData.ApplicationInfo -> {
        val newData = data.copy(customIcon = customIcon)

        gridItem.copy(data = newData)
    }

    is GridItemData.Folder -> {
        val newData = data.copy(icon = customIcon)

        gridItem.copy(data = newData)
    }

    is GridItemData.ShortcutConfig -> {
        val newData = data.copy(customIcon = customIcon)

        gridItem.copy(data = newData)
    }

    is GridItemData.ShortcutInfo -> {
        val newData = data.copy(customIcon = customIcon)

        gridItem.copy(data = newData)
    }

    else -> gridItem
}

internal fun GridItemActionType.getGridItemActionSubtitle(componentName: String) = when (this) {
    GridItemActionType.None -> "None"
    GridItemActionType.OpenAppDrawer -> "Open app drawer"
    GridItemActionType.OpenNotificationPanel -> "Open notification panel"
    GridItemActionType.OpenApp -> "Open $componentName"
    GridItemActionType.LockScreen -> "Lock screen"
    GridItemActionType.OpenQuickSettings -> "Open quick settings"
    GridItemActionType.OpenRecents -> "Open recents"
}
