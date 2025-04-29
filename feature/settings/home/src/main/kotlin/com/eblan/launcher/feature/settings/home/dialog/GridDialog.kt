package com.eblan.launcher.feature.settings.home.dialog

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
    rows: Int,
    columns: Int,
    onDismissRequest: () -> Unit,
    onUpdateClick: (
        rows: Int,
        columns: Int,
    ) -> Unit,
) {
    var currentRows by remember { mutableStateOf("$rows") }

    var currentColumns by remember { mutableStateOf("$columns") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier.fillMaxWidth(),
        ) {
            Column(modifier = modifier.fillMaxWidth()) {
                Text(text = "Grid")

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = currentRows,
                        onValueChange = {
                            currentRows = it
                        },
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(text = "Rows")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )

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
                                currentRows.toInt().coerceAtLeast(1),
                                currentColumns.toInt().coerceAtLeast(1),
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