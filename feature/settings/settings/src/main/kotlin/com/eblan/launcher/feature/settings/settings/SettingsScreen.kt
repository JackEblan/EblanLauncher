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
package com.eblan.launcher.feature.settings.settings

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.feature.settings.settings.model.SettingsUiState
import com.eblan.launcher.ui.local.LocalPackageManager
import com.eblan.launcher.ui.settings.HintRow

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onFinish: () -> Unit,
    onGeneral: () -> Unit,
    onHome: () -> Unit,
    onAppDrawer: () -> Unit,
    onGestures: () -> Unit,
    onFolder: () -> Unit,
) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()

    SettingsScreen(
        modifier = modifier,
        settingsUiState = settingsUiState,
        onFinish = onFinish,
        onGeneral = onGeneral,
        onHome = onHome,
        onAppDrawer = onAppDrawer,
        onGestures = onGestures,
        onFolder = onFolder,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsUiState: SettingsUiState,
    onFinish: () -> Unit,
    onGeneral: () -> Unit,
    onHome: () -> Unit,
    onAppDrawer: () -> Unit,
    onGestures: () -> Unit,
    onFolder: () -> Unit,
) {
    BackHandler {
        onFinish()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Settings")
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
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
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {
            when (settingsUiState) {
                SettingsUiState.Loading -> {
                }

                is SettingsUiState.Success -> {
                    Success(
                        onGeneral = onGeneral,
                        onHome = onHome,
                        onAppDrawer = onAppDrawer,
                        onGestures = onGestures,
                        onFolder = onFolder,
                    )
                }
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    onGeneral: () -> Unit,
    onHome: () -> Unit,
    onAppDrawer: () -> Unit,
    onGestures: () -> Unit,
    onFolder: () -> Unit,
) {
    val context = LocalContext.current

    val packageManager = LocalPackageManager.current

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
    ) {
        if (!packageManager.isDefaultLauncher()) {
            HintRow(
                hint = "Set Eblan Launcher as your default launcher",
                onClick = {
                    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                    context.startActivity(intent)
                },
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        SettingsRow(
            imageVector = EblanLauncherIcons.Settings,
            title = "General",
            subtitle = "Themes, icon packs",
            onClick = onGeneral,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsRow(
            imageVector = EblanLauncherIcons.Home,
            title = "Home",
            subtitle = "Grid, icon, dock, and more",
            onClick = onHome,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsRow(
            imageVector = EblanLauncherIcons.Apps,
            title = "App Drawer",
            subtitle = "Columns and rows count",
            onClick = onAppDrawer,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsRow(
            imageVector = EblanLauncherIcons.Folder,
            title = "Folder",
            subtitle = "Columns and rows count",
            onClick = onFolder,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsRow(
            imageVector = EblanLauncherIcons.Gesture,
            title = "Gestures",
            subtitle = "Swipe gesture actions",
            onClick = onGestures,
        )
    }
}

@Composable
private fun SettingsRow(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )

        Spacer(modifier = Modifier.width(5.dp))

        Column {
            Text(text = title)

            Spacer(modifier = Modifier.height(5.dp))

            Text(text = subtitle)
        }
    }
}
