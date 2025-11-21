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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.feature.settings.gestures.dialog.GestureActionBottomSheet
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

    var showDoubleTapBottomSheet by remember { mutableStateOf(false) }

    var showSwipeUpBottomSheet by remember { mutableStateOf(false) }

    var showSwipeDownBottomSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsColumn(
            title = "Accessibility Services",
            subtitle = "Grant Eblan Launcher to use Accessibility services",
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            },
        )

        SettingsColumn(
            title = "Double tap",
            subtitle = gestureSettings.doubleTap.getGestureActionSubtitle(),
            onClick = {
                showDoubleTapBottomSheet = true
            },
        )

        SettingsColumn(
            title = "Swipe up",
            subtitle = gestureSettings.swipeUp.getGestureActionSubtitle(),
            onClick = {
                showSwipeUpBottomSheet = true
            },
        )

        SettingsColumn(
            title = "Swipe down",
            subtitle = gestureSettings.swipeDown.getGestureActionSubtitle(),
            onClick = {
                showSwipeDownBottomSheet = true
            },
        )
    }

    if (showDoubleTapBottomSheet) {
        GestureActionBottomSheet(
            title = "Double Tap",
            gestureAction = gestureSettings.doubleTap,
            eblanApplicationInfos = eblanApplicationInfos,
            onUpdateGestureAction = { doubleTap ->
                onUpdateGestureSettings(gestureSettings.copy(doubleTap = doubleTap))
            },
            onDismiss = {
                showDoubleTapBottomSheet = false
            },
        )
    }

    if (showSwipeUpBottomSheet) {
        GestureActionBottomSheet(
            title = "Swipe Up",
            gestureAction = gestureSettings.swipeUp,
            eblanApplicationInfos = eblanApplicationInfos,
            onUpdateGestureAction = { swipeUp ->
                onUpdateGestureSettings(gestureSettings.copy(swipeUp = swipeUp))
            },
            onDismiss = {
                showSwipeUpBottomSheet = false
            },
        )
    }

    if (showSwipeDownBottomSheet) {
        GestureActionBottomSheet(
            title = "Swipe Down",
            gestureAction = gestureSettings.swipeDown,
            eblanApplicationInfos = eblanApplicationInfos,
            onUpdateGestureAction = { swipeDown ->
                onUpdateGestureSettings(gestureSettings.copy(swipeDown = swipeDown))
            },
            onDismiss = {
                showSwipeDownBottomSheet = false
            },
        )
    }
}

fun GestureAction.getGestureActionSubtitle(): String {
    return when (this) {
        GestureAction.None -> "None"
        is GestureAction.OpenApp -> "Open $componentName"
        GestureAction.OpenAppDrawer -> "Open app drawer"
        GestureAction.OpenNotificationPanel -> "Open notification panel"
        GestureAction.LockScreen -> "Lock screen"
        GestureAction.OpenQuickSettings -> "Open quick settings"
        GestureAction.OpenRecents -> "Open recents"
    }
}
