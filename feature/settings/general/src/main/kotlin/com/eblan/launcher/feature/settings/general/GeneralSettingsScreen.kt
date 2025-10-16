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

import android.content.Intent
import android.os.Build
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.EblanIconPackInfo
import com.eblan.launcher.domain.model.GeneralSettings
import com.eblan.launcher.domain.model.ThemeBrand
import com.eblan.launcher.feature.settings.general.dialog.ImportIconPackDialog
import com.eblan.launcher.feature.settings.general.dialog.SelectIconPackDialog
import com.eblan.launcher.feature.settings.general.model.GeneralSettingsUiState
import com.eblan.launcher.service.IconPackInfoService
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

    val packageManagerEblanIconPackInfos by viewModel.packageManagerEblanIconPackInfos.collectAsStateWithLifecycle()

    val eblanIconPackInfos by viewModel.eblanIconPackInfos.collectAsStateWithLifecycle()

    GeneralSettingsScreen(
        modifier = modifier,
        generalSettingsUiState = generalSettingsUiState,
        packageManagerIconPackInfos = packageManagerEblanIconPackInfos,
        eblanIconPackInfos = eblanIconPackInfos,
        onDeleteEblanIconPackInfo = viewModel::deleteIconPackInfo,
        onUpdateGeneralSettings = viewModel::updateGeneralSettings,
        onNavigateUp = onNavigateUp,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
    modifier: Modifier = Modifier,
    generalSettingsUiState: GeneralSettingsUiState,
    packageManagerIconPackInfos: List<EblanIconPackInfo>,
    eblanIconPackInfos: List<EblanIconPackInfo>,
    onDeleteEblanIconPackInfo: (String) -> Unit,
    onUpdateGeneralSettings: (GeneralSettings) -> Unit,
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
                .padding(paddingValues),
        ) {
            when (generalSettingsUiState) {
                GeneralSettingsUiState.Loading -> {
                }

                is GeneralSettingsUiState.Success -> {
                    Success(
                        modifier = modifier,
                        generalSettings = generalSettingsUiState.generalSettings,
                        packageManagerEblanIconPackInfos = packageManagerIconPackInfos,
                        eblanIconPackInfos = eblanIconPackInfos,
                        onDeleteEblanIconPackInfo = onDeleteEblanIconPackInfo,
                        onUpdateGeneralSettings = onUpdateGeneralSettings,
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
    packageManagerEblanIconPackInfos: List<EblanIconPackInfo>,
    eblanIconPackInfos: List<EblanIconPackInfo>,
    onDeleteEblanIconPackInfo: (String) -> Unit,
    onUpdateGeneralSettings: (GeneralSettings) -> Unit,
) {
    val context = LocalContext.current

    var showThemeBrandDialog by remember { mutableStateOf(false) }

    var showDarkThemeConfigDialog by remember { mutableStateOf(false) }

    var showImportIconPackDialog by remember { mutableStateOf(false) }

    var selectIconPackDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
    ) {
        SettingsColumn(
            title = "Import Icon Pack",
            subtitle = "Import icon pack",
            onClick = {
                showImportIconPackDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Select Icon Pack",
            subtitle = generalSettings.iconPackInfoPackageName.ifEmpty { "Default" },
            onClick = {
                selectIconPackDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingsSwitch(
                checked = generalSettings.dynamicTheme,
                title = "Dynamic Theme",
                subtitle = "Dynamic theme",
                onCheckedChange = { dynamicTheme ->
                    onUpdateGeneralSettings(generalSettings.copy(dynamicTheme = dynamicTheme))
                },
            )
        }
    }

    if (showThemeBrandDialog) {
        RadioOptionsDialog(
            title = "Theme Brand",
            options = ThemeBrand.entries,
            selected = generalSettings.themeBrand,
            label = {
                it.name
            },
            onDismissRequest = {
                showThemeBrandDialog = false
            },
            onUpdateClick = { themeBrand ->
                onUpdateGeneralSettings(generalSettings.copy(themeBrand = themeBrand))

                showThemeBrandDialog = false
            },
        )
    }

    if (showDarkThemeConfigDialog) {
        RadioOptionsDialog(
            title = "Dark Theme Config",
            options = DarkThemeConfig.entries,
            selected = generalSettings.darkThemeConfig,
            label = {
                it.name
            },
            onDismissRequest = {
                showDarkThemeConfigDialog = false
            },
            onUpdateClick = { darkThemeConfig ->
                onUpdateGeneralSettings(generalSettings.copy(darkThemeConfig = darkThemeConfig))

                showDarkThemeConfigDialog = false
            },
        )
    }

    if (showImportIconPackDialog) {
        ImportIconPackDialog(
            packageManagerIconPackInfos = packageManagerEblanIconPackInfos,
            onDismissRequest = {
                showImportIconPackDialog = false
            },
            onUpdateIconPack = { packageName, label ->
                val intent = Intent(context, IconPackInfoService::class.java).apply {
                    putExtra(IconPackManager.ICON_PACK_INFO_PACKAGE_NAME, packageName)
                    putExtra(IconPackManager.ICON_PACK_INFO_LABEL, label)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }

                showImportIconPackDialog = false
            },
        )
    }

    if (selectIconPackDialog) {
        SelectIconPackDialog(
            eblanIconPackInfos = eblanIconPackInfos,
            iconPackInfoPackageName = generalSettings.iconPackInfoPackageName,
            onDismissRequest = {
                selectIconPackDialog = false
            },
            onUpdateIconPackInfoPackageName = { iconPackInfoPackageName ->
                onUpdateGeneralSettings(generalSettings.copy(iconPackInfoPackageName = iconPackInfoPackageName))

                selectIconPackDialog = false
            },
            onDeleteEblanIconPackInfo = { eblanIconPackInfo ->
                onDeleteEblanIconPackInfo(eblanIconPackInfo.packageName)

                selectIconPackDialog = false
            },
            onReset = {
                onUpdateGeneralSettings(generalSettings.copy(iconPackInfoPackageName = ""))

                selectIconPackDialog = false
            },
        )
    }
}
