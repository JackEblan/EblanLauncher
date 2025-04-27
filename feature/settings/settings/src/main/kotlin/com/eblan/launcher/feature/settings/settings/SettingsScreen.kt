package com.eblan.launcher.feature.settings.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.settings.settings.model.SettingsUiState

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onHome: () -> Unit,
) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()

    SettingsScreen(
        modifier = modifier,
        settingsUiState = settingsUiState,
        onNavigateUp = onNavigateUp,
        onHome = onHome,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsUiState: SettingsUiState,
    onNavigateUp: () -> Unit,
    onHome: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Settings")
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
            when (settingsUiState) {
                SettingsUiState.Loading -> {

                }

                is SettingsUiState.Success -> {
                    Success(
                        userData = settingsUiState.userData,
                        onHome = onHome,
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
    onHome: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Image(imageVector = Icons.Default.Home, contentDescription = null)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onHome),
            ) {
                Text(text = "Home screen")

                Text(text = "Set grid, icon layout, dock settings, and more")
            }
        }
    }
}