package com.eblan.launcher.ui.dialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

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

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = RoundedCornerShape(size = 10.dp)) {
            Column(
                modifier = modifier
                    .selectableGroup()
                    .fillMaxWidth()
                    .padding(10.dp),
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)

                options.forEach { option ->
                    RadioButtonRow(
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
}

@Composable
private fun RadioButtonRow(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}