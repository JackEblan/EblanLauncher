package com.eblan.launcher.feature.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import coil.compose.AsyncImage
import com.eblan.launcher.domain.model.GridItemType
import com.eblan.launcher.domain.model.InMemoryApplicationInfo

@Composable
fun EditRoute(
    modifier: Modifier = Modifier,
    viewModel: EditViewModel = hiltViewModel(),
    onNavigationIconClick: () -> Unit,
) {
    val applicationInfos by viewModel.applicationInfos.collectAsStateWithLifecycle()

    EditScreen(
        modifier = modifier,
        applicationInfos = applicationInfos,
        onNavigationIconClick = onNavigationIconClick,
        onAddApplicationInfo = viewModel::addApplicationInfo,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    applicationInfos: List<InMemoryApplicationInfo>,
    onNavigationIconClick: () -> Unit,
    onAddApplicationInfo: (
        type: GridItemType,
        packageName: String,
        flags: Int,
        icon: ByteArray?,
        label: String,
    ) -> Unit,
) {
    var selectedGridItemType by remember { mutableStateOf(GridItemType.Application) }

    val applicationScreenUiState = rememberApplicationScreenUiState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Edit")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigationIconClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedGridItemType) {
                        GridItemType.Application -> {
                            if (applicationScreenUiState.validate()) {
                                onAddApplicationInfo(
                                    selectedGridItemType,
                                    applicationScreenUiState.packageName!!,
                                    applicationScreenUiState.flags!!,
                                    applicationScreenUiState.icon,
                                    applicationScreenUiState.label,
                                )
                            }
                        }

                        GridItemType.Widget -> {

                        }
                    }
                },
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
            }
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {

            ContextualFlowRow(modifier = Modifier.fillMaxWidth(), itemCount = 2) { index ->
                FilterChip(
                    selected = GridItemType.entries[index] == selectedGridItemType,
                    onClick = {
                        selectedGridItemType = GridItemType.entries[index]
                    },
                    label = {
                        val title = when (GridItemType.entries[index]) {
                            GridItemType.Application -> "Application"
                            GridItemType.Widget -> "Widget"
                        }

                        Text(text = title)
                    },
                )
            }

            when (selectedGridItemType) {
                GridItemType.Application -> {
                    ApplicationScreen(
                        applicationScreenUiState = applicationScreenUiState,
                        applicationInfos = applicationInfos,
                    )
                }

                GridItemType.Widget -> {

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationScreen(
    modifier: Modifier = Modifier,
    applicationScreenUiState: ApplicationScreenUiState,
    applicationInfos: List<InMemoryApplicationInfo>,
) {
    val selectApplicationBottomSheetState = rememberModalBottomSheetState()

    var showSelectApplicationBottomSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Select Application",
            modifier = Modifier.clickable {
                showSelectApplicationBottomSheet = true
            },
        )

        AsyncImage(
            model = applicationScreenUiState.icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )

        TextField(
            value = applicationScreenUiState.label,
            onValueChange = {
                applicationScreenUiState.label = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Label") },
        )
    }

    if (showSelectApplicationBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSelectApplicationBottomSheet = false
            },
            sheetState = selectApplicationBottomSheetState,
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(applicationInfos) { eblanLauncherApplicationInfo ->
                    Column(
                        modifier = Modifier.clickable {
                            applicationScreenUiState.packageName =
                                eblanLauncherApplicationInfo.packageName

                            applicationScreenUiState.flags = eblanLauncherApplicationInfo.flags

                            applicationScreenUiState.icon = eblanLauncherApplicationInfo.icon

                            applicationScreenUiState.label = eblanLauncherApplicationInfo.label

                            showSelectApplicationBottomSheet = false
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AsyncImage(
                            model = eblanLauncherApplicationInfo.icon,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )

                        Text(
                            text = eblanLauncherApplicationInfo.label,
                        )
                    }
                }
            }
        }
    }
}