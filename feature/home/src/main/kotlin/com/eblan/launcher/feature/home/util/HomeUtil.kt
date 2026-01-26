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
package com.eblan.launcher.feature.home.util

import android.content.Intent
import com.eblan.launcher.feature.home.model.Screen

internal fun handleActionMainIntent(
    intent: Intent,
    onUpdateScreen: (Screen) -> Unit,
) {
    if (intent.action != Intent.ACTION_MAIN && !intent.hasCategory(Intent.CATEGORY_HOME)) {
        return
    }

    if ((intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
        return
    }

    onUpdateScreen(Screen.Pager)
}

internal const val KUSTOM_ACTION = "org.kustom.action.SEND_VAR"
internal const val KUSTOM_ACTION_EXT_NAME = "org.kustom.action.EXT_NAME"
internal const val KUSTOM_ACTION_VAR_NAME = "org.kustom.action.VAR_NAME"
internal const val KUSTOM_ACTION_VAR_VALUE = "org.kustom.action.VAR_VALUE"

internal const val PAGE_INDICATOR_HEIGHT = 30
internal const val EDGE_DISTANCE = 15
internal const val DRAG_HANDLE_SIZE = 30
internal const val GRID_ITEM_MAX_SWIPE_Y = 40
