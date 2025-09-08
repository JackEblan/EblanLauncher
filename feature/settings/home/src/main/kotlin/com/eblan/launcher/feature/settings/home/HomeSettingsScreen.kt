package com.eblan.launcher.feature.settings.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.settings.home.dialog.DockHeightDialog
import com.eblan.launcher.feature.settings.home.dialog.GridDialog
import com.eblan.launcher.feature.settings.home.dialog.IconSizeDialog
import com.eblan.launcher.feature.settings.home.dialog.TextColorDialog
import com.eblan.launcher.feature.settings.home.dialog.TextSizeDialog
import com.eblan.launcher.feature.settings.home.model.HomeSettingsUiState

@Composable
fun HomeSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val homeSettingsUiState by viewModel.homeSettingsUiState.collectAsStateWithLifecycle()

    HomeSettingsScreen(
        modifier = modifier,
        homeSettingsUiState = homeSettingsUiState,
        onNavigateUp = onNavigateUp,
        onUpdateGrid = viewModel::updateGrid,
        onUpdateInfiniteScroll = viewModel::updateInfiniteScroll,
        onUpdateWallpaperScroll = viewModel::updateWallpaperScroll,
        onUpdateDockGrid = viewModel::updateDockGrid,
        onUpdateDockHeight = viewModel::updateDockHeight,
        onUpdateIconSize = viewModel::updateIconSize,
        onUpdateTextColor = viewModel::updateTextColor,
        onUpdateTextSize = viewModel::updateTextSize,
        onUpdateShowLabel = viewModel::updateShowLabel,
        onUpdateSingleLineLabel = viewModel::updateSingleLineLabel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSettingsScreen(
    modifier: Modifier = Modifier,
    homeSettingsUiState: HomeSettingsUiState,
    onNavigateUp: () -> Unit,
    onUpdateGrid: (
        rows: Int,
        columns: Int,
    ) -> Unit,
    onUpdateInfiniteScroll: (Boolean) -> Unit,
    onUpdateWallpaperScroll: (Boolean) -> Unit,
    onUpdateDockGrid: (
        dockRows: Int,
        dockColumns: Int,
    ) -> Unit,
    onUpdateDockHeight: (Int) -> Unit,
    onUpdateIconSize: (Int) -> Unit,
    onUpdateTextColor: (TextColor) -> Unit,
    onUpdateTextSize: (Int) -> Unit,
    onUpdateShowLabel: (Boolean) -> Unit,
    onUpdateSingleLineLabel: (Boolean) -> Unit,
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
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {
            when (homeSettingsUiState) {
                HomeSettingsUiState.Loading -> {

                }

                is HomeSettingsUiState.Success -> {
                    Success(
                        homeSettings = homeSettingsUiState.homeSettings,
                        onUpdateGrid = onUpdateGrid,
                        onUpdateInfiniteScroll = onUpdateInfiniteScroll,
                        onUpdateWallpaperScroll = onUpdateWallpaperScroll,
                        onUpdateDockGrid = onUpdateDockGrid,
                        onUpdateDockHeight = onUpdateDockHeight,
                        onUpdateIconSize = onUpdateIconSize,
                        onUpdateTextColor = onUpdateTextColor,
                        onUpdateTextSize = onUpdateTextSize,
                        onUpdateShowLabel = onUpdateShowLabel,
                        onUpdateSingleLineLabel = onUpdateSingleLineLabel,
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
    onUpdateGrid: (
        rows: Int,
        columns: Int,
    ) -> Unit,
    onUpdateInfiniteScroll: (Boolean) -> Unit,
    onUpdateWallpaperScroll: (Boolean) -> Unit,
    onUpdateDockGrid: (
        dockRows: Int,
        dockColumns: Int,
    ) -> Unit,
    onUpdateDockHeight: (Int) -> Unit,
    onUpdateIconSize: (Int) -> Unit,
    onUpdateTextColor: (TextColor) -> Unit,
    onUpdateTextSize: (Int) -> Unit,
    onUpdateShowLabel: (Boolean) -> Unit,
    onUpdateSingleLineLabel: (Boolean) -> Unit,
) {
    var showGridDialog by remember { mutableStateOf(false) }

    var showDockGridDialog by remember { mutableStateOf(false) }

    var showDockHeightDialog by remember { mutableStateOf(false) }

    var showIconSizeDialog by remember { mutableStateOf(false) }

    var showTextColorDialog by remember { mutableStateOf(false) }

    var showTextSizeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        Text(text = "Grid", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(5.dp))

        SettingsColumn(
            title = "Grid",
            subtitle = "Number of rows and columns",
            onClick = {
                showGridDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SwitchRow(
            checked = homeSettings.infiniteScroll,
            title = "Infinite Scrolling",
            subtitle = "Scrolling at the end returns to first page",
            onCheckedChange = onUpdateInfiniteScroll,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SwitchRow(
            checked = homeSettings.wallpaperScroll,
            title = "Wallpaper Scrolling",
            subtitle = "Scroll the wallpaper as you go through pages",
            onCheckedChange = onUpdateWallpaperScroll,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Dock", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(5.dp))

        SettingsColumn(
            title = "Dock Grid",
            subtitle = "Number of rows and columns",
            onClick = {
                showDockGridDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Dock Height",
            subtitle = "Height of the dock by pixels",
            onClick = {
                showDockHeightDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Grid Item", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(5.dp))

        SettingsColumn(
            title = "Icon Size",
            subtitle = "${homeSettings.gridItemSettings.iconSize}",
            onClick = {
                showIconSizeDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Text Color",
            subtitle = homeSettings.gridItemSettings.textColor.getTextColorSubtitle(),
            onClick = {
                showTextColorDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Text Size",
            subtitle = "${homeSettings.gridItemSettings.textSize}",
            onClick = {
                showTextSizeDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SwitchRow(
            checked = homeSettings.gridItemSettings.showLabel,
            title = "Show Label",
            subtitle = "Show label",
            onCheckedChange = onUpdateShowLabel,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SwitchRow(
            checked = homeSettings.gridItemSettings.singleLineLabel,
            title = "Single Line Label",
            subtitle = "Show single line label",
            onCheckedChange = onUpdateSingleLineLabel,
        )
    }

    if (showGridDialog) {
        GridDialog(
            rows = homeSettings.rows,
            columns = homeSettings.columns,
            onDismissRequest = {
                showGridDialog = false
            },
            onUpdateClick = onUpdateGrid,
        )
    }

    if (showDockGridDialog) {
        GridDialog(
            rows = homeSettings.dockRows,
            columns = homeSettings.dockColumns,
            onDismissRequest = {
                showDockGridDialog = false
            },
            onUpdateClick = onUpdateDockGrid,
        )
    }

    if (showDockHeightDialog) {
        DockHeightDialog(
            dockHeight = homeSettings.dockHeight,
            onDismissRequest = {
                showDockHeightDialog = false
            },
            onUpdateClick = onUpdateDockHeight,
        )
    }

    if (showIconSizeDialog) {
        IconSizeDialog(
            iconSize = homeSettings.gridItemSettings.iconSize,
            onDismissRequest = {
                showIconSizeDialog = false
            },
            onUpdateClick = onUpdateIconSize,
        )
    }

    if (showTextColorDialog) {
        TextColorDialog(
            textColor = homeSettings.gridItemSettings.textColor,
            onDismissRequest = {
                showTextColorDialog = false
            },
            onUpdateClick = onUpdateTextColor,
        )
    }

    if (showTextSizeDialog) {
        TextSizeDialog(
            textSize = homeSettings.gridItemSettings.textSize,
            onDismissRequest = {
                showTextSizeDialog = false
            },
            onUpdateClick = onUpdateTextSize,
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
            .clickable(onClick = {
                onCheckedChange(!checked)
            })
            .fillMaxWidth()
            .padding(5.dp),
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
private fun SettingsColumn(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(5.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
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