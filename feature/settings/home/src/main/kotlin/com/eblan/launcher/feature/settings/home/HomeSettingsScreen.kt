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
package com.eblan.launcher.feature.settings.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.feature.settings.home.model.HomeSettingsUiState
import com.eblan.launcher.ui.dialog.SingleTextFieldDialog
import com.eblan.launcher.ui.dialog.TwoTextFieldsDialog
import com.eblan.launcher.ui.settings.GridItemSettings
import com.eblan.launcher.ui.settings.SettingsColumn
import com.eblan.launcher.ui.settings.SettingsSwitch

@Composable
internal fun HomeSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val homeSettingsUiState by viewModel.homeSettingsUiState.collectAsStateWithLifecycle()

    HomeSettingsScreen(
        modifier = modifier,
        homeSettingsUiState = homeSettingsUiState,
        onNavigateUp = onNavigateUp,
        onUpdateHomeSettings = viewModel::updateHomeSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeSettingsScreen(
    modifier: Modifier = Modifier,
    homeSettingsUiState: HomeSettingsUiState,
    onNavigateUp: () -> Unit,
    onUpdateHomeSettings: (HomeSettings) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Home")
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
            when (homeSettingsUiState) {
                HomeSettingsUiState.Loading -> {
                }

                is HomeSettingsUiState.Success -> {
                    Success(
                        homeSettings = homeSettingsUiState.homeSettings,
                        onUpdateHomeSettings = onUpdateHomeSettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    homeSettings: HomeSettings,
    onUpdateHomeSettings: (HomeSettings) -> Unit,
) {
    var showGridDialog by remember { mutableStateOf(false) }

    var showDockGridDialog by remember { mutableStateOf(false) }

    var showDockHeightDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
    ) {
        SettingsColumn(
            title = "Grid",
            subtitle = "Number of columns and rows",
            onClick = {
                showGridDialog = true
            },
        )

        SettingsSwitch(
            checked = homeSettings.infiniteScroll,
            title = "Infinite Scrolling",
            subtitle = "Seamless loop from last page back to first",
            onCheckedChange = { infiniteScroll ->
                onUpdateHomeSettings(homeSettings.copy(infiniteScroll = infiniteScroll))
            },
        )

        SettingsSwitch(
            checked = homeSettings.wallpaperScroll,
            title = "Wallpaper Scrolling",
            subtitle = "Scroll wallpaper across pages",
            onCheckedChange = { wallpaperScroll ->
                onUpdateHomeSettings(homeSettings.copy(wallpaperScroll = wallpaperScroll))
            },
        )

        Text(
            modifier = Modifier.padding(10.dp),
            text = "Dock",
            style = MaterialTheme.typography.bodySmall,
        )

        SettingsColumn(
            title = "Dock Grid",
            subtitle = "Number of columns and rows",
            onClick = {
                showDockGridDialog = true
            },
        )

        SettingsColumn(
            title = "Dock Height",
            subtitle = "Height of the dock",
            onClick = {
                showDockHeightDialog = true
            },
        )

        GridItemSettings(
            gridItemSettings = homeSettings.gridItemSettings,
            onUpdateGridItemSettings = { gridItemSettings ->
                onUpdateHomeSettings(homeSettings.copy(gridItemSettings = gridItemSettings))
            },
        )
    }

    if (showGridDialog) {
        var columns by remember { mutableStateOf("${homeSettings.columns}") }

        var rows by remember { mutableStateOf("${homeSettings.rows}") }

        var firstTextFieldIsError by remember { mutableStateOf(false) }

        var secondTextFieldIsError by remember { mutableStateOf(false) }

        TwoTextFieldsDialog(
            title = "Grid",
            firstTextFieldTitle = "Columns",
            secondTextFieldTitle = "Rows",
            firstTextFieldValue = columns,
            secondTextFieldValue = rows,
            firstTextFieldIsError = firstTextFieldIsError,
            secondTextFieldIsError = secondTextFieldIsError,
            keyboardType = KeyboardType.Number,
            onFirstValueChange = {
                columns = it
            },
            onSecondValueChange = {
                rows = it
            },
            onDismissRequest = {
                showGridDialog = false
            },
            onUpdateClick = {
                val columns = try {
                    columns.toInt()
                } catch (_: NumberFormatException) {
                    firstTextFieldIsError = true
                    0
                }

                val rows = try {
                    rows.toInt()
                } catch (_: NumberFormatException) {
                    secondTextFieldIsError = true
                    0
                }

                if (columns > 0 && rows > 0) {
                    onUpdateHomeSettings(
                        homeSettings.copy(
                            columns = columns,
                            rows = rows,
                        ),
                    )

                    showGridDialog = false
                }
            },
        )
    }

    if (showDockGridDialog) {
        var dockColumns by remember { mutableStateOf("${homeSettings.dockColumns}") }

        var dockRows by remember { mutableStateOf("${homeSettings.dockRows}") }

        var firstTextFieldIsError by remember { mutableStateOf(false) }

        var secondTextFieldIsError by remember { mutableStateOf(false) }

        TwoTextFieldsDialog(
            title = "Dock Grid",
            firstTextFieldTitle = "Columns",
            secondTextFieldTitle = "Rows",
            firstTextFieldValue = dockColumns,
            secondTextFieldValue = dockRows,
            firstTextFieldIsError = firstTextFieldIsError,
            secondTextFieldIsError = secondTextFieldIsError,
            keyboardType = KeyboardType.Number,
            onFirstValueChange = {
                dockColumns = it
            },
            onSecondValueChange = {
                dockRows = it
            },
            onDismissRequest = {
                showDockGridDialog = false
            },
            onUpdateClick = {
                val dockColumns = try {
                    dockColumns.toInt()
                } catch (_: NumberFormatException) {
                    firstTextFieldIsError = true
                    0
                }

                val dockRows = try {
                    dockRows.toInt()
                } catch (_: NumberFormatException) {
                    secondTextFieldIsError = true
                    0
                }

                if (dockColumns > 0 && dockRows > 0) {
                    onUpdateHomeSettings(
                        homeSettings.copy(
                            dockColumns = dockColumns,
                            dockRows = dockRows,
                        ),
                    )

                    showDockGridDialog = false
                }
            },
        )
    }

    if (showDockHeightDialog) {
        var value by remember { mutableStateOf("${homeSettings.dockHeight}") }

        var isError by remember { mutableStateOf(false) }

        SingleTextFieldDialog(
            title = "Dock Height",
            textFieldTitle = "Dock Height",
            value = value,
            isError = isError,
            keyboardType = KeyboardType.Number,
            onValueChange = {
                value = it
            },
            onDismissRequest = {
                showDockHeightDialog = false
            },
            onUpdateClick = {
                try {
                    onUpdateHomeSettings(
                        homeSettings.copy(
                            dockHeight = value.toInt(),
                        ),
                    )

                    showDockHeightDialog = false
                } catch (_: NumberFormatException) {
                    isError = true
                }
            },
        )
    }
}
