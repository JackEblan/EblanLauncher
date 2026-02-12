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
package com.eblan.launcher.feature.settings.experimental.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialogContainer
import com.eblan.launcher.ui.settings.SettingsSwitch

@Composable
internal fun SyncDataDialog(
    modifier: Modifier = Modifier,
    syncData: Boolean,
    onUpdateSyncData: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier.padding(
                    start = 15.dp,
                    top = 10.dp,
                ),
                text = "Warning",
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                modifier = Modifier.padding(15.dp),
                text = "Disabling background sync helps save a bit of memory and keeps things lighter, but it also means Yagni Launcher won’t automatically update your apps, widgets, or shortcuts.\n" +
                    "Your app drawer might show outdated icons, missing widgets, or shortcuts that no longer work.",
            )

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
            ) {
                SettingsSwitch(
                    checked = syncData,
                    title = "Sync Data",
                    subtitle = "Keep data up to date",
                    onCheckedChange = onUpdateSyncData,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onDismissRequest,
                ) {
                    Text(text = "Okay")
                }
            }
        }
    }
}
