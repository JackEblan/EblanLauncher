package com.eblan.launcher.feature.settings.gestures.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(10.dp))

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