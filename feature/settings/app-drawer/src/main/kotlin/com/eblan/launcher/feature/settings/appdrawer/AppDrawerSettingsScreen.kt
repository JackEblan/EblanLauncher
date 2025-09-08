package com.eblan.launcher.feature.settings.appdrawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.settings.appdrawer.dialog.GridDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.IconSizeDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.TextColorDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.TextSizeDialog
import com.eblan.launcher.feature.settings.appdrawer.model.AppDrawerSettingsUiState

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
fun Success(
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
        Text(text = "Grid", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(5.dp))

        SettingsColumn(
            title = "App Drawer",
            subtitle = "Number of columns and rows height",
            onClick = {
                showGridDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Grid Item", style = MaterialTheme.typography.bodySmall)

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
            subtitle = appDrawerSettings.gridItemSettings.textColor.getTextColorSubtitle(),
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

        SwitchRow(
            modifier = modifier,
            checked = appDrawerSettings.gridItemSettings.showLabel,
            title = "Show Label",
            subtitle = "Show the label",
            onCheckedChange = onUpdateAppDrawerShowLabel,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SwitchRow(
            modifier = modifier,
            checked = appDrawerSettings.gridItemSettings.singleLineLabel,
            title = "Single Line Label",
            subtitle = "Show single line label",
            onCheckedChange = onUpdateAppDrawerSingleLineLabel,
        )
    }

    if (showGridDialog) {
        GridDialog(
            columns = appDrawerSettings.appDrawerColumns,
            rowsHeight = appDrawerSettings.appDrawerRowsHeight,
            onDismissRequest = {
                showGridDialog = false
            },
            onUpdateClick = onUpdateAppDrawerGrid,
        )
    }



    if (showIconSizeDialog) {
        IconSizeDialog(
            iconSize = appDrawerSettings.gridItemSettings.iconSize,
            onDismissRequest = {
                showIconSizeDialog = false
            },
            onUpdateClick = onUpdateAppDrawerIconSize,
        )
    }

    if (showTextColorDialog) {
        TextColorDialog(
            textColor = appDrawerSettings.gridItemSettings.textColor,
            onDismissRequest = {
                showTextColorDialog = false
            },
            onUpdateClick = onUpdateAppDrawerTextColor,
        )
    }

    if (showTextSizeDialog) {
        TextSizeDialog(
            textSize = appDrawerSettings.gridItemSettings.textSize,
            onDismissRequest = {
                showTextSizeDialog = false
            },
            onUpdateClick = onUpdateAppDrawerTextSize,
        )
    }
}

@Composable
private fun SettingsColumn(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SwitchRow(
    modifier: Modifier = Modifier,
    checked: Boolean,
    title: String,
    subtitle: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}


@Composable
private fun TextColor.getTextColorSubtitle(): String {
    return when (this) {
        TextColor.System -> "System"
        TextColor.Light -> "Light"
        TextColor.Dark -> "Dark"
    }
}