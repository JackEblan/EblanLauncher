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
package com.eblan.launcher.feature.home.component.modifier

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.handleEblanAction
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun onDoubleTap(
    context: Context,
    doubleTap: EblanAction,
    launcherApps: AndroidLauncherAppsWrapper,
    scope: CoroutineScope,
    onOpenAppDrawer: () -> Unit,
) {
    if (doubleTap.eblanActionType == EblanActionType.None) return

    scope.launch {
        handleEblanAction(
            context = context,
            eblanAction = doubleTap,
            launcherApps = launcherApps,
            onOpenAppDrawer = onOpenAppDrawer,
        )
    }
}

internal fun onLongPress(
    scope: CoroutineScope,
    graphicsLayer: GraphicsLayer,
    intOffset: IntOffset,
    intSize: IntSize,
    gridItemSource: GridItemSource,
    sharedElementKey: SharedElementKey,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsLongPress: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateShowGridItemPopup: (Boolean) -> Unit,
) {
    scope.launch {
        onUpdateGridItemSource(gridItemSource)

        onUpdateImageBitmap(graphicsLayer.toImageBitmap())

        onUpdateOverlayBounds(
            intOffset,
            intSize,
        )

        onUpdateSharedElementKey(sharedElementKey)

        onUpdateIsLongPress(true)

        onUpdateShowGridItemPopup(true)
    }
}
