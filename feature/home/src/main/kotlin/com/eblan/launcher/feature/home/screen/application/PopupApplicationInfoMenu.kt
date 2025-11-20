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
package com.eblan.launcher.feature.home.screen.application

import android.graphics.Rect
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.popup.GridItemPopupPositionProvider
import com.eblan.launcher.ui.local.LocalLauncherApps

@Composable
internal fun PopupApplicationInfoMenu(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    popupMenuIntOffset: IntOffset,
    gridItem: GridItem?,
    popupMenuIntSize: IntSize,
    onDismissRequest: () -> Unit,
) {
    val applicationInfo = gridItem?.data as? GridItemData.ApplicationInfo ?: return

    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val x = popupMenuIntOffset.x - leftPadding

    val y = popupMenuIntOffset.y - topPadding

    Popup(
        popupPositionProvider = GridItemPopupPositionProvider(
            x = x,
            y = y,
            width = popupMenuIntSize.width,
            height = popupMenuIntSize.height,
        ),
        onDismissRequest = onDismissRequest,
        content = {
            ApplicationInfoMenu(
                modifier = modifier,
                onApplicationInfo = {
                    launcherApps.startAppDetailsActivity(
                        serialNumber = applicationInfo.serialNumber,
                        componentName = applicationInfo.componentName,
                        sourceBounds = Rect(
                            x,
                            y,
                            x + popupMenuIntSize.width,
                            y + popupMenuIntSize.height,
                        ),
                    )

                    onDismissRequest()
                },
            )
        },
    )
}

@Composable
private fun ApplicationInfoMenu(
    modifier: Modifier = Modifier,
    onApplicationInfo: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Row {
                IconButton(
                    onClick = onApplicationInfo,
                ) {
                    Icon(imageVector = EblanLauncherIcons.Info, contentDescription = null)
                }
            }
        },
    )
}
