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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanAction
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionScreen(
    modifier: Modifier = Modifier,
    onUpdateEblanAction: suspend (
        resId: Int,
        eblanAction: EblanAction,
    ) -> Unit,
    onFinish: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var selectedEblanAction by remember {
        mutableStateOf<EblanAction>(EblanAction.None)
    }

    val eblanActions by remember {
        derivedStateOf {
            listOf(
                R.drawable.adb_24px to EblanAction.None,
                R.drawable.outline_apps_24 to EblanAction.OpenAppDrawer,
                R.drawable.notification_settings_24px to EblanAction.OpenNotificationPanel,
                R.drawable.adb_24px to selectedEblanAction.let { eblanAction ->
                    if (eblanAction is EblanAction.OpenApp) {
                        EblanAction.OpenApp(componentName = eblanAction.componentName)
                    } else {
                        EblanAction.OpenApp(componentName = "app")
                    }
                },
                R.drawable.lock_24px to EblanAction.LockScreen,
                R.drawable.settings_24px to EblanAction.OpenQuickSettings,
                R.drawable.preview_24px to EblanAction.OpenRecents,
            )
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = "Eblan Action")
        }, navigationIcon = {
            IconButton(onClick = onFinish) {
                Icon(
                    imageVector = EblanLauncherIcons.ArrowBack,
                    contentDescription = null,
                )
            }
        })
    }) { paddingValues ->
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            eblanActions.forEach { (resId, eblanAction) ->
                ListItem(
                    modifier = Modifier
                        .clickable {
                            scope.launch {
                                onUpdateEblanAction(
                                    resId,
                                    eblanAction,
                                )
                            }
                        }
                        .fillMaxWidth()
                        .padding(10.dp),
                    headlineContent = {
                        Text(text = eblanAction.getEblanActionSubtitle())
                    },
                    leadingContent = {
                        Icon(
                            imageVector = ImageVector.vectorResource(resId),
                            contentDescription = null,
                        )
                    },
                )
            }
        }
    }
}

private fun EblanAction.getEblanActionSubtitle(): String {
    return when (this) {
        EblanAction.None -> "None"
        is EblanAction.OpenApp -> "Open $componentName"
        EblanAction.OpenAppDrawer -> "Open app drawer"
        EblanAction.OpenNotificationPanel -> "Open notification panel"
        EblanAction.LockScreen -> "Lock screen"
        EblanAction.OpenQuickSettings -> "Open quick settings"
        EblanAction.OpenRecents -> "Open recents"
    }
}
