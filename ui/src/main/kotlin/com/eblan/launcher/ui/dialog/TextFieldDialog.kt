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
package com.eblan.launcher.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialogContainer

@Composable
fun SingleNumberTextFieldDialog(
    modifier: Modifier = Modifier,
    title: String,
    value: Int,
    onDismissRequest: () -> Unit,
    onUpdateClick: (Int) -> Unit,
) {
    var currentValue by remember { mutableStateOf("$value") }

    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = currentValue,
                onValueChange = {
                    currentValue = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = title)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onDismissRequest,
                ) {
                    Text(text = "Cancel")
                }
                TextButton(
                    onClick = {
                        try {
                            onUpdateClick(currentValue.toInt().coerceAtLeast(10))
                        } catch (_: NumberFormatException) {
                        } finally {
                            onDismissRequest()
                        }
                    },
                ) {
                    Text(text = "Update")
                }
            }
        }
    }
}

@Composable
fun TwoNumberTextFieldsDialog(
    modifier: Modifier = Modifier,
    title: String,
    firstTextFieldTitle: String,
    secondTextFieldTitle: String,
    firstTextFieldValue: Int,
    secondTextFieldValue: Int,
    onDismissRequest: () -> Unit,
    onUpdateClick: (
        firstTextFieldValue: Int,
        secondTextFieldValue: Int,
    ) -> Unit,
) {
    var currentFirstTextFieldValue by remember { mutableStateOf("$firstTextFieldValue") }

    var currentSecondTextFieldValue by remember { mutableStateOf("$secondTextFieldValue") }

    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = currentFirstTextFieldValue,
                    onValueChange = {
                        currentFirstTextFieldValue = it
                    },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(text = firstTextFieldTitle)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                Spacer(modifier = Modifier.width(5.dp))

                TextField(
                    value = currentSecondTextFieldValue,
                    onValueChange = {
                        currentSecondTextFieldValue = it
                    },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(text = secondTextFieldTitle)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onDismissRequest,
                ) {
                    Text(text = "Cancel")
                }
                TextButton(
                    onClick = {
                        try {
                            onUpdateClick(
                                currentFirstTextFieldValue.toInt().coerceAtLeast(1),
                                currentSecondTextFieldValue.toInt().coerceAtLeast(1),
                            )
                        } catch (_: NumberFormatException) {
                        } finally {
                            onDismissRequest()
                        }
                    },
                ) {
                    Text(text = "Update")
                }
            }
        }
    }
}
