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
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.edit.model.EditUiState
import com.eblan.launcher.ui.dialog.SingleTextFieldDialog
import com.eblan.launcher.ui.settings.GridItemSettings
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
    var showEditLabelDialog by remember { mutableStateOf(false) }

    val subtitle = when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> data.label.toString()
        is GridItemData.Folder -> data.label
        is GridItemData.ShortcutInfo -> data.shortLabel
        is GridItemData.Widget -> ""
    }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsColumn(
            title = "Edit Label",
            subtitle = subtitle,
            onClick = {
                showEditLabelDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsSwitch(
            checked = gridItem.override,
            title = "Override",
            subtitle = "Override the Grid Item Settings",
            onCheckedChange = {
                onUpdateGridItem(gridItem.copy(override = it))
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        GridItemSettings(
            gridItemSettings = gridItem.gridItemSettings,
            onUpdateGridItemSettings = { gridItemSettings ->
                onUpdateGridItem(gridItem.copy(gridItemSettings = gridItemSettings))
            },
        )
    }

    if (showEditLabelDialog) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                var value by remember { mutableStateOf(data.label.toString()) }

                var isError by remember { mutableStateOf(false)}

                SingleTextFieldDialog(
                    title = "Label",
                    textFieldTitle = "Label",
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
                            val newData = data.copy(label = value)

                            onUpdateGridItem(gridItem.copy(data = newData))

                            showEditLabelDialog = false
                        } else {
                            isError = true
                        }
                    },
                )
            }

            is GridItemData.Folder -> {
                var value by remember { mutableStateOf(data.label) }

                var isError by remember { mutableStateOf(false)}

                SingleTextFieldDialog(
                    title = "Label",
                    textFieldTitle = "Label",
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
                            val newData = data.copy(label = value)

                            onUpdateGridItem(gridItem.copy(data = newData))

                            showEditLabelDialog = false
                        } else {
                            isError = true
                        }
                    },
                )
            }

            is GridItemData.ShortcutInfo -> {
                var value by remember { mutableStateOf(data.shortLabel) }

                var isError by remember { mutableStateOf(false)}

                SingleTextFieldDialog(
                    title = "Label",
                    textFieldTitle = "Label",
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
                            val newData = data.copy(shortLabel = value)

                            onUpdateGridItem(gridItem.copy(data = newData))

                            showEditLabelDialog = false
                        } else {
                            isError = true
                        }
                    },
                )
            }

            else -> Unit
        }
    }
}
