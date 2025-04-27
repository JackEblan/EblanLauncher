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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.settings.home.dialog.DockHeightDialog
import com.eblan.launcher.feature.settings.home.dialog.GridDialog
import com.eblan.launcher.feature.settings.home.model.HomeSettingsUiState

@Composable
fun HomeSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val homeSettingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()

    HomeSettingsScreen(
        modifier = modifier,
        homeSettingsUiState = homeSettingsUiState,
        onNavigateUp = onNavigateUp,
        onUpdateGrid = viewModel::updateGrid,
        onUpdateInfiniteScroll = viewModel::updateInfiniteScroll,
        onUpdateDockGrid = viewModel::updateDockGrid,
        onUpdateDockHeight = viewModel::updateDockHeight,
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
    onUpdateDockGrid: (
        dockRows: Int,
        dockColumns: Int,
    ) -> Unit,
    onUpdateDockHeight: (Int) -> Unit,
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                        userData = homeSettingsUiState.userData,
                        onUpdateGrid = onUpdateGrid,
                        onUpdateInfiniteScroll = onUpdateInfiniteScroll,
                        onUpdateDockGrid = onUpdateDockGrid,
                        onUpdateDockHeight = onUpdateDockHeight,
                    )
                }
            }
        }
    }

}

@Composable
fun Success(
    modifier: Modifier = Modifier,
    userData: UserData,
    onUpdateGrid: (
        rows: Int,
        columns: Int,
    ) -> Unit,
    onUpdateInfiniteScroll: (Boolean) -> Unit,
    onUpdateDockGrid: (
        dockRows: Int,
        dockColumns: Int,
    ) -> Unit,
    onUpdateDockHeight: (Int) -> Unit,
) {
    var showGridDialog by remember { mutableStateOf(false) }

    var showDockGridDialog by remember { mutableStateOf(false) }

    var showDockHeightDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(text = "Grid", style = MaterialTheme.typography.bodySmall)

        Column(
            modifier = modifier
                .fillMaxWidth()
                .clickable {
                    showGridDialog = true
                },
        ) {
            Text(text = "Grid", style = MaterialTheme.typography.bodyLarge)

            Text(
                text = "Number of rows and columns",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(
            modifier = modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Infinite scroll",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Scrolling at the end returns to first page",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Switch(
                checked = userData.infiniteScroll,
                onCheckedChange = onUpdateInfiniteScroll,
            )
        }

        Text(text = "Dock", style = MaterialTheme.typography.bodySmall)

        Column(
            modifier = modifier
                .fillMaxWidth()
                .clickable {
                    showDockGridDialog = true
                },
        ) {
            Text(text = "Dock Grid", style = MaterialTheme.typography.bodyLarge)

            Text(
                text = "Number of rows and columns",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .clickable {
                    showDockHeightDialog = true
                },
        ) {
            Text(text = "Dock height", style = MaterialTheme.typography.bodyLarge)

            Text(
                text = "Height of the dock by pixels",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    if (showGridDialog) {
        GridDialog(
            rows = userData.rows,
            columns = userData.columns,
            onDismissRequest = {
                showGridDialog = false
            },
            onUpdateClick = onUpdateGrid,
        )
    }

    if (showDockGridDialog) {
        GridDialog(
            rows = userData.dockRows,
            columns = userData.dockColumns,
            onDismissRequest = {
                showDockGridDialog = false
            },
            onUpdateClick = onUpdateDockGrid,
        )
    }

    if (showDockHeightDialog) {
        DockHeightDialog(
            dockHeight = userData.dockHeight,
            onDismissRequest = {
                showDockHeightDialog = false
            },
            onUpdateClick = onUpdateDockHeight,
        )
    }
}