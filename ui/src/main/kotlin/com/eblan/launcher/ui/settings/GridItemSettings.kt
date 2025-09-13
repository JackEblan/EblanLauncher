package com.eblan.launcher.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.GridItemSettings

@Composable
fun GridItemSettings(
    gridItemSettings: GridItemSettings,
    onIconSizeClick: () -> Unit,
    onTextColorClick: () -> Unit,
    onTextSizeClick: () -> Unit,
    onUpdateShowLabel: (Boolean) -> Unit,
    onUpdateSingleLineLabel: (Boolean) -> Unit,
) {
    Text(
        modifier = Modifier.padding(5.dp),
        text = "Grid Item",
        style = MaterialTheme.typography.bodySmall,
    )

    Spacer(modifier = Modifier.height(5.dp))

    SettingsColumn(
        title = "Icon Size",
        subtitle = "${gridItemSettings.iconSize}",
        onClick = onIconSizeClick,
    )

    Spacer(modifier = Modifier.height(10.dp))

    SettingsColumn(
        title = "Text Color",
        subtitle = gridItemSettings.textColor.name,
        onClick = onTextColorClick,
    )

    Spacer(modifier = Modifier.height(10.dp))

    SettingsColumn(
        title = "Text Size",
        subtitle = "${gridItemSettings.textSize}",
        onClick = onTextSizeClick,
    )

    Spacer(modifier = Modifier.height(10.dp))

    SettingsSwitch(
        checked = gridItemSettings.showLabel,
        title = "Show Label",
        subtitle = "Show label",
        onCheckedChange = onUpdateShowLabel,
    )

    Spacer(modifier = Modifier.height(10.dp))

    SettingsSwitch(
        checked = gridItemSettings.singleLineLabel,
        title = "Single Line Label",
        subtitle = "Show single line label",
        onCheckedChange = onUpdateSingleLineLabel,
    )
}