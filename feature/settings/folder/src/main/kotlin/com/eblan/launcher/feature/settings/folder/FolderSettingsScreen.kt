package com.eblan.launcher.feature.settings.folder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.feature.settings.folder.dialog.GridDialog
import com.eblan.launcher.feature.settings.folder.model.FolderSettingsUiState

@Composable
fun FolderSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: FolderSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val homeSettingsUiState by viewModel.folderSettingsUiState.collectAsStateWithLifecycle()

    FolderSettingsScreen(
        modifier = modifier,
        homeSettingsUiState = homeSettingsUiState,
        onNavigateUp = onNavigateUp,
        onUpdateFolderGrid = viewModel::updateFolderGrid,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSettingsScreen(
    modifier: Modifier = Modifier,
    homeSettingsUiState: FolderSettingsUiState,
    onNavigateUp: () -> Unit,
    onUpdateFolderGrid: (
        rows: Int,
        columns: Int,
    ) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Folder")
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
                FolderSettingsUiState.Loading -> {

                }

                is FolderSettingsUiState.Success -> {
                    Success(
                        homeSettings = homeSettingsUiState.userData.homeSettings,
                        onUpdateFolderGrid = onUpdateFolderGrid,
                    )
                }
            }
        }
    }

}

@Composable
fun Success(
    modifier: Modifier = Modifier,
    homeSettings: HomeSettings,
    onUpdateFolderGrid: (
        rows: Int,
        columns: Int,
    ) -> Unit,
) {
    var showGridDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(text = "Grid", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(5.dp))

        SettingsColumn(
            title = "Grid",
            subtitle = "Number of rows and columns",
            onClick = {
                showGridDialog = true
            },
        )
    }

    if (showGridDialog) {
        GridDialog(
            rows = homeSettings.folderRows,
            columns = homeSettings.folderColumns,
            onDismissRequest = {
                showGridDialog = false
            },
            onUpdateClick = onUpdateFolderGrid,
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