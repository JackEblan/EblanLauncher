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
package com.eblan.launcher.feature.action

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.EblanAction
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionScreen(
    modifier: Modifier = Modifier,
    onUpdateEblanAction: suspend (EblanAction) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var selectedEblanAction by remember {
        mutableStateOf<EblanAction>(EblanAction.None)
    }

    val eblanActions by remember {
        derivedStateOf {
            listOf(
                EblanAction.None,
                EblanAction.OpenAppDrawer,
                EblanAction.OpenNotificationPanel,
                selectedEblanAction.let { eblanAction ->
                    if (eblanAction is EblanAction.OpenApp) {
                        EblanAction.OpenApp(componentName = eblanAction.componentName)
                    } else {
                        EblanAction.OpenApp(componentName = "app")
                    }
                },
                EblanAction.LockScreen,
                EblanAction.OpenQuickSettings,
                EblanAction.OpenRecents,
            )
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = "Eblan Action")
        })
    }) { paddingValues ->
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            eblanActions.forEach { eblanAction ->
                Row(
                    modifier = Modifier
                        .clickable {
                            scope.launch {
                                onUpdateEblanAction(eblanAction)
                            }
                        }
                        .fillMaxWidth()
                        .padding(10.dp),
                ) {
                    Text(text = eblanAction.getEblanActionSubtitle())
                }
            }
        }
    }
}

private fun EblanAction.getEblanActionSubtitle(): String {
    return when (this) {
        EblanAction.None -> "None"
        is EblanAction.OpenApp -> "Open $componentName"
        EblanAction.OpenAppDrawer -> "Open App Drawer"
        EblanAction.OpenNotificationPanel -> "Open Notification Panel"
        EblanAction.LockScreen -> "Lock Screen"
        EblanAction.OpenQuickSettings -> "Open Quick Settings"
        EblanAction.OpenRecents -> "Open Recents"
    }
}
