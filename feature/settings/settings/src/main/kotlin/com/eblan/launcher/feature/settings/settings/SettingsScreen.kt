package com.eblan.launcher.feature.settings.settings

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.feature.settings.settings.model.SettingsUiState

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onFinish: () -> Unit,
    onGeneral: () -> Unit,
    onHome: () -> Unit,
    onAppDrawer: () -> Unit,
    onGestures: () -> Unit,
    onFolder: () -> Unit,
) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()

    SettingsScreen(
        modifier = modifier,
        settingsUiState = settingsUiState,
        onFinish = onFinish,
        onGeneral = onGeneral,
        onHome = onHome,
        onAppDrawer = onAppDrawer,
        onGestures = onGestures,
        onFolder = onFolder,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsUiState: SettingsUiState,
    onFinish: () -> Unit,
    onGeneral: () -> Unit,
    onHome: () -> Unit,
    onAppDrawer: () -> Unit,
    onGestures: () -> Unit,
    onFolder: () -> Unit,
) {
    BackHandler {
        onFinish()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Settings")
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
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
            when (settingsUiState) {
                SettingsUiState.Loading -> {

                }

                is SettingsUiState.Success -> {
                    Success(
                        onGeneral = onGeneral,
                        onHome = onHome,
                        onAppDrawer = onAppDrawer,
                        onGestures = onGestures,
                        onFolder = onFolder,
                    )
                }
            }
        }
    }
}

@Composable
fun Success(
    modifier: Modifier = Modifier,
    onGeneral: () -> Unit,
    onHome: () -> Unit,
    onAppDrawer: () -> Unit,
    onGestures: () -> Unit,
    onFolder: () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
    ) {
        SettingsRow(
            imageVector = EblanLauncherIcons.Settings,
            title = "General",
            subtitle = "Colors, icon packs",
            onClick = onGeneral,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsRow(
            imageVector = EblanLauncherIcons.Home,
            title = "Home",
            subtitle = "Grid, icon, dock, and more",
            onClick = onHome,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsRow(
            imageVector = EblanLauncherIcons.Apps,
            title = "App Drawer",
            subtitle = "Rows and columns count",
            onClick = onAppDrawer,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsRow(
            imageVector = EblanLauncherIcons.Folder,
            title = "Folder",
            subtitle = "Rows and columns count",
            onClick = onFolder,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsRow(
            imageVector = EblanLauncherIcons.Gesture,
            title = "Gestures",
            subtitle = "Grid, icon, dock, and more",
            onClick = onGestures,
        )
    }
}

@Composable
private fun SettingsRow(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f),
        ) {
            Text(text = title)

            Text(text = subtitle)
        }
    }
}