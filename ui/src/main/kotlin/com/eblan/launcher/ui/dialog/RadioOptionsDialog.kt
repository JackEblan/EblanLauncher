package com.eblan.launcher.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialogContainer
import com.eblan.launcher.designsystem.component.EblanRadioButton

@Composable
fun <T> RadioOptionsDialog(
    title: String,
    modifier: Modifier = Modifier,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onDismissRequest: () -> Unit,
    onUpdateClick: (T) -> Unit,
) {
    var selectedOption by remember { mutableStateOf(selected) }

    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .selectableGroup()
                .fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = title, style = MaterialTheme.typography.titleLarge
            )

            options.forEach { option ->
                EblanRadioButton(
                    text = label(option),
                    selected = selectedOption == option,
                    onClick = { selectedOption = option }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Cancel")
                }
                TextButton(
                    onClick = {
                        onUpdateClick(selectedOption)
                        onDismissRequest()
                    },
                ) {
                    Text(text = "Update")
                }
            }
        }
    }
}