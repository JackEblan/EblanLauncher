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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.eblan.launcher.domain.model.GestureAction
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionScreen(
    modifier: Modifier = Modifier,
    onUpdateEblanAction: suspend (GestureAction) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var selectedGestureAction by remember {
        mutableStateOf<GestureAction>(GestureAction.None)
    }

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

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = "Eblan Action")
        })
    }) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            LazyColumn(modifier = Modifier.matchParentSize()) {
                items(gestureActions) { gestureAction ->
                    Row(
                        modifier = Modifier
                            .clickable {
                                scope.launch {
                                    onUpdateEblanAction(gestureAction)
                                }
                            }
                            .fillMaxWidth()
                            .padding(10.dp),
                    ) {
                        Text(text = gestureAction.getGestureActionSubtitle())
                    }
                }
            }
        }
    }
}

private fun GestureAction.getGestureActionSubtitle(): String {
    return when (this) {
        GestureAction.None -> "None"
        is GestureAction.OpenApp -> "Open $componentName"
        GestureAction.OpenAppDrawer -> "Open app drawer"
        GestureAction.OpenNotificationPanel -> "Open notification panel"
        GestureAction.LockScreen -> "Lock screen"
        GestureAction.OpenQuickSettings -> "Open quick settings"
        GestureAction.OpenRecents -> "Open recents"
    }
}
