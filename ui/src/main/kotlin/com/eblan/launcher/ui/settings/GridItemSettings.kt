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
package com.eblan.launcher.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HorizontalAlignment
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.model.VerticalArrangement
import com.eblan.launcher.ui.dialog.RadioOptionsDialog
import com.eblan.launcher.ui.dialog.SingleTextFieldDialog

@Composable
fun GridItemSettings(
    modifier: Modifier = Modifier,
    gridItemSettings: GridItemSettings,
    onUpdateGridItemSettings: (GridItemSettings) -> Unit,
) {
    var showIconSizeDialog by remember { mutableStateOf(false) }

    var showTextColorDialog by remember { mutableStateOf(false) }

    var showTextSizeDialog by remember { mutableStateOf(false) }

    var showHorizontalAlignment by remember { mutableStateOf(false) }

    var showVerticalArrangement by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(5.dp),
            text = "Grid Item",
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(5.dp))

        SettingsColumn(
            title = "Icon Size",
            subtitle = "${gridItemSettings.iconSize}",
            onClick = {
                showIconSizeDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Text Color",
            subtitle = gridItemSettings.textColor.name,
            onClick = {
                showTextColorDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Text Size",
            subtitle = "${gridItemSettings.textSize}",
            onClick = {
                showTextSizeDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsSwitch(
            checked = gridItemSettings.showLabel,
            title = "Show Label",
            subtitle = "Show label",
            onCheckedChange = { showLabel ->
                onUpdateGridItemSettings(gridItemSettings.copy(showLabel = showLabel))
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsSwitch(
            checked = gridItemSettings.singleLineLabel,
            title = "Single Line Label",
            subtitle = "Show single line label",
            onCheckedChange = { singleLineLabel ->
                onUpdateGridItemSettings(gridItemSettings.copy(singleLineLabel = singleLineLabel))
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Horizontal Alignment",
            subtitle = gridItemSettings.horizontalAlignment.name.replace(
                regex = Regex(pattern = "([a-z])([A-Z])"),
                replacement = "$1 $2",
            ),
            onClick = {
                showHorizontalAlignment = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Vertical Arrangement",
            subtitle = gridItemSettings.verticalArrangement.name,
            onClick = {
                showVerticalArrangement = true
            },
        )
    }

    if (showIconSizeDialog) {
        var value by remember { mutableStateOf("${gridItemSettings.iconSize}") }

        var isError by remember { mutableStateOf(false) }

        SingleTextFieldDialog(
            title = "Icon Size",
            textFieldTitle = "Icon Size",
            value = value,
            isError = isError,
            keyboardType = KeyboardType.Number,
            onValueChange = {
                value = it
            },
            onDismissRequest = {
                showIconSizeDialog = false
            },
            onUpdateClick = {
                try {
                    onUpdateGridItemSettings(gridItemSettings.copy(iconSize = value.toInt()))

                    showIconSizeDialog = false
                } catch (_: NumberFormatException) {
                    isError = true
                }
            },
        )
    }

    if (showTextColorDialog) {
        RadioOptionsDialog(
            title = "Text Color",
            options = TextColor.entries,
            selected = gridItemSettings.textColor,
            label = {
                it.name
            },
            onDismissRequest = {
                showTextColorDialog = false
            },
            onUpdateClick = { textColor ->
                onUpdateGridItemSettings(gridItemSettings.copy(textColor = textColor))

                showTextColorDialog = false
            },
        )
    }

    if (showTextSizeDialog) {
        var value by remember { mutableStateOf("${gridItemSettings.textSize}") }

        var isError by remember { mutableStateOf(false) }

        SingleTextFieldDialog(
            title = "Text Size",
            textFieldTitle = "Text Size",
            value = value,
            isError = isError,
            keyboardType = KeyboardType.Number,
            onValueChange = {
                value = it
            },
            onDismissRequest = {
                showTextSizeDialog = false
            },
            onUpdateClick = {
                try {
                    onUpdateGridItemSettings(gridItemSettings.copy(textSize = value.toInt()))

                    showTextSizeDialog = false
                } catch (_: NumberFormatException) {
                    isError = true
                }
            },
        )
    }

    if (showHorizontalAlignment) {
        RadioOptionsDialog(
            title = "Horizontal Alignment",
            options = HorizontalAlignment.entries,
            selected = gridItemSettings.horizontalAlignment,
            label = {
                it.name.replace(
                    regex = Regex(pattern = "([a-z])([A-Z])"),
                    replacement = "$1 $2",
                )
            },
            onDismissRequest = {
                showHorizontalAlignment = false
            },
            onUpdateClick = { horizontalAlignment ->
                onUpdateGridItemSettings(gridItemSettings.copy(horizontalAlignment = horizontalAlignment))

                showHorizontalAlignment = false
            },
        )
    }

    if (showVerticalArrangement) {
        RadioOptionsDialog(
            title = "Vertical Arrangement",
            options = VerticalArrangement.entries,
            selected = gridItemSettings.verticalArrangement,
            label = {
                it.name
            },
            onDismissRequest = {
                showVerticalArrangement = false
            },
            onUpdateClick = { verticalArrangement ->
                onUpdateGridItemSettings(gridItemSettings.copy(verticalArrangement = verticalArrangement))

                showVerticalArrangement = false
            },
        )
    }
}
