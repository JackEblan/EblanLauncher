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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialogContainer

@Composable
fun SyncDataDialog(
    modifier: Modifier = Modifier,
    syncData: Boolean,
    isDataSyncing: Boolean,
    onUpdateSyncData: (Boolean) -> Unit,
    onSyncData: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(
                text = "Warning",
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Disabling background sync helps save a bit of memory and keeps things lighter, but it also means Eblan Launcher won’t automatically update your apps, widgets, or shortcuts.\n" + "Your app drawer might show outdated icons, missing widgets, or shortcuts that no longer work.\n" + "\n" + "Only turn this off if you rarely change your apps and don’t mind syncing manually from Settings when needed.",
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isDataSyncing) {
                Row {
                    CircularProgressIndicator(modifier = Modifier.height(IntrinsicSize.Max))

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Syncing Data... Please don't close this dialog",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !syncData && !isDataSyncing,
                onClick = onSyncData,
            ) {
                Text(text = "Sync Data")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row {
                Checkbox(
                    checked = syncData,
                    onCheckedChange = onUpdateSyncData,
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(text = "Sync data", modifier = Modifier.align(Alignment.CenterVertically))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
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
