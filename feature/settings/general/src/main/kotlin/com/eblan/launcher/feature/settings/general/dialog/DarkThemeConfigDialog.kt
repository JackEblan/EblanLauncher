package com.eblan.launcher.feature.settings.general.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import com.eblan.launcher.domain.model.DarkThemeConfig

@Composable
fun DarkThemeConfigDialog(
    modifier: Modifier = Modifier,
    darkThemeConfig: DarkThemeConfig,
    onDismissRequest: () -> Unit,
    onUpdateClick: (DarkThemeConfig) -> Unit,
) {
    var selectedDarkThemeConfig by remember { mutableStateOf(darkThemeConfig) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface {
            Column(
                modifier = modifier
                    .selectableGroup()
                    .fillMaxWidth(),
            ) {
                RadioButtonContent(
                    text = "System",
                    selected = selectedDarkThemeConfig == DarkThemeConfig.System,
                    onClick = {
                        selectedDarkThemeConfig = DarkThemeConfig.System
                    },
                )

                RadioButtonContent(
                    text = "Light",
                    selected = selectedDarkThemeConfig == DarkThemeConfig.Light,
                    onClick = {
                        selectedDarkThemeConfig = DarkThemeConfig.Light
                    },
                )

                RadioButtonContent(
                    text = "Dark",
                    selected = selectedDarkThemeConfig == DarkThemeConfig.Dark,
                    onClick = {
                        selectedDarkThemeConfig = DarkThemeConfig.Dark
                    },
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
                            onUpdateClick(selectedDarkThemeConfig)

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
private fun RadioButtonContent(
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
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}