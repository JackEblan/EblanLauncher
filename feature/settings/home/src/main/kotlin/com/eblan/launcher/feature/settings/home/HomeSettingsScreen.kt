package com.eblan.launcher.feature.settings.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSettingsScreen(
    modifier: Modifier = Modifier,
    homeSettingsUiState: HomeSettingsUiState,
    onNavigateUp: () -> Unit,
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
                    Success()
                }
            }
        }
    }

}

@Composable
fun Success(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(text = "Grid", style = MaterialTheme.typography.bodySmall)

        Text(text = "Grid")

        Text(text = "Infinite scroll")

        Text(text = "Dock", style = MaterialTheme.typography.bodySmall)

        Text(text = "Dock")

        Text(text = "Dock Height")
    }
}