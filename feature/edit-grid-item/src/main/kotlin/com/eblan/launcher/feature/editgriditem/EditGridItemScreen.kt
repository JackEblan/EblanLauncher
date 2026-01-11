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
package com.eblan.launcher.feature.editgriditem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.IconPackInfoComponent
import com.eblan.launcher.domain.model.PackageManagerIconPackInfo
import com.eblan.launcher.feature.editgriditem.model.EditGridItemUiState
import com.eblan.launcher.ui.dialog.IconPackInfoFilesDialog
import com.eblan.launcher.ui.dialog.SingleTextFieldDialog
import com.eblan.launcher.ui.edit.CustomIcon
import com.eblan.launcher.ui.settings.GridItemSettings
import com.eblan.launcher.ui.settings.SettingsColumn
import com.eblan.launcher.ui.settings.SettingsSwitch

@Composable
internal fun EditGridItemRoute(
    modifier: Modifier = Modifier,
    viewModel: EditGridItemViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val editUiState by viewModel.editGridItemUiState.collectAsStateWithLifecycle()

    val packageManagerIconPackInfos by viewModel.packageManagerIconPackInfos.collectAsStateWithLifecycle()

    val iconPackInfoComponents by viewModel.iconPackInfoComponents.collectAsStateWithLifecycle()

    EditGridItemScreen(
        modifier = modifier,
        editGridItemUiState = editUiState,
        packageManagerIconPackInfos = packageManagerIconPackInfos,
        iconPackInfoComponents = iconPackInfoComponents,
        onNavigateUp = onNavigateUp,
        onUpdateGridItem = viewModel::updateGridItem,
        onUpdateIconPackInfoPackageName = viewModel::updateIconPackInfoPackageName,
        onResetIconPackInfoPackageName = viewModel::resetIconPackInfoPackageName,
        onUpdateGridItemCustomIcon = viewModel::updateGridItemCustomIcon,
        onRestoreGridItem = viewModel::restoreGridItem,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditGridItemScreen(
    modifier: Modifier = Modifier,
    editGridItemUiState: EditGridItemUiState,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    onNavigateUp: () -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onUpdateGridItemCustomIcon: (
        byteArray: ByteArray,
        gridItem: GridItem,
    ) -> Unit,
    onRestoreGridItem: (GridItem) -> Unit,
) {
    if (editGridItemUiState is EditGridItemUiState.Success &&
        editGridItemUiState.gridItem != null
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Edit Grid Item")
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                imageVector = EblanLauncherIcons.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            when (editGridItemUiState.gridItem.data) {
                                is GridItemData.ApplicationInfo,
                                is GridItemData.ShortcutConfig,
                                is GridItemData.ShortcutInfo,
                                    -> {
                                    onRestoreGridItem(editGridItemUiState.gridItem)
                                }

                                else -> Unit
                            }
                        }) {
                            Icon(
                                imageVector = EblanLauncherIcons.Restore,
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
                Success(
                    modifier = modifier,
                    gridItem = editGridItemUiState.gridItem,
                    packageManagerIconPackInfos = packageManagerIconPackInfos,
                    iconPackInfoComponents = iconPackInfoComponents,
                    onUpdateGridItem = onUpdateGridItem,
                    onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                    onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                    onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                )
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onUpdateGridItemCustomIcon: (
        byteArray: ByteArray,
        gridItem: GridItem,
    ) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
        ) {
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    EditApplicationInfo(
                        gridItem = gridItem,
                        data = data,
                        packageManagerIconPackInfos = packageManagerIconPackInfos,
                        iconPackInfoComponents = iconPackInfoComponents,
                        onUpdateGridItem = onUpdateGridItem,
                        onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                        onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                        onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                    )
                }

                is GridItemData.Folder -> {
                    EditFolder(
                        gridItem = gridItem,
                        data = data,
                        packageManagerIconPackInfos = packageManagerIconPackInfos,
                        iconPackInfoComponents = iconPackInfoComponents,
                        onUpdateGridItem = onUpdateGridItem,
                        onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                        onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                        onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    EditShortcutInfo(
                        gridItem = gridItem,
                        data = data,
                        packageManagerIconPackInfos = packageManagerIconPackInfos,
                        iconPackInfoComponents = iconPackInfoComponents,
                        onUpdateGridItem = onUpdateGridItem,
                        onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                        onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                        onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                    )
                }

                is GridItemData.ShortcutConfig -> {
                    EditShortcutConfig(
                        gridItem = gridItem,
                        data = data,
                        packageManagerIconPackInfos = packageManagerIconPackInfos,
                        iconPackInfoComponents = iconPackInfoComponents,
                        onUpdateGridItem = onUpdateGridItem,
                        onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                        onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                        onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                    )
                }

                else -> Unit
            }

            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            SettingsSwitch(
                checked = gridItem.override,
                title = "Override",
                subtitle = "Override the Grid Item Settings",
                onCheckedChange = {
                    onUpdateGridItem(gridItem.copy(override = it))
                },
            )
        }

        if (gridItem.override) {
            GridItemSettings(
                gridItemSettings = gridItem.gridItemSettings,
                onUpdateGridItemSettings = { gridItemSettings ->
                    onUpdateGridItem(gridItem.copy(gridItemSettings = gridItemSettings))
                },
            )
        }
    }
}

@Composable
private fun EditApplicationInfo(
    gridItem: GridItem,
    data: GridItemData.ApplicationInfo,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onUpdateGridItemCustomIcon: (
        byteArray: ByteArray,
        gridItem: GridItem,
    ) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }

    var showCustomLabelDialog by remember { mutableStateOf(false) }

    var iconPackInfoPackageName by remember { mutableStateOf<String?>(null) }

    var iconPackInfoLabel by remember { mutableStateOf<String?>(null) }

    CustomIcon(
        customIcon = data.customIcon,
        packageManagerIconPackInfos = packageManagerIconPackInfos,
        onUpdateIconPackInfoPackageName = { packageName, label ->
            iconPackInfoPackageName = packageName

            iconPackInfoLabel = label

            showCustomIconDialog = true

            onUpdateIconPackInfoPackageName(packageName)
        },
        onUpdateUri = { uri ->
            val newData = data.copy(customIcon = uri)

            onUpdateGridItem(gridItem.copy(data = newData))
        },
    )

    HorizontalDivider(modifier = Modifier.fillMaxWidth())

    SettingsColumn(
        title = "Custom Label",
        subtitle = data.customLabel ?: "None",
        onClick = {
            showCustomLabelDialog = true
        },
    )

    if (showCustomIconDialog) {
        IconPackInfoFilesDialog(
            iconPackInfoComponents = iconPackInfoComponents,
            iconPackInfoPackageName = iconPackInfoPackageName,
            iconPackInfoLabel = iconPackInfoLabel,
            onDismissRequest = {
                onResetIconPackInfoPackageName()

                showCustomIconDialog = false
            },
            onUpdateByteArray = { byteArray ->
                onUpdateGridItemCustomIcon(
                    byteArray,
                    gridItem,
                )
            },
        )
    }

    if (showCustomLabelDialog) {
        var value by remember { mutableStateOf(data.customLabel ?: "") }

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
                showCustomLabelDialog = false
            },
            onUpdateClick = {
                if (value.isNotBlank()) {
                    val newData = data.copy(customLabel = value)

                    onUpdateGridItem(gridItem.copy(data = newData))

                    showCustomLabelDialog = false
                } else {
                    isError = true
                }
            },
        )
    }
}

@Composable
private fun EditFolder(
    gridItem: GridItem,
    data: GridItemData.Folder,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onUpdateGridItemCustomIcon: (
        byteArray: ByteArray,
        gridItem: GridItem,
    ) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }

    var showEditLabelDialog by remember { mutableStateOf(false) }

    var showEditPageCountDialog by remember { mutableStateOf(false) }

    var iconPackInfoPackageName by remember { mutableStateOf<String?>(null) }

    var iconPackInfoLabel by remember { mutableStateOf<String?>(null) }

    CustomIcon(
        customIcon = data.icon,
        packageManagerIconPackInfos = packageManagerIconPackInfos,
        onUpdateIconPackInfoPackageName = { packageName, label ->
            iconPackInfoPackageName = packageName

            iconPackInfoLabel = label

            showCustomIconDialog = true

            onUpdateIconPackInfoPackageName(packageName)
        },
        onUpdateUri = { uri ->
            val newData = data.copy(icon = uri)

            onUpdateGridItem(gridItem.copy(data = newData))
        },
    )

    HorizontalDivider(modifier = Modifier.fillMaxWidth())

    SettingsColumn(
        title = "Edit Label",
        subtitle = data.label,
        onClick = {
            showEditLabelDialog = true
        },
    )

    HorizontalDivider(modifier = Modifier.fillMaxWidth())

    SettingsColumn(
        title = "Edit Page Count",
        subtitle = data.pageCount.toString(),
        onClick = {
            showEditPageCountDialog = true
        },
    )

    if (showCustomIconDialog) {
        IconPackInfoFilesDialog(
            iconPackInfoComponents = iconPackInfoComponents,
            iconPackInfoPackageName = iconPackInfoPackageName,
            iconPackInfoLabel = iconPackInfoLabel,
            onDismissRequest = {
                onResetIconPackInfoPackageName()

                showCustomIconDialog = false
            },
            onUpdateByteArray = { byteArray ->
                onUpdateGridItemCustomIcon(
                    byteArray,
                    gridItem,
                )
            },
        )
    }

    if (showEditLabelDialog) {
        var value by remember { mutableStateOf(data.label) }

        var isError by remember { mutableStateOf(false) }

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

    if (showEditPageCountDialog) {
        var value by remember { mutableStateOf("${data.pageCount}") }

        var isError by remember { mutableStateOf(false) }

        SingleTextFieldDialog(
            title = "Page Count",
            textFieldTitle = "Page Count",
            value = value,
            isError = isError,
            keyboardType = KeyboardType.Number,
            onValueChange = {
                value = it
            },
            onDismissRequest = {
                showEditPageCountDialog = false
            },
            onUpdateClick = {
                if (value.isNotBlank()) {
                    try {
                        val newData = data.copy(pageCount = value.toInt())

                        onUpdateGridItem(gridItem.copy(data = newData))

                        showEditPageCountDialog = false
                    } catch (_: NumberFormatException) {
                        isError = true
                    }
                } else {
                    isError = true
                }
            },
        )
    }
}

@Composable
private fun EditShortcutInfo(
    gridItem: GridItem,
    data: GridItemData.ShortcutInfo,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onUpdateGridItemCustomIcon: (
        byteArray: ByteArray,
        gridItem: GridItem,
    ) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }

    var showCustomShortLabelDialog by remember { mutableStateOf(false) }

    var iconPackInfoPackageName by remember { mutableStateOf<String?>(null) }

    var iconPackInfoLabel by remember { mutableStateOf<String?>(null) }

    CustomIcon(
        customIcon = data.customIcon,
        packageManagerIconPackInfos = packageManagerIconPackInfos,
        onUpdateIconPackInfoPackageName = { packageName, label ->
            iconPackInfoPackageName = packageName

            iconPackInfoLabel = label

            showCustomIconDialog = true

            onUpdateIconPackInfoPackageName(packageName)
        },
        onUpdateUri = { uri ->
            val newData = data.copy(customIcon = uri)

            onUpdateGridItem(gridItem.copy(data = newData))
        },
    )

    HorizontalDivider(modifier = Modifier.fillMaxWidth())

    SettingsColumn(
        title = "Custom Short Label",
        subtitle = data.customShortLabel ?: "None",
        onClick = {
            showCustomShortLabelDialog = true
        },
    )

    if (showCustomIconDialog) {
        IconPackInfoFilesDialog(
            iconPackInfoComponents = iconPackInfoComponents,
            iconPackInfoPackageName = iconPackInfoPackageName,
            iconPackInfoLabel = iconPackInfoLabel,
            onDismissRequest = {
                onResetIconPackInfoPackageName()

                showCustomIconDialog = false
            },
            onUpdateByteArray = { byteArray ->
                onUpdateGridItemCustomIcon(
                    byteArray,
                    gridItem,
                )
            },
        )
    }

    if (showCustomShortLabelDialog) {
        var value by remember { mutableStateOf(data.customShortLabel ?: "") }

        var isError by remember { mutableStateOf(false) }

        SingleTextFieldDialog(
            title = "Custom Short Label",
            textFieldTitle = "Custom Short Label",
            value = value,
            isError = isError,
            keyboardType = KeyboardType.Text,
            onValueChange = {
                value = it
            },
            onDismissRequest = {
                showCustomShortLabelDialog = false
            },
            onUpdateClick = {
                if (value.isNotBlank()) {
                    val newData = data.copy(customShortLabel = value)

                    onUpdateGridItem(gridItem.copy(data = newData))

                    showCustomShortLabelDialog = false
                } else {
                    isError = true
                }
            },
        )
    }
}

@Composable
private fun EditShortcutConfig(
    gridItem: GridItem,
    data: GridItemData.ShortcutConfig,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onUpdateGridItemCustomIcon: (
        byteArray: ByteArray,
        gridItem: GridItem,
    ) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }

    var showCustomLabelDialog by remember { mutableStateOf(false) }

    var iconPackInfoPackageName by remember { mutableStateOf<String?>(null) }

    var iconPackInfoLabel by remember { mutableStateOf<String?>(null) }

    CustomIcon(
        customIcon = data.customIcon,
        packageManagerIconPackInfos = packageManagerIconPackInfos,
        onUpdateIconPackInfoPackageName = { packageName, label ->
            iconPackInfoPackageName = packageName

            iconPackInfoLabel = label

            showCustomIconDialog = true

            onUpdateIconPackInfoPackageName(packageName)
        },
        onUpdateUri = { uri ->
            val newData = data.copy(customIcon = uri)

            onUpdateGridItem(gridItem.copy(data = newData))
        },
    )

    HorizontalDivider(modifier = Modifier.fillMaxWidth())

    SettingsColumn(
        title = "Custom Label",
        subtitle = data.customLabel ?: "None",
        onClick = {
            showCustomLabelDialog = true
        },
    )

    if (showCustomIconDialog) {
        IconPackInfoFilesDialog(
            iconPackInfoComponents = iconPackInfoComponents,
            iconPackInfoPackageName = iconPackInfoPackageName,
            iconPackInfoLabel = iconPackInfoLabel,
            onDismissRequest = {
                onResetIconPackInfoPackageName()

                showCustomIconDialog = false
            },
            onUpdateByteArray = { byteArray ->
                onUpdateGridItemCustomIcon(
                    byteArray,
                    gridItem,
                )
            },
        )
    }

    if (showCustomLabelDialog) {
        var value by remember { mutableStateOf(data.customLabel ?: "") }

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
                showCustomLabelDialog = false
            },
            onUpdateClick = {
                if (value.isNotBlank()) {
                    val newData = data.copy(customLabel = value)

                    onUpdateGridItem(gridItem.copy(data = newData))

                    showCustomLabelDialog = false
                } else {
                    isError = true
                }
            },
        )
    }
}
