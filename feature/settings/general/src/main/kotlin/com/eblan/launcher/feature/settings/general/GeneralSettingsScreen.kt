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
package com.eblan.launcher.feature.settings.general

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.designsystem.theme.supportsDynamicTheming
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.GeneralSettings
import com.eblan.launcher.domain.model.ThemeBrand
import com.eblan.launcher.feature.settings.general.model.GeneralSettingsUiState
import com.eblan.launcher.ui.dialog.RadioOptionsDialog
import com.eblan.launcher.ui.settings.SettingsColumn
import com.eblan.launcher.ui.settings.SettingsSwitch

@Composable
fun GeneralSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: GeneralSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val generalSettingsUiState by viewModel.generalSettingsUiState.collectAsStateWithLifecycle()

    GeneralSettingsScreen(
        modifier = modifier,
        generalSettingsUiState = generalSettingsUiState,
        onUpdateThemeBrand = viewModel::updateThemeBrand,
        onUpdateDarkThemeConfig = viewModel::updateDarkThemeConfig,
        onUpdateDynamicTheme = viewModel::updateDynamicTheme,
        onNavigateUp = onNavigateUp,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
    modifier: Modifier = Modifier,
    generalSettingsUiState: GeneralSettingsUiState,
    onUpdateThemeBrand: (ThemeBrand) -> Unit,
    onUpdateDarkThemeConfig: (DarkThemeConfig) -> Unit,
    onUpdateDynamicTheme: (Boolean) -> Unit,
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "General")
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
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {
            when (generalSettingsUiState) {
                GeneralSettingsUiState.Loading -> {
                }

                is GeneralSettingsUiState.Success -> {
                    Success(
                        modifier = modifier,
                        generalSettings = generalSettingsUiState.generalSettings,
                        onUpdateThemeBrand = onUpdateThemeBrand,
                        onUpdateDarkThemeConfig = onUpdateDarkThemeConfig,
                        onUpdateDynamicTheme = onUpdateDynamicTheme,
                    )
                }
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    generalSettings: GeneralSettings,
    onUpdateThemeBrand: (ThemeBrand) -> Unit,
    onUpdateDarkThemeConfig: (DarkThemeConfig) -> Unit,
    onUpdateDynamicTheme: (Boolean) -> Unit,
) {
    var showThemeBrandDialog by remember { mutableStateOf(false) }

    var showDarkThemeConfigDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsColumn(
            title = "Theme Brand",
            subtitle = generalSettings.themeBrand.name,
            onClick = {
                showThemeBrandDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Dark Theme Config",
            subtitle = generalSettings.darkThemeConfig.name,
            onClick = {
                showDarkThemeConfigDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsSwitch(
            checked = generalSettings.dynamicTheme,
            title = "Dynamic Theme",
            subtitle = "Dynamic theme",
            enabled = supportsDynamicTheming(),
            onCheckedChange = onUpdateDynamicTheme,
        )
    }

    if (showThemeBrandDialog) {
        RadioOptionsDialog(
            title = "Text Color",
            options = listOf(
                ThemeBrand.Green,
                ThemeBrand.Purple,
            ),
            selected = generalSettings.themeBrand,
            label = {
                it.name
            },
            onDismissRequest = {
                showThemeBrandDialog = false
            },
            onUpdateClick = {
                onUpdateThemeBrand(it)

                showThemeBrandDialog = false
            },
        )
    }

    if (showDarkThemeConfigDialog) {
        RadioOptionsDialog(
            title = "Text Color",
            options = listOf(
                DarkThemeConfig.System,
                DarkThemeConfig.Light,
                DarkThemeConfig.Dark,
            ),
            selected = generalSettings.darkThemeConfig,
            label = {
                it.name
            },
            onDismissRequest = {
                showDarkThemeConfigDialog = false
            },
            onUpdateClick = {
                onUpdateDarkThemeConfig(it)

                showDarkThemeConfigDialog = false
            },
        )
    }
}
