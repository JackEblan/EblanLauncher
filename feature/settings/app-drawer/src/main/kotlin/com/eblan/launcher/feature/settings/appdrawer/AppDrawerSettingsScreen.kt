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
package com.eblan.launcher.feature.settings.appdrawer

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
import androidx.compose.material3.MaterialTheme
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
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.settings.appdrawer.model.AppDrawerSettingsUiState
import com.eblan.launcher.ui.dialog.RadioOptionsDialog
import com.eblan.launcher.ui.dialog.SingleTextFieldDialog
import com.eblan.launcher.ui.dialog.TwoTextFieldsDialog
import com.eblan.launcher.ui.settings.SettingsColumn
import com.eblan.launcher.ui.settings.SettingsSwitch

@Composable
fun AppDrawerSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: AppDrawerSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val appDrawerSettingsUiState by viewModel.appDrawerSettingsUiState.collectAsStateWithLifecycle()

    AppDrawerSettingsScreen(
        modifier = modifier,
        appDrawerSettingsUiState = appDrawerSettingsUiState,
        onNavigateUp = onNavigateUp,
        onUpdateAppDrawerGrid = viewModel::updateAppDrawerGrid,
        onUpdateAppDrawerIconSize = viewModel::updateAppDrawerIconSize,
        onUpdateAppDrawerTextColor = viewModel::updateAppDrawerTextColor,
        onUpdateAppDrawerTextSize = viewModel::updateAppDrawerTextSize,
        onUpdateAppDrawerShowLabel = viewModel::updateAppDrawerShowLabel,
        onUpdateAppDrawerSingleLineLabel = viewModel::updateAppDrawerSingleLineLabel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerSettingsScreen(
    modifier: Modifier = Modifier,
    appDrawerSettingsUiState: AppDrawerSettingsUiState,
    onNavigateUp: () -> Unit,
    onUpdateAppDrawerGrid: (
        columns: Int,
        rowsHeight: Int,
    ) -> Unit,
    onUpdateAppDrawerIconSize: (Int) -> Unit,
    onUpdateAppDrawerTextColor: (TextColor) -> Unit,
    onUpdateAppDrawerTextSize: (Int) -> Unit,
    onUpdateAppDrawerShowLabel: (Boolean) -> Unit,
    onUpdateAppDrawerSingleLineLabel: (Boolean) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "App Drawer")
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
            when (appDrawerSettingsUiState) {
                AppDrawerSettingsUiState.Loading -> {
                }

                is AppDrawerSettingsUiState.Success -> {
                    Success(
                        appDrawerSettings = appDrawerSettingsUiState.appDrawerSettings,
                        onUpdateAppDrawerGrid = onUpdateAppDrawerGrid,
                        onUpdateAppDrawerIconSize = onUpdateAppDrawerIconSize,
                        onUpdateAppDrawerTextColor = onUpdateAppDrawerTextColor,
                        onUpdateAppDrawerTextSize = onUpdateAppDrawerTextSize,
                        onUpdateAppDrawerShowLabel = onUpdateAppDrawerShowLabel,
                        onUpdateAppDrawerSingleLineLabel = onUpdateAppDrawerSingleLineLabel,
                    )
                }
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    appDrawerSettings: AppDrawerSettings,
    onUpdateAppDrawerGrid: (
        columns: Int,
        rowsHeight: Int,
    ) -> Unit,
    onUpdateAppDrawerIconSize: (Int) -> Unit,
    onUpdateAppDrawerTextColor: (TextColor) -> Unit,
    onUpdateAppDrawerTextSize: (Int) -> Unit,
    onUpdateAppDrawerShowLabel: (Boolean) -> Unit,
    onUpdateAppDrawerSingleLineLabel: (Boolean) -> Unit,
) {
    var showGridDialog by remember { mutableStateOf(false) }

    var showIconSizeDialog by remember { mutableStateOf(false) }

    var showTextColorDialog by remember { mutableStateOf(false) }

    var showTextSizeDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsColumn(
            title = "App Drawer Grid",
            subtitle = "Number of columns and rows height",
            onClick = {
                showGridDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            modifier = Modifier.padding(5.dp),
            text = "Grid Item",
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(5.dp))

        SettingsColumn(
            title = "Icon Size",
            subtitle = "${appDrawerSettings.gridItemSettings.iconSize}",
            onClick = {
                showIconSizeDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Text Color",
            subtitle = appDrawerSettings.gridItemSettings.textColor.name,
            onClick = {
                showTextColorDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Text Size",
            subtitle = "${appDrawerSettings.gridItemSettings.textSize}",
            onClick = {
                showTextSizeDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsSwitch(
            modifier = modifier,
            checked = appDrawerSettings.gridItemSettings.showLabel,
            title = "Show Label",
            subtitle = "Show the label",
            onCheckedChange = onUpdateAppDrawerShowLabel,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsSwitch(
            modifier = modifier,
            checked = appDrawerSettings.gridItemSettings.singleLineLabel,
            title = "Single Line Label",
            subtitle = "Show single line label",
            onCheckedChange = onUpdateAppDrawerSingleLineLabel,
        )
    }

    if (showGridDialog) {
        var appDrawerColumns by remember { mutableStateOf("${appDrawerSettings.appDrawerColumns}") }

        var appDrawerRowsHeight by remember { mutableStateOf("${appDrawerSettings.appDrawerRowsHeight}") }

        TwoTextFieldsDialog(
            title = "App Drawer Grid",
            firstTextFieldTitle = "Columns",
            secondTextFieldTitle = "Rows Height",
            firstTextFieldValue = appDrawerColumns,
            secondTextFieldValue = appDrawerRowsHeight,
            keyboardType = KeyboardType.Number,
            onFirstValueChange = {
                appDrawerColumns = it
            },
            onSecondValueChange = {
                appDrawerRowsHeight = it
            },
            onDismissRequest = {
                showGridDialog = false
            },
            onUpdateClick = {
                try {
                    onUpdateAppDrawerGrid(appDrawerColumns.toInt(), appDrawerRowsHeight.toInt())
                } catch (e: NumberFormatException) {
                    TODO("Show Error")
                }

                showGridDialog = false
            },
        )
    }

    if (showIconSizeDialog) {
        var value by remember { mutableStateOf("${appDrawerSettings.gridItemSettings.iconSize}") }

        SingleTextFieldDialog(
            title = "Icon Size",
            textFieldTitle = "Icon Size",
            value = value,
            keyboardType = KeyboardType.Number,
            onValueChange = {
                value = it
            },
            onDismissRequest = {
                showIconSizeDialog = false
            },
            onUpdateClick = {
                try {
                    onUpdateAppDrawerIconSize(value.toInt())
                } catch (e: NumberFormatException) {
                    TODO("Show error")
                }

                showIconSizeDialog = false
            },
        )
    }

    if (showTextColorDialog) {
        RadioOptionsDialog(
            title = "Text Color",
            options = TextColor.entries,
            selected = appDrawerSettings.gridItemSettings.textColor,
            label = {
                it.name
            },
            onDismissRequest = {
                showTextColorDialog = false
            },
            onUpdateClick = {
                onUpdateAppDrawerTextColor(it)

                showTextColorDialog = false
            },
        )
    }

    if (showTextSizeDialog) {
        var value by remember { mutableStateOf("${appDrawerSettings.gridItemSettings.textSize}") }

        SingleTextFieldDialog(
            title = "Text Size",
            textFieldTitle = "Text Size",
            value = value,
            keyboardType = KeyboardType.Number,
            onValueChange = {
                value = it
            },
            onDismissRequest = {
                showTextSizeDialog = false
            },
            onUpdateClick = {
                try {
                    onUpdateAppDrawerTextSize(value.toInt())
                } catch (e: NumberFormatException) {
                    TODO("Show error")
                }

                showTextSizeDialog = false
            },
        )
    }
}
