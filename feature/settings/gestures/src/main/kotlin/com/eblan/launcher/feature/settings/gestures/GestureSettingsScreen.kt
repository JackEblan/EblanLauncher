package com.eblan.launcher.feature.settings.gestures

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.feature.settings.gestures.dialog.SelectApplicationDialog
import com.eblan.launcher.feature.settings.gestures.model.GesturesSettingsUiState
import kotlinx.coroutines.launch

@Composable
fun GestureSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: GestureSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val gesturesSettingsUiState by viewModel.gesturesSettingsUiState.collectAsStateWithLifecycle()

    GestureSettingsScreen(
        modifier = modifier,
        gesturesSettingsUiState = gesturesSettingsUiState,
        onNavigateUp = onNavigateUp,
        onUpdateDoubleTapGestureAction = viewModel::updateDoubleTap,
        onUpdateSwipeUpGestureAction = viewModel::updateSwipeUp,
        onUpdateSwipeDownGestureAction = viewModel::updateSwipeDown,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GestureSettingsScreen(
    modifier: Modifier = Modifier,
    gesturesSettingsUiState: GesturesSettingsUiState,
    onNavigateUp: () -> Unit,
    onUpdateDoubleTapGestureAction: (GestureAction) -> Unit,
    onUpdateSwipeUpGestureAction: (GestureAction) -> Unit,
    onUpdateSwipeDownGestureAction: (GestureAction) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Gestures")
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
            when (gesturesSettingsUiState) {
                GesturesSettingsUiState.Loading -> {

                }

                is GesturesSettingsUiState.Success -> {
                    Success(
                        modifier = modifier,
                        gestureSettings = gesturesSettingsUiState.userData.gestureSettings,
                        eblanApplicationInfos = gesturesSettingsUiState.eblanApplicationInfos,
                        onUpdateDoubleTapGestureAction = onUpdateDoubleTapGestureAction,
                        onUpdateSwipeUpGestureAction = onUpdateSwipeUpGestureAction,
                        onUpdateSwipeDownGestureAction = onUpdateSwipeDownGestureAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    gestureSettings: GestureSettings,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onUpdateDoubleTapGestureAction: (GestureAction) -> Unit,
    onUpdateSwipeUpGestureAction: (GestureAction) -> Unit,
    onUpdateSwipeDownGestureAction: (GestureAction) -> Unit,
) {
    var showDoubleTapBottomSheet by remember { mutableStateOf(false) }

    var showSwipeUpBottomSheet by remember { mutableStateOf(false) }

    var showSwipeDownBottomSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        GestureColumn(
            title = "Double tap",
            subtitle = gestureSettings.doubleTap.getSubtitle(),
            onClick = {
                showDoubleTapBottomSheet = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        GestureColumn(
            title = "Swipe up",
            subtitle = gestureSettings.swipeUp.getSubtitle(),
            onClick = {
                showSwipeUpBottomSheet = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        GestureColumn(
            title = "Swipe down",
            subtitle = gestureSettings.swipeDown.getSubtitle(),
            onClick = {
                showSwipeDownBottomSheet = true
            },
        )
    }

    if (showDoubleTapBottomSheet) {
        BottomSheetContent(
            gestureAction = gestureSettings.doubleTap,
            eblanApplicationInfos = eblanApplicationInfos,
            onUpdateGestureAction = onUpdateDoubleTapGestureAction,
            onDismiss = {
                showDoubleTapBottomSheet = false
            },
        )
    }

    if (showSwipeUpBottomSheet) {
        BottomSheetContent(
            gestureAction = gestureSettings.swipeUp,
            eblanApplicationInfos = eblanApplicationInfos,
            onUpdateGestureAction = onUpdateSwipeUpGestureAction,
            onDismiss = {
                showSwipeUpBottomSheet = false
            },
        )
    }

    if (showSwipeDownBottomSheet) {
        BottomSheetContent(
            gestureAction = gestureSettings.swipeDown,
            eblanApplicationInfos = eblanApplicationInfos,
            onUpdateGestureAction = onUpdateSwipeDownGestureAction,
            onDismiss = {
                showSwipeDownBottomSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetContent(
    modifier: Modifier = Modifier,
    gestureAction: GestureAction,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onUpdateGestureAction: (GestureAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    var selectedGestureAction by remember { mutableStateOf(gestureAction) }

    val scope = rememberCoroutineScope()

    var showSelectApplicationDialog by remember { mutableStateOf(false) }

    var selectedComponentName by remember { mutableStateOf("app") }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .fillMaxWidth(),
            ) {
                RadioButtonContent(
                    text = "None",
                    selected = selectedGestureAction is GestureAction.None,
                    onClick = {
                        selectedGestureAction = GestureAction.None
                    },
                )

                RadioButtonContent(
                    text = "Open app drawer",
                    selected = selectedGestureAction is GestureAction.OpenAppDrawer,
                    onClick = {
                        selectedGestureAction = GestureAction.OpenAppDrawer
                    },
                )

                RadioButtonContent(
                    text = "Open notification panel",
                    selected = selectedGestureAction is GestureAction.OpenNotificationPanel,
                    onClick = {
                        selectedGestureAction = GestureAction.OpenNotificationPanel
                    },
                )

                RadioButtonContent(
                    text = "Open $selectedComponentName",
                    selected = selectedGestureAction is GestureAction.OpenApp,
                    onClick = {
                        showSelectApplicationDialog = true
                    },
                )
            }
        }

        Button(
            onClick = {
                scope
                    .launch { sheetState.hide() }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismiss()
                        }
                    }
            },
        ) {
            Text("Cancel")
        }

        Button(
            onClick = {
                onUpdateGestureAction(selectedGestureAction)

                scope
                    .launch { sheetState.hide() }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismiss()
                        }
                    }
            },
        ) {
            Text("Save")
        }
    }

    if (showSelectApplicationDialog) {
        SelectApplicationDialog(
            eblanApplicationInfos = eblanApplicationInfos,
            onDismissRequest = {
                showSelectApplicationDialog = false
            },
            onUpdateGestureAction = { openAppGestureAction ->
                selectedGestureAction = openAppGestureAction

                selectedComponentName = openAppGestureAction.componentName
            },
        )
    }
}

@Composable
private fun RadioButtonContent(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun GestureColumn(
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

@Composable
private fun GestureAction.getSubtitle(): String {
    return when (this) {
        GestureAction.None -> "None"
        is GestureAction.OpenApp -> "Open $componentName"
        GestureAction.OpenAppDrawer -> "Open app drawer"
        GestureAction.OpenNotificationPanel -> "Open notification panel"
    }
}