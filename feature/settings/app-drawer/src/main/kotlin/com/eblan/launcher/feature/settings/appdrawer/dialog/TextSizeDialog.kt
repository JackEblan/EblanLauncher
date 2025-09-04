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
fun TextSizeDialog(
    modifier: Modifier = Modifier,
    textSize: Int,
    onDismissRequest: () -> Unit,
    onUpdateClick: (Int) -> Unit,
) {
    var currentValue by remember { mutableStateOf("$textSize") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface {
            Column(modifier = modifier.fillMaxWidth()) {
                Text(text = "Icon Size")

                TextField(
                    value = currentValue,
                    onValueChange = {
                        currentValue = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Icon size")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

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
                            onUpdateClick(currentValue.toInt().coerceAtLeast(10))

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