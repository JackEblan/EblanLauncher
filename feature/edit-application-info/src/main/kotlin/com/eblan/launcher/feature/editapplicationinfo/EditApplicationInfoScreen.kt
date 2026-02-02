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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.EblanApplicationInfoTagUi
import com.eblan.launcher.domain.model.IconPackInfoComponent
import com.eblan.launcher.domain.model.PackageManagerIconPackInfo
import com.eblan.launcher.feature.editapplicationinfo.model.EditApplicationInfoUiState
import com.eblan.launcher.ui.dialog.IconPackInfoFilesDialog
import com.eblan.launcher.ui.dialog.SingleTextFieldDialog
import com.eblan.launcher.ui.edit.CustomIcon
import com.eblan.launcher.ui.settings.SettingsColumn
import com.eblan.launcher.ui.settings.SettingsSwitch

@Composable
internal fun EditApplicationInfoRoute(
    modifier: Modifier = Modifier,
    viewModel: EditApplicationInfoViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val editApplicationInfoUiState by viewModel.editApplicationInfoUiState.collectAsStateWithLifecycle()

    val packageManagerIconPackInfos by viewModel.packageManagerIconPackInfos.collectAsStateWithLifecycle()

    val iconPackInfoComponents by viewModel.iconPackInfoComponents.collectAsStateWithLifecycle()

    val eblanApplicationInfoTagsUi by viewModel.eblanApplicationInfoTagsUi.collectAsStateWithLifecycle()

    EditApplicationInfoScreen(
        modifier = modifier,
        editApplicationInfoUiState = editApplicationInfoUiState,
        onNavigateUp = onNavigateUp,
        packageManagerIconPackInfos = packageManagerIconPackInfos,
        iconPackInfoComponents = iconPackInfoComponents,
        eblanApplicationInfoTagsUi = eblanApplicationInfoTagsUi,
        onUpdateEblanApplicationInfo = viewModel::updateEblanApplicationInfo,
        onUpdateIconPackInfoPackageName = viewModel::updateIconPackInfoPackageName,
        onRestoreEblanApplicationInfo = viewModel::restoreEblanApplicationInfo,
        onResetIconPackInfoPackageName = viewModel::resetIconPackInfoPackageName,
        onUpdateGridItemCustomIcon = viewModel::updateEblanApplicationInfoCustomIcon,
        onSearchIconPackInfoComponent = viewModel::searchIconPackInfoComponent,
        onAddEblanApplicationInfoTag = viewModel::addEblanApplicationInfoTag,
        onAddEblanApplicationInfoCrossRef = viewModel::addEblanApplicationInfoTagCrossRef,
        onDeleteEblanApplicationInfoCrossRef = viewModel::deleteEblanApplicationInfoTagCrossRef,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditApplicationInfoScreen(
    modifier: Modifier = Modifier,
    editApplicationInfoUiState: EditApplicationInfoUiState,
    onNavigateUp: () -> Unit,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    eblanApplicationInfoTagsUi: List<EblanApplicationInfoTagUi>,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
    onRestoreEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onUpdateGridItemCustomIcon: (
        customIcon: String?,
        eblanApplicationInfo: EblanApplicationInfo,
    ) -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onAddEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onAddEblanApplicationInfoCrossRef: (Long) -> Unit,
    onDeleteEblanApplicationInfoCrossRef: (Long) -> Unit,
) {
    if (editApplicationInfoUiState is EditApplicationInfoUiState.Success && editApplicationInfoUiState.eblanApplicationInfo != null) {
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
                    actions = {
                        IconButton(
                            onClick = {
                                onRestoreEblanApplicationInfo(editApplicationInfoUiState.eblanApplicationInfo)
                            },
                        ) {
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
                    eblanApplicationInfo = editApplicationInfoUiState.eblanApplicationInfo,
                    packageManagerIconPackInfos = packageManagerIconPackInfos,
                    iconPackInfoComponents = iconPackInfoComponents,
                    eblanApplicationInfoTagsUi = eblanApplicationInfoTagsUi,
                    onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
                    onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                    onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                    onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                    onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
                    onAddEblanApplicationInfoTag = onAddEblanApplicationInfoTag,
                    onAddEblanApplicationInfoCrossRef = onAddEblanApplicationInfoCrossRef,
                    onDeleteEblanApplicationInfoCrossRef = onDeleteEblanApplicationInfoCrossRef,
                )
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    eblanApplicationInfo: EblanApplicationInfo,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    eblanApplicationInfoTagsUi: List<EblanApplicationInfoTagUi>,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onUpdateGridItemCustomIcon: (
        customIcon: String?,
        eblanApplicationInfo: EblanApplicationInfo,
    ) -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onAddEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onAddEblanApplicationInfoCrossRef: (Long) -> Unit,
    onDeleteEblanApplicationInfoCrossRef: (Long) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }

    var showCustomLabelDialog by remember { mutableStateOf(false) }

    var iconPackInfoPackageName by remember { mutableStateOf<String?>(null) }

    var iconPackInfoLabel by remember { mutableStateOf<String?>(null) }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
    ) {
        Tags(
            eblanApplicationInfoTagsUi = eblanApplicationInfoTagsUi,
            onAddEblanApplicationInfoTag = onAddEblanApplicationInfoTag,
            onAddEblanApplicationInfoCrossRef = onAddEblanApplicationInfoCrossRef,
            onDeleteEblanApplicationInfoCrossRef = onDeleteEblanApplicationInfoCrossRef,
        )

        CustomIcon(
            customIcon = eblanApplicationInfo.customIcon,
            packageManagerIconPackInfos = packageManagerIconPackInfos,
            onUpdateIconPackInfoPackageName = { packageName, label ->
                iconPackInfoPackageName = packageName

                iconPackInfoLabel = label

                showCustomIconDialog = true

                onUpdateIconPackInfoPackageName(packageName)
            },
            onUpdateUri = { uri ->
                onUpdateEblanApplicationInfo(eblanApplicationInfo.copy(customIcon = uri))
            },
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        SettingsColumn(
            title = "Custom Label",
            subtitle = eblanApplicationInfo.customLabel ?: "None",
            onClick = {
                showCustomLabelDialog = true
            },
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        SettingsSwitch(
            checked = eblanApplicationInfo.isHidden,
            title = "Hide From Drawer",
            subtitle = "Hide from drawer",
            onCheckedChange = { isHidden ->
                onUpdateEblanApplicationInfo(eblanApplicationInfo.copy(isHidden = isHidden))
            },
        )

        if (showCustomIconDialog) {
            IconPackInfoFilesDialog(
                iconPackInfoComponents = iconPackInfoComponents,
                iconPackInfoPackageName = iconPackInfoPackageName,
                iconPackInfoLabel = iconPackInfoLabel,
                iconName = eblanApplicationInfo.packageName,
                onDismissRequest = {
                    onResetIconPackInfoPackageName()

                    showCustomIconDialog = false
                },
                onUpdateIcon = { icon ->
                    onUpdateGridItemCustomIcon(
                        icon,
                        eblanApplicationInfo,
                    )
                },
                onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
            )
        }

        if (showCustomLabelDialog) {
            var value by remember { mutableStateOf(eblanApplicationInfo.customLabel ?: "") }

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
                        onUpdateEblanApplicationInfo(eblanApplicationInfo.copy(customLabel = value))

                        showCustomLabelDialog = false
                    } else {
                        isError = true
                    }
                },
            )
        }
    }
}

@Composable
private fun Tags(
    modifier: Modifier = Modifier,
    eblanApplicationInfoTagsUi: List<EblanApplicationInfoTagUi>,
    onAddEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onAddEblanApplicationInfoCrossRef: (Long) -> Unit,
    onDeleteEblanApplicationInfoCrossRef: (Long) -> Unit,
) {
    var showAddTagDialog by remember { mutableStateOf(false) }

    FlowRow(modifier = modifier.fillMaxWidth()) {
        eblanApplicationInfoTagsUi.forEach { eblanApplicationInfoTagUi ->
            FilterChip(
                modifier = Modifier.padding(5.dp),
                onClick = {
                    if (eblanApplicationInfoTagUi.selected) {
                        onDeleteEblanApplicationInfoCrossRef(eblanApplicationInfoTagUi.id)
                    } else {
                        onAddEblanApplicationInfoCrossRef(eblanApplicationInfoTagUi.id)
                    }
                },
                label = {
                    Text(text = eblanApplicationInfoTagUi.name)
                },
                selected = eblanApplicationInfoTagUi.selected,
                leadingIcon = if (eblanApplicationInfoTagUi.selected) {
                    {
                        Icon(
                            imageVector = EblanLauncherIcons.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
            )
        }

        AssistChip(
            modifier = Modifier.padding(5.dp),
            onClick = {
                showAddTagDialog = true
            },
            label = { Text("Add Tag") },
            leadingIcon = {
                Icon(
                    imageVector = EblanLauncherIcons.Add,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
            },
        )
    }

    if (showAddTagDialog) {
        var value by remember { mutableStateOf("") }

        var isError by remember { mutableStateOf(false) }

        SingleTextFieldDialog(
            title = "Add Tag",
            textFieldTitle = "Add Tag",
            value = value,
            isError = isError,
            keyboardType = KeyboardType.Text,
            onValueChange = {
                value = it
            },
            onDismissRequest = {
                showAddTagDialog = false
            },
            onUpdateClick = {
                if (value.isNotBlank()) {
                    onAddEblanApplicationInfoTag(EblanApplicationInfoTag(name = value))

                    showAddTagDialog = false
                } else {
                    isError = true
                }
            },
        )
    }
}
