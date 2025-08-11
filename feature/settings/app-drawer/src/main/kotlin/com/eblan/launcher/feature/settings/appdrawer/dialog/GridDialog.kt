package com.eblan.launcher.feature.settings.appdrawer.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog

@Composable
fun GridDialog(
    modifier: Modifier = Modifier,
    columns: Int,
    rowsHeight: Int,
    onDismissRequest: () -> Unit,
    onUpdateClick: (
        columns: Int,
        rowsHeight: Int,
    ) -> Unit,
) {
    var currentColumns by remember { mutableStateOf("$columns") }

    var currentRowsHeight by remember { mutableStateOf("$rowsHeight") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface {
            Column(modifier = modifier.fillMaxWidth()) {
                Text(text = "Grid")

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = currentColumns,
                        onValueChange = {
                            currentColumns = it
                        },
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(text = "Columns")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )

                    TextField(
                        value = currentRowsHeight,
                        onValueChange = {
                            currentRowsHeight = it
                        },
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(text = "Row Height")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }

                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                    ) {
                        Text(text = "Cancel")
                    }
                    TextButton(
                        onClick = {
                            onUpdateClick(
                                currentColumns.toInt().coerceAtLeast(1),
                                currentRowsHeight.toInt().coerceAtLeast(100),
                            )

                            onDismissRequest()
                        },
                    ) {
                        Text(text = "Update")
                    }
                }
            }
        }
    }
}