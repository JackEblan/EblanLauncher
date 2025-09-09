package com.eblan.launcher.feature.settings.gestures.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanRadioButton
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GestureAction
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureActionBottomSheet(
    modifier: Modifier = Modifier,
    title: String,
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

    val options = listOf(
        GestureAction.None,
        GestureAction.OpenAppDrawer,
        GestureAction.OpenNotificationPanel,
        GestureAction.OpenApp(componentName = selectedComponentName),
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = title, style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .selectableGroup()
                    .fillMaxWidth(),
            ) {
                options.forEach { gestureActionClass ->
                    val label by remember {
                        derivedStateOf {
                            when (gestureActionClass) {
                                GestureAction.None -> "None"
                                is GestureAction.OpenApp -> "Open $selectedComponentName"
                                GestureAction.OpenAppDrawer -> "Open App Drawer"
                                GestureAction.OpenNotificationPanel -> "Open Notification Panel"
                            }
                        }
                    }

                    EblanRadioButton(
                        text = label,
                        selected = selectedGestureAction == gestureActionClass,
                        onClick = {
                            if (gestureActionClass is GestureAction.OpenApp) {
                                showSelectApplicationDialog = true
                            }

                            selectedGestureAction = gestureActionClass
                        },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
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

                Spacer(modifier = Modifier.width(5.dp))

                TextButton(
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