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
package com.eblan.launcher.feature.editapplicationinfo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.feature.editapplicationinfo.model.EditApplicationInfoUiState
import com.eblan.launcher.ui.dialog.SingleTextFieldDialog
import com.eblan.launcher.ui.settings.GridItemSettings
import com.eblan.launcher.ui.settings.SettingsColumn
import com.eblan.launcher.ui.settings.SettingsSwitch

@Composable
fun EditApplicationInfoRoute(
    modifier: Modifier = Modifier,
    viewModel: EditApplicationInfoViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val editApplicationInfoUiState by viewModel.editApplicationInfoUiState.collectAsStateWithLifecycle()

    EditApplicationInfoScreen(
        modifier = modifier,
        editApplicationInfoUiState = editApplicationInfoUiState,
        onNavigateUp = onNavigateUp,
        onUpdateEblanApplicationInfo = viewModel::updateEblanApplicationInfo,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditApplicationInfoScreen(
    modifier: Modifier = Modifier,
    editApplicationInfoUiState: EditApplicationInfoUiState,
    onNavigateUp: () -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Edit Application Info")
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
            when (editApplicationInfoUiState) {
                EditApplicationInfoUiState.Loading -> {
                }

                is EditApplicationInfoUiState.Success -> {
                    if (editApplicationInfoUiState.eblanApplicationInfo != null) {
                        Success(
                            modifier = modifier,
                            eblanApplicationInfo = editApplicationInfoUiState.eblanApplicationInfo,
                            onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    eblanApplicationInfo: EblanApplicationInfo,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
    ) {
        EditApplicationInfo(
            eblanApplicationInfo = eblanApplicationInfo,
            onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsSwitch(
            checked = eblanApplicationInfo.override,
            title = "Override",
            subtitle = "Override the Grid Item Settings",
            onCheckedChange = {
                onUpdateEblanApplicationInfo(eblanApplicationInfo.copy(override = it))
            },
        )

        if (eblanApplicationInfo.override) {
            Spacer(modifier = Modifier.height(10.dp))

            GridItemSettings(
                gridItemSettings = eblanApplicationInfo.gridItemSettings,
                onUpdateGridItemSettings = { gridItemSettings ->
                    onUpdateEblanApplicationInfo(eblanApplicationInfo.copy(gridItemSettings = gridItemSettings))
                },
            )
        }
    }
}

@Composable
private fun EditApplicationInfo(
    eblanApplicationInfo: EblanApplicationInfo,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
) {
    var showEditLabelDialog by remember { mutableStateOf(false) }

    SettingsColumn(
        title = "Edit Custom Label",
        subtitle = eblanApplicationInfo.customLabel.toString(),
        onClick = {
            showEditLabelDialog = true
        },
    )

    if (showEditLabelDialog) {
        var value by remember { mutableStateOf(eblanApplicationInfo.label.toString()) }

        var isError by remember { mutableStateOf(false) }

        SingleTextFieldDialog(
            title = "Custom Label",
            textFieldTitle = "Custom Label",
            value = value,
            isError = isError,
            keyboardType = KeyboardType.Text,
            onValueChange = {
                value = it
            },
            onDismissRequest = {
                showEditLabelDialog = false
            },
            onUpdateClick = {
                if (value.isNotBlank()) {
                    onUpdateEblanApplicationInfo(eblanApplicationInfo.copy(customLabel = value))

                    showEditLabelDialog = false
                } else {
                    isError = true
                }
            },
        )
    }
}
