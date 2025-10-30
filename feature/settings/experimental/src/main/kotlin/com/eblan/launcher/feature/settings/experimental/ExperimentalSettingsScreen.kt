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
package com.eblan.launcher.feature.settings.experimental

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.ExperimentalSettings
import com.eblan.launcher.feature.settings.experimental.dialog.SyncDataDialog
import com.eblan.launcher.feature.settings.experimental.model.ExperimentalSettingsUiState
import com.eblan.launcher.ui.settings.SettingsColumn

@Composable
fun ExperimentalSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: ExperimentalSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val experimentalSettingsUiState by viewModel.experimentalSettingsUiState.collectAsStateWithLifecycle()

    ExperimentalSettingsScreen(
        modifier = modifier,
        experimentalSettingsUiState = experimentalSettingsUiState,
        onUpdateExperimentalSettings = viewModel::updateExperimentalSettings,
        onNavigateUp = onNavigateUp,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalSettingsScreen(
    modifier: Modifier = Modifier,
    experimentalSettingsUiState: ExperimentalSettingsUiState,
    onUpdateExperimentalSettings: (ExperimentalSettings) -> Unit,
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Experimental")
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
            when (experimentalSettingsUiState) {
                ExperimentalSettingsUiState.Loading -> {
                }

                is ExperimentalSettingsUiState.Success -> {
                    Success(
                        modifier = modifier,
                        experimentalSettings = experimentalSettingsUiState.experimentalSettings,
                        onUpdateExperimentalSettings = onUpdateExperimentalSettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    experimentalSettings: ExperimentalSettings,
    onUpdateExperimentalSettings: (ExperimentalSettings) -> Unit,
) {
    var showSyncDataDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
    ) {
        SettingsColumn(
            title = "Sync Data",
            subtitle = "Enable or disable sync data",
            onClick = {
                showSyncDataDialog = true
            },
        )
    }

    if (showSyncDataDialog) {
        SyncDataDialog(
            syncData = experimentalSettings.syncData,
            onUpdateSyncData = { newSyncData ->
                onUpdateExperimentalSettings(experimentalSettings.copy(syncData = newSyncData))
            },
            onDismissRequest = {
                showSyncDataDialog = false
            },
        )
    }
}
