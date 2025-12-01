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
package com.eblan.launcher.feature.settings.general.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.component.EblanDialogContainer
import com.eblan.launcher.domain.model.PackageManagerIconPackInfo

@Composable
internal fun ImportIconPackDialog(
    modifier: Modifier = Modifier,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    onDismissRequest: () -> Unit,
    onUpdateIconPack: (
        packageName: String,
        label: String,
    ) -> Unit,
) {
    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier.fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = "Import Icon Pack",
                style = MaterialTheme.typography.titleLarge,
            )

            LazyColumn(
                modifier = Modifier.weight(
                    weight = 1f,
                    fill = false,
                ),
            ) {
                items(packageManagerIconPackInfos) { packageManagerIconPackInfo ->
                    ListItem(
                        headlineContent = { Text(text = packageManagerIconPackInfo.label) },
                        leadingContent = {
                            AsyncImage(
                                model = packageManagerIconPackInfo.icon,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                            )
                        },
                        modifier = Modifier
                            .clickable {
                                onUpdateIconPack(
                                    packageManagerIconPackInfo.packageName,
                                    packageManagerIconPackInfo.label,
                                )
                            }
                            .fillMaxWidth()
                            .padding(10.dp),
                    )
                }
            }

            TextButton(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(10.dp),
                onClick = onDismissRequest,
            ) {
                Text(text = "Cancel")
            }
        }
    }
}
