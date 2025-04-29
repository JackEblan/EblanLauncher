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
fun DockHeightDialog(
    modifier: Modifier = Modifier,
    dockHeight: Int,
    onDismissRequest: () -> Unit,
    onUpdateClick: (Int) -> Unit,
) {
    var currentDockHeight by remember { mutableStateOf("$dockHeight") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier.fillMaxWidth(),
        ) {
            Column(modifier = modifier.fillMaxWidth()) {
                Text(text = "Dock Height")

                TextField(
                    value = currentDockHeight,
                    onValueChange = {
                        currentDockHeight = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Dock height")
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
                            onUpdateClick(currentDockHeight.toInt().coerceAtLeast(300))

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