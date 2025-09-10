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
package com.eblan.launcher.feature.edit

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.edit.model.EditUiState
import com.eblan.launcher.ui.dialog.RadioOptionsDialog
import com.eblan.launcher.ui.dialog.SingleNumberTextFieldDialog
import com.eblan.launcher.ui.settings.SettingsColumn
import com.eblan.launcher.ui.settings.SettingsSwitch

@Composable
fun EditRoute(
    modifier: Modifier = Modifier,
    viewModel: EditViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val editUiState by viewModel.editUiState.collectAsStateWithLifecycle()

    EditScreen(
        modifier = modifier,
        editUiState = editUiState,
        onNavigateUp = onNavigateUp,
        onUpdateGridItem = viewModel::updateGridItem,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    editUiState: EditUiState,
    onNavigateUp: () -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Edit")
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
            when (editUiState) {
                EditUiState.Loading -> {
                }

                is EditUiState.Success -> {
                    if (editUiState.gridItem != null) {
                        Success(
                            modifier = modifier,
                            gridItem = editUiState.gridItem,
                            onUpdateGridItem = onUpdateGridItem,
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
    gridItem: GridItem,
    onUpdateGridItem: (GridItem) -> Unit,
) {
    var showIconSizeDialog by remember { mutableStateOf(false) }

    var showTextColorDialog by remember { mutableStateOf(false) }

    var showTextSizeDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsColumn(
            title = "Edit Label",
            subtitle = "Edit label",
            onClick = {
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            modifier = Modifier.padding(5.dp),
            text = "Grid Item",
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(5.dp))

        SettingsSwitch(
            checked = gridItem.override,
            title = "Override",
            subtitle = "Override the Grid Item Settings",
            onCheckedChange = {
                onUpdateGridItem(gridItem.copy(override = it))
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Icon Size",
            subtitle = "${gridItem.gridItemSettings.iconSize}",
            onClick = {
                showIconSizeDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Text Color",
            subtitle = gridItem.gridItemSettings.textColor.name,
            onClick = {
                showTextColorDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Text Size",
            subtitle = "${gridItem.gridItemSettings.textSize}",
            onClick = {
                showTextSizeDialog = true
            },
        )

        Spacer(modifier = Modifier.height(5.dp))

        SettingsSwitch(
            checked = gridItem.gridItemSettings.showLabel,
            title = "Show Label",
            subtitle = "Show label",
            onCheckedChange = {
                val newGridItemSettings = gridItem.gridItemSettings.copy(showLabel = it)

                onUpdateGridItem(gridItem.copy(gridItemSettings = newGridItemSettings))
            },
        )

        Spacer(modifier = Modifier.height(5.dp))

        SettingsSwitch(
            checked = gridItem.gridItemSettings.singleLineLabel,
            title = "Show Single Line Label",
            subtitle = "Show single line label",
            onCheckedChange = {
                val newGridItemSettings = gridItem.gridItemSettings.copy(singleLineLabel = it)

                onUpdateGridItem(gridItem.copy(gridItemSettings = newGridItemSettings))
            },
        )
    }

    if (showIconSizeDialog) {
        SingleNumberTextFieldDialog(
            title = "Icon Size",
            value = gridItem.gridItemSettings.iconSize,
            onDismissRequest = {
                showIconSizeDialog = false
            },
            onUpdateClick = { newIconSize ->
                val newGridItemSettings = gridItem.gridItemSettings.copy(iconSize = newIconSize)

                onUpdateGridItem(gridItem.copy(gridItemSettings = newGridItemSettings))
            },
        )
    }

    if (showTextColorDialog) {
        RadioOptionsDialog(
            title = "Text Color",
            options = listOf(
                TextColor.System,
                TextColor.Light,
                TextColor.Dark,
            ),
            selected = gridItem.gridItemSettings.textColor,
            label = {
                it.name
            },
            onDismissRequest = {
                showTextColorDialog = false
            },
            onUpdateClick = { newTextColor ->
                val newGridItemSettings = gridItem.gridItemSettings.copy(textColor = newTextColor)

                onUpdateGridItem(gridItem.copy(gridItemSettings = newGridItemSettings))
            },
        )
    }

    if (showTextSizeDialog) {
        SingleNumberTextFieldDialog(
            title = "Text Size",
            value = gridItem.gridItemSettings.textSize,
            onDismissRequest = {
                showTextSizeDialog = false
            },
            onUpdateClick = { newTextSize ->
                val newGridItemSettings = gridItem.gridItemSettings.copy(textSize = newTextSize)

                onUpdateGridItem(gridItem.copy(gridItemSettings = newGridItemSettings))
            },
        )
    }
}
