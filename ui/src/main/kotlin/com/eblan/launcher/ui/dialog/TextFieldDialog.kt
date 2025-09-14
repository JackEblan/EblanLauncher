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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialogContainer

@Composable
fun SingleTextFieldDialog(
    modifier: Modifier = Modifier,
    title: String,
    textFieldTitle: String,
    value: String,
    isError: Boolean,
    keyboardType: KeyboardType,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onUpdateClick: () -> Unit,
) {
    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = textFieldTitle)
                },
                supportingText = {
                    if (isError) {
                        Text(text = "$textFieldTitle is not valid")
                    }
                },
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = singleLine,
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
                    onClick = onUpdateClick,
                ) {
                    Text(text = "Update")
                }
            }
        }
    }
}

@Composable
fun TwoTextFieldsDialog(
    modifier: Modifier = Modifier,
    title: String,
    firstTextFieldTitle: String,
    secondTextFieldTitle: String,
    firstTextFieldValue: String,
    secondTextFieldValue: String,
    firstTextFieldIsError: Boolean,
    secondTextFieldIsError: Boolean,
    keyboardType: KeyboardType,
    singleLine: Boolean = true,
    onFirstValueChange: (String) -> Unit,
    onSecondValueChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onUpdateClick: () -> Unit,
) {
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
                    value = firstTextFieldValue,
                    onValueChange = onFirstValueChange,
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(text = firstTextFieldTitle)
                    },
                    supportingText = {
                        if (firstTextFieldIsError) {
                            Text(text = "$firstTextFieldTitle is not valid")
                        }
                    },
                    isError = firstTextFieldIsError,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = singleLine,
                )

                Spacer(modifier = Modifier.width(5.dp))

                TextField(
                    value = secondTextFieldValue,
                    onValueChange = onSecondValueChange,
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(text = secondTextFieldTitle)
                    },
                    supportingText = {
                        if (secondTextFieldIsError) {
                            Text(text = "$secondTextFieldTitle is not valid")
                        }
                    },
                    isError = secondTextFieldIsError,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = singleLine,
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
                    onClick = onUpdateClick,
                ) {
                    Text(text = "Update")
                }
            }
        }
    }
}
