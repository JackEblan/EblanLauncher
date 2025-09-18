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
import com.eblan.launcher.ui.settings.GridItemSettings
import com.eblan.launcher.ui.settings.SettingsColumn

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
        onUpdateAppDrawerSettings = viewModel::updateAppDrawerSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerSettingsScreen(
    modifier: Modifier = Modifier,
    appDrawerSettingsUiState: AppDrawerSettingsUiState,
    onNavigateUp: () -> Unit,
    onUpdateAppDrawerSettings: (AppDrawerSettings) -> Unit,
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
                        onUpdateAppDrawerSettings = onUpdateAppDrawerSettings,
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
    onUpdateAppDrawerSettings: (AppDrawerSettings) -> Unit,
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

        GridItemSettings(
            gridItemSettings = appDrawerSettings.gridItemSettings,
            onIconSizeClick = {
                showIconSizeDialog = true
            },
            onTextColorClick = {
                showTextColorDialog = true
            },
            onTextSizeClick = {
                showTextSizeDialog = true
            },
            onUpdateShowLabel = { showLabel ->
                val gridItemSettings =
                    appDrawerSettings.gridItemSettings.copy(showLabel = showLabel)

                onUpdateAppDrawerSettings(appDrawerSettings.copy(gridItemSettings = gridItemSettings))
            },
            onUpdateSingleLineLabel = { singleLineLabel ->
                val gridItemSettings =
                    appDrawerSettings.gridItemSettings.copy(singleLineLabel = singleLineLabel)

                onUpdateAppDrawerSettings(appDrawerSettings.copy(gridItemSettings = gridItemSettings))
            },
        )
    }

    if (showGridDialog) {
        var appDrawerColumns by remember { mutableStateOf("${appDrawerSettings.appDrawerColumns}") }

        var appDrawerRowsHeight by remember { mutableStateOf("${appDrawerSettings.appDrawerRowsHeight}") }

        var firstTextFieldIsError by remember { mutableStateOf(false) }

        var secondTextFieldIsError by remember { mutableStateOf(false) }

        TwoTextFieldsDialog(
            title = "App Drawer Grid",
            firstTextFieldTitle = "Columns",
            secondTextFieldTitle = "Rows Height",
            firstTextFieldValue = appDrawerColumns,
            secondTextFieldValue = appDrawerRowsHeight,
            firstTextFieldIsError = firstTextFieldIsError,
            secondTextFieldIsError = secondTextFieldIsError,
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
                val appDrawerColumns = try {
                    appDrawerColumns.toInt()
                } catch (_: NumberFormatException) {
                    firstTextFieldIsError = true
                    0
                }

                val appDrawerRowsHeight = try {
                    appDrawerRowsHeight.toInt()
                } catch (_: NumberFormatException) {
                    secondTextFieldIsError = true
                    0
                }

                if (appDrawerColumns > 0 && appDrawerRowsHeight > 0) {
                    onUpdateAppDrawerSettings(
                        appDrawerSettings.copy(
                            appDrawerColumns = appDrawerColumns,
                            appDrawerRowsHeight = appDrawerRowsHeight
                        )
                    )

                    showGridDialog = false
                }
            },
        )
    }

    if (showIconSizeDialog) {
        var value by remember { mutableStateOf("${appDrawerSettings.gridItemSettings.iconSize}") }

        var isError by remember { mutableStateOf(false) }

        SingleTextFieldDialog(
            title = "Icon Size",
            textFieldTitle = "Icon Size",
            value = value,
            isError = isError,
            keyboardType = KeyboardType.Number,
            onValueChange = {
                value = it
            },
            onDismissRequest = {
                showIconSizeDialog = false
            },
            onUpdateClick = {
                try {
                    val gridItemSettings =
                        appDrawerSettings.gridItemSettings.copy(iconSize = value.toInt())

                    onUpdateAppDrawerSettings(appDrawerSettings.copy(gridItemSettings = gridItemSettings))

                    showIconSizeDialog = false
                } catch (_: NumberFormatException) {
                    isError = true
                }
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
                val gridItemSettings =
                    appDrawerSettings.gridItemSettings.copy(textColor = it)

                onUpdateAppDrawerSettings(appDrawerSettings.copy(gridItemSettings = gridItemSettings))

                showTextColorDialog = false
            },
        )
    }

    if (showTextSizeDialog) {
        var value by remember { mutableStateOf("${appDrawerSettings.gridItemSettings.textSize}") }

        var isError by remember { mutableStateOf(false) }

        SingleTextFieldDialog(
            title = "Text Size",
            textFieldTitle = "Text Size",
            value = value,
            isError = isError,
            keyboardType = KeyboardType.Number,
            onValueChange = {
                value = it
            },
            onDismissRequest = {
                showTextSizeDialog = false
            },
            onUpdateClick = {
                try {
                    val gridItemSettings =
                        appDrawerSettings.gridItemSettings.copy(textSize = value.toInt())

                    onUpdateAppDrawerSettings(appDrawerSettings.copy(gridItemSettings = gridItemSettings))

                    showTextSizeDialog = false
                } catch (_: NumberFormatException) {
                    isError = true
                }
            },
        )
    }
}
