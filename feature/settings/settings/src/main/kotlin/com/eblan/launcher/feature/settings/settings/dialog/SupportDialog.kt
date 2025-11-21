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
package com.eblan.launcher.feature.settings.settings.dialog

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.eblan.launcher.designsystem.component.EblanDialogContainer

@Composable
internal fun SupportDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current

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
                text = "Support Development",
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                modifier = Modifier.padding(15.dp),
                text = "Thank you for using Eblan Launcher Alpha! I’ve been building this project since January 2025, releasing weekly updates. It’s my most complex project yet, and I pour my heart into it. If you enjoy it, you can support development with a donation or a star on GitHub.",
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        end = 10.dp,
                        bottom = 10.dp,
                    ),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Later")
                }

                Spacer(modifier = Modifier.width(5.dp))

                Button(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://github.com/JackEblan/EblanLauncher".toUri(),
                            ),
                        )

                        onDismissRequest()
                    },
                ) {
                    Text(text = "Okay")
                }
            }
        }
    }
}
