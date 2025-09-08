package com.eblan.launcher.feature.settings.home.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
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
        Surface(shape = RoundedCornerShape(size = 10.dp)) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Text(text = "Grid")

                Spacer(modifier = Modifier.height(10.dp))

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

                    Spacer(modifier = Modifier.width(5.dp))

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
                                    currentRows.toInt().coerceAtLeast(1),
                                    currentColumns.toInt().coerceAtLeast(1),
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
}