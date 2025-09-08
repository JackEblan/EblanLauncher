package com.eblan.launcher.feature.settings.home.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun IconSizeDialog(
    modifier: Modifier = Modifier,
    iconSize: Int,
    onDismissRequest: () -> Unit,
    onUpdateClick: (Int) -> Unit,
) {
    var currentValue by remember { mutableStateOf("$iconSize") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = RoundedCornerShape(size = 10.dp)) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Text(text = "Icon Size")

                Spacer(modifier = Modifier.height(10.dp))

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
}