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
package com.eblan.launcher.feature.editgriditem.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.component.EblanDialogContainer

@Composable
internal fun IconPackInfoFilesDialog(
    modifier: Modifier = Modifier,
    iconPackInfoFiles: List<String>,
    iconPackInfoLabel: String?,
    onDismissRequest: () -> Unit,
    onUpdateIconPackInfoFile: (String) -> Unit,
) {
    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(modifier = modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = iconPackInfoLabel.toString(),
                style = MaterialTheme.typography.titleLarge,
            )

            LazyVerticalGrid(
                modifier = Modifier.weight(
                    weight = 1f,
                    fill = false,
                ),
                columns = GridCells.Fixed(4),
            ) {
                items(iconPackInfoFiles) { iconPackInfoFile ->
                    AsyncImage(
                        model = iconPackInfoFile,
                        contentDescription = null,
                        modifier = Modifier
                            .clickable {
                                onUpdateIconPackInfoFile(iconPackInfoFile)
                            }
                            .size(40.dp)
                            .padding(2.dp),
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
