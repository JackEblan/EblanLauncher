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
package com.eblan.launcher.feature.settings.gestures

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.feature.settings.gestures.dialog.EblanActionDialog
import com.eblan.launcher.feature.settings.gestures.model.GesturesSettingsUiState
import com.eblan.launcher.ui.settings.SettingsColumn

@Composable
internal fun GestureSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: GestureSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val gesturesSettingsUiState by viewModel.gesturesSettingsUiState.collectAsStateWithLifecycle()

    GestureSettingsScreen(
        modifier = modifier,
        gesturesSettingsUiState = gesturesSettingsUiState,
        onNavigateUp = onNavigateUp,
        onUpdateGestureSettings = viewModel::updateGestureSettings,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GestureSettingsScreen(
    modifier: Modifier = Modifier,
    gesturesSettingsUiState: GesturesSettingsUiState,
    onNavigateUp: () -> Unit,
    onUpdateGestureSettings: (GestureSettings) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Gestures")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = EblanLauncherIcons.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (gesturesSettingsUiState) {
                GesturesSettingsUiState.Loading -> {
                }

                is GesturesSettingsUiState.Success -> {
                    Success(
                        modifier = modifier,
                        gestureSettings = gesturesSettingsUiState.gestureSettings,
                        eblanApplicationInfos = gesturesSettingsUiState.eblanApplicationInfos,
                        onUpdateGestureSettings = onUpdateGestureSettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    gestureSettings: GestureSettings,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onUpdateGestureSettings: (GestureSettings) -> Unit,
) {
    val context = LocalContext.current

    var showDoubleTapDialog by remember { mutableStateOf(false) }

    var showSwipeUpDialog by remember { mutableStateOf(false) }

    var showSwipeDownDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
        ) {
            SettingsColumn(
                title = "Accessibility Services",
                subtitle = "Perform global actions",
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                },
            )

            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            SettingsColumn(
                title = "Double Tap",
                subtitle = gestureSettings.doubleTap.getEblanActionSubtitle(),
                onClick = {
                    showDoubleTapDialog = true
                },
            )

            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            SettingsColumn(
                title = "Swipe Up",
                subtitle = gestureSettings.swipeUp.getEblanActionSubtitle(),
                onClick = {
                    showSwipeUpDialog = true
                },
            )

            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            SettingsColumn(
                title = "Swipe Down",
                subtitle = gestureSettings.swipeDown.getEblanActionSubtitle(),
                onClick = {
                    showSwipeDownDialog = true
                },
            )
        }
    }

    if (showDoubleTapDialog) {
        EblanActionDialog(
            title = "Double Tap",
            eblanAction = gestureSettings.doubleTap,
            eblanApplicationInfos = eblanApplicationInfos,
            onUpdateEblanAction = { doubleTap ->
                onUpdateGestureSettings(gestureSettings.copy(doubleTap = doubleTap))
            },
            onDismissRequest = {
                showDoubleTapDialog = false
            },
        )
    }

    if (showSwipeUpDialog) {
        EblanActionDialog(
            title = "Swipe Up",
            eblanAction = gestureSettings.swipeUp,
            eblanApplicationInfos = eblanApplicationInfos,
            onUpdateEblanAction = { swipeUp ->
                onUpdateGestureSettings(gestureSettings.copy(swipeUp = swipeUp))
            },
            onDismissRequest = {
                showSwipeUpDialog = false
            },
        )
    }

    if (showSwipeDownDialog) {
        EblanActionDialog(
            title = "Swipe Down",
            eblanAction = gestureSettings.swipeDown,
            eblanApplicationInfos = eblanApplicationInfos,
            onUpdateEblanAction = { swipeDown ->
                onUpdateGestureSettings(gestureSettings.copy(swipeDown = swipeDown))
            },
            onDismissRequest = {
                showSwipeDownDialog = false
            },
        )
    }
}

fun EblanAction.getEblanActionSubtitle(): String = when (this) {
    EblanAction.None -> "None"
    is EblanAction.OpenApp -> "Open $componentName"
    EblanAction.OpenAppDrawer -> "Open app drawer"
    EblanAction.OpenNotificationPanel -> "Open notification panel"
    EblanAction.LockScreen -> "Lock screen"
    EblanAction.OpenQuickSettings -> "Open quick settings"
    EblanAction.OpenRecents -> "Open recents"
}
