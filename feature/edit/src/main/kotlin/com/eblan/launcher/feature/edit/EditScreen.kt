package com.eblan.launcher.feature.edit

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData

@Composable
fun EditRoute(
    modifier: Modifier = Modifier,
    viewModel: EditViewModel = hiltViewModel(),
    onNavigationIconClick: () -> Unit,
) {
    val applicationInfos by viewModel.applicationInfos.collectAsStateWithLifecycle()

    val gridItem by viewModel.gridItem.collectAsStateWithLifecycle()

    val gridRepositoryUpdate by viewModel.gridRepositoryUpdate.collectAsStateWithLifecycle()

    val eblanApplicationInfoInstalledProviders by viewModel.eblanApplicationInfoInstalledProviders.collectAsStateWithLifecycle()

    val installedProvidersByPackageName by viewModel.installedProvidersByPackageName.collectAsStateWithLifecycle()

    EditScreen(
        modifier = modifier,
        eblanApplicationInfos = applicationInfos,
        eblanApplicationInfoInstalledProviders = eblanApplicationInfoInstalledProviders,
        installedProvidersByPackageName = installedProvidersByPackageName,
        gridItem = gridItem,
        gridRepositoryUpdate = gridRepositoryUpdate,
        onNavigationIconClick = onNavigationIconClick,
        onAddApplicationInfo = viewModel::addApplicationInfo,
        onGetInstalledProviderByPackageName = viewModel::getInstalledProviderByPackageName,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    eblanApplicationInfoInstalledProviders: List<EblanApplicationInfo>,
    installedProvidersByPackageName: List<AppWidgetProviderInfo>,
    gridItem: GridItem?,
    gridRepositoryUpdate: Boolean?,
    onNavigationIconClick: () -> Unit,
    onAddApplicationInfo: (
        packageName: String,
        label: String,
    ) -> Unit,
    onGetInstalledProviderByPackageName: (String) -> Unit,
) {
    var selectedGridItemIndex by remember { mutableIntStateOf(0) }

    val applicationScreenUiState = rememberApplicationScreenUiState()

    val snackbarHostState = remember { SnackbarHostState() }

    val gridItemDataItems = listOf(
        "Application",
        "Widget",
    )

    LaunchedEffect(key1 = gridItem) {
        when (val gridItemData = gridItem?.data) {
            is GridItemData.ApplicationInfo -> {
                applicationScreenUiState.packageName = gridItemData.packageName

                applicationScreenUiState.icon = gridItemData.icon

                applicationScreenUiState.label = gridItemData.label
            }

            is GridItemData.Widget -> {

            }

            null -> {

            }
        }
    }

    LaunchedEffect(key1 = gridRepositoryUpdate) {
        when (gridRepositoryUpdate) {
            true -> {
                snackbarHostState.showSnackbar("Grid Item Data updated")
            }

            false -> {
                snackbarHostState.showSnackbar("Grid Item Data updated failed")
            }

            null -> {

            }
        }
    }

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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedGridItemIndex) {
                        0 -> {
                            if (applicationScreenUiState.validate()) {
                                onAddApplicationInfo(
                                    applicationScreenUiState.packageName!!,
                                    applicationScreenUiState.label,
                                )
                            }
                        }

                        1 -> {

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

            ContextualFlowRow(
                modifier = Modifier.fillMaxWidth(),
                itemCount = gridItemDataItems.size,
            ) { index ->
                FilterChip(
                    selected = index == selectedGridItemIndex,
                    onClick = {
                        selectedGridItemIndex = index
                    },
                    label = {
                        Text(text = gridItemDataItems[index])
                    },
                )
            }

            when (selectedGridItemIndex) {
                0 -> {
                    ApplicationScreen(
                        applicationScreenUiState = applicationScreenUiState,
                        eblanApplicationInfos = eblanApplicationInfos,
                    )
                }

                1 -> {
                    WidgetScreen(
                        eblanApplicationInfoInstalledProviders = eblanApplicationInfoInstalledProviders,
                        installedProvidersByPackageName = installedProvidersByPackageName,
                        onGetInstalledProviderByPackageName = onGetInstalledProviderByPackageName,
                    )
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
    eblanApplicationInfos: List<EblanApplicationInfo>,
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
                items(eblanApplicationInfos) { eblanLauncherApplicationInfo ->
                    Column(
                        modifier = Modifier.clickable {
                            applicationScreenUiState.packageName =
                                eblanLauncherApplicationInfo.packageName

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetScreen(
    modifier: Modifier = Modifier,
    eblanApplicationInfoInstalledProviders: List<EblanApplicationInfo>,
    installedProvidersByPackageName: List<AppWidgetProviderInfo>,
    onGetInstalledProviderByPackageName: (String) -> Unit,
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

        LazyColumn {
            items(installedProvidersByPackageName) { appWidgetProviderInfo ->
                WidgetPreview(appWidgetProviderInfo = appWidgetProviderInfo)
            }
        }
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
                items(eblanApplicationInfoInstalledProviders) { eblanLauncherApplicationInfo ->
                    Column(
                        modifier = Modifier.clickable {
                            onGetInstalledProviderByPackageName(eblanLauncherApplicationInfo.packageName)
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

@Composable
fun WidgetPreview(modifier: Modifier = Modifier, appWidgetProviderInfo: AppWidgetProviderInfo) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        AsyncImage(
            model = appWidgetProviderInfo.loadPreviewImage(context, 0),
            contentDescription = null,
        )
    }
}
