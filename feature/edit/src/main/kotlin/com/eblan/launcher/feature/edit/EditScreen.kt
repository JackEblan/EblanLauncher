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
import com.eblan.launcher.domain.model.EblanLauncherApplicationInfo

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
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    applicationInfos: List<EblanLauncherApplicationInfo>,
    onNavigationIconClick: () -> Unit,
) {
    var label by remember { mutableStateOf("") }

    var icon by remember { mutableStateOf(ByteArray(0)) }

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
            var selectedGridItemType by remember { mutableStateOf(GridItemType.Application) }

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
                        label = label,
                        icon = icon,
                        applicationInfos = applicationInfos,
                        onLabelChange = {
                            label = it
                        },
                        onIconChange = {
                            icon = it
                        },
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
    label: String,
    icon: ByteArray,
    applicationInfos: List<EblanLauncherApplicationInfo>,
    onLabelChange: (String) -> Unit,
    onIconChange: (ByteArray) -> Unit,
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

        AsyncImage(model = icon, contentDescription = null, modifier = Modifier.size(40.dp))

        TextField(
            value = label,
            onValueChange = onLabelChange,
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
                            onLabelChange(eblanLauncherApplicationInfo.label)

                            onIconChange(eblanLauncherApplicationInfo.icon)

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