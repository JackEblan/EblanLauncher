/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.feature.settings.gestures.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialogContainer
import com.eblan.launcher.designsystem.component.EblanRadioButton
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.feature.settings.gestures.getGestureActionSubtitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GestureActionDialog(
    modifier: Modifier = Modifier,
    title: String,
    gestureAction: GestureAction,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onUpdateGestureAction: (GestureAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var selectedGestureAction by remember { mutableStateOf(gestureAction) }

    var showSelectApplicationDialog by remember { mutableStateOf(false) }

    val gestureActions by remember {
        derivedStateOf {
            listOf(
                GestureAction.None,
                GestureAction.OpenAppDrawer,
                GestureAction.OpenNotificationPanel,
                selectedGestureAction.let { gestureAction ->
                    if (gestureAction is GestureAction.OpenApp) {
                        GestureAction.OpenApp(componentName = gestureAction.componentName)
                    } else {
                        GestureAction.OpenApp(componentName = "app")
                    }
                },
                GestureAction.LockScreen,
                GestureAction.OpenQuickSettings,
                GestureAction.OpenRecents,
            )
        }
    }

    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .selectableGroup()
                    .fillMaxWidth(),
            ) {
                gestureActions.forEach { currentGestureAction ->
                    EblanRadioButton(
                        text = currentGestureAction.getGestureActionSubtitle(),
                        selected = selectedGestureAction == currentGestureAction,
                        onClick = {
                            if (currentGestureAction is GestureAction.OpenApp) {
                                showSelectApplicationDialog = true
                            }

                            selectedGestureAction = currentGestureAction
                        },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        end = 10.dp,
                        bottom = 10.dp,
                    ),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onDismissRequest,
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(5.dp))

                TextButton(
                    onClick = {
                        onUpdateGestureAction(selectedGestureAction)

                        onDismissRequest()
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
                selectedGestureAction = GestureAction.None

                showSelectApplicationDialog = false
            },
            onUpdateGestureAction = { openAppGestureAction ->
                selectedGestureAction = openAppGestureAction

                showSelectApplicationDialog = false
            },
        )
    }
}
