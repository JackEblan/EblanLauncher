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
package com.eblan.launcher.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanUser
import com.eblan.launcher.ui.dialog.EblanActionDialog

@Composable
fun EblanActionSettings(
    doubleTap: EblanAction,
    eblanApplicationInfos: Map<EblanUser, List<EblanApplicationInfo>>,
    modifier: Modifier = Modifier,
    swipeDown: EblanAction,
    swipeUp: EblanAction,
    onUpdateDoubleTap: (EblanAction) -> Unit,
    onUpdateSwipeDown: (EblanAction) -> Unit,
    onUpdateSwipeUp: (EblanAction) -> Unit,
) {
    val context = LocalContext.current

    var showDoubleTapDialog by remember { mutableStateOf(false) }

    var showSwipeUpDialog by remember { mutableStateOf(false) }

    var showSwipeDownDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
    ) {
        SettingsColumn(
            subtitle = "Perform global actions",
            title = "Accessibility Services",
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            },
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        SettingsColumn(
            subtitle = doubleTap.eblanActionType.getEblanActionTypeSubtitle(componentName = doubleTap.componentName),
            title = "Double Tap",
            onClick = {
                showDoubleTapDialog = true
            },
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        SettingsColumn(
            subtitle = swipeUp.eblanActionType.getEblanActionTypeSubtitle(componentName = swipeUp.componentName),
            title = "Swipe Up",
            onClick = {
                showSwipeUpDialog = true
            },
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        SettingsColumn(
            subtitle = swipeDown.eblanActionType.getEblanActionTypeSubtitle(componentName = swipeDown.componentName),
            title = "Swipe Down",
            onClick = {
                showSwipeDownDialog = true
            },
        )
    }

    if (showDoubleTapDialog) {
        EblanActionDialog(
            eblanAction = doubleTap,
            eblanApplicationInfos = eblanApplicationInfos,
            title = "Double Tap",
            onDismissRequest = {
                showDoubleTapDialog = false
            },
            onSelectEblanAction = { newEblanAction ->
                onUpdateDoubleTap(newEblanAction)

                showDoubleTapDialog = false
            },
        )
    }

    if (showSwipeUpDialog) {
        EblanActionDialog(
            eblanAction = swipeUp,
            eblanApplicationInfos = eblanApplicationInfos,
            title = "Swipe Up",
            onDismissRequest = {
                showSwipeUpDialog = false
            },
            onSelectEblanAction = { newEblanAction ->
                onUpdateSwipeUp(newEblanAction)

                showSwipeUpDialog = false
            },
        )
    }

    if (showSwipeDownDialog) {
        EblanActionDialog(
            eblanAction = swipeDown,
            eblanApplicationInfos = eblanApplicationInfos,
            title = "Swipe Down",
            onDismissRequest = {
                showSwipeDownDialog = false
            },
            onSelectEblanAction = { newEblanAction ->
                onUpdateSwipeDown(newEblanAction)

                showSwipeDownDialog = false
            },
        )
    }
}

fun EblanActionType.getEblanActionTypeSubtitle(componentName: String): String = when (this) {
    EblanActionType.None -> "None"
    EblanActionType.OpenApp -> "Open ${componentName.ifBlank { "App" }}"
    EblanActionType.OpenAppDrawer -> "Open App Drawer"
    EblanActionType.OpenNotificationPanel -> "Open Notification Panel"
    EblanActionType.LockScreen -> "Lock Screen"
    EblanActionType.OpenQuickSettings -> "Open Quick Settings"
    EblanActionType.OpenRecents -> "Open Recents"
}
