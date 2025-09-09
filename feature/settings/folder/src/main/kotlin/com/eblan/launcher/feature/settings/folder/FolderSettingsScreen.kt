package com.eblan.launcher.feature.settings.folder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.feature.settings.folder.model.FolderSettingsUiState
import com.eblan.launcher.ui.dialog.TwoNumberTextFieldsDialog
import com.eblan.launcher.ui.settings.SettingsColumn

@Composable
fun FolderSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: FolderSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val folderSettingsUiState by viewModel.folderSettingsUiState.collectAsStateWithLifecycle()

    FolderSettingsScreen(
        modifier = modifier,
        folderSettingsUiState = folderSettingsUiState,
        onNavigateUp = onNavigateUp,
        onUpdateFolderGrid = viewModel::updateFolderGrid,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSettingsScreen(
    modifier: Modifier = Modifier,
    folderSettingsUiState: FolderSettingsUiState,
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
            when (folderSettingsUiState) {
                FolderSettingsUiState.Loading -> {

                }

                is FolderSettingsUiState.Success -> {
                    Success(
                        homeSettings = folderSettingsUiState.homeSettings,
                        onUpdateFolderGrid = onUpdateFolderGrid,
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
    onUpdateFolderGrid: (
        rows: Int,
        columns: Int,
    ) -> Unit,
) {
    var showGridDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsColumn(
            title = "Folder Grid",
            subtitle = "Number of rows and columns",
            onClick = {
                showGridDialog = true
            },
        )
    }

    if (showGridDialog) {
        TwoNumberTextFieldsDialog(
            title = "Folder Grid",
            firstTextFieldTitle = "Folder Rows",
            secondTextFieldTitle = "Folder Columns",
            firstTextFieldValue = homeSettings.folderRows,
            secondTextFieldValue = homeSettings.folderColumns,
            onDismissRequest = {
                showGridDialog = false
            },
            onUpdateClick = onUpdateFolderGrid,
        )
    }
}