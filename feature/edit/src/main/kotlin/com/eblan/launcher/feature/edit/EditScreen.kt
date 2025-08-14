package com.eblan.launcher.feature.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.edit.dialog.IconSizeDialog
import com.eblan.launcher.feature.edit.dialog.TextColorDialog
import com.eblan.launcher.feature.edit.dialog.TextSizeDialog
import com.eblan.launcher.feature.edit.model.EditUiState

@Composable
fun EditRoute(
    modifier: Modifier = Modifier,
    viewModel: EditViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val editUiState by viewModel.editUiState.collectAsStateWithLifecycle()

    EditScreen(
        modifier = modifier,
        editUiState = editUiState,
        onNavigateUp = onNavigateUp,
        onUpdateGridItem = viewModel::updateGridItem,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    editUiState: EditUiState,
    onNavigateUp: () -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Edit")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = EblanLauncherIcons.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {
            when (editUiState) {
                EditUiState.Loading -> {

                }

                is EditUiState.Success -> {
                    if (editUiState.gridItem != null) {
                        Success(
                            modifier = modifier,
                            gridItem = editUiState.gridItem,
                            onUpdateGridItem = onUpdateGridItem,
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun Success(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    onUpdateGridItem: (GridItem) -> Unit,
) {
    var showIconSizeDialog by remember { mutableStateOf(false) }

    var showTextColorDialog by remember { mutableStateOf(false) }

    var showTextSizeDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(text = "Grid Item", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(5.dp))

        SwitchRow(
            checked = gridItem.override,
            title = "Override",
            subtitle = "Override the Grid Item Settings",
            onCheckedChange = {
                onUpdateGridItem(gridItem.copy(override = it))
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Icon Size",
            subtitle = "${gridItem.gridItemSettings.iconSize}",
            onClick = {
                showIconSizeDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Text Color",
            subtitle = gridItem.gridItemSettings.textColor.getTextColorSubtitle(),
            onClick = {
                showTextColorDialog = true
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingsColumn(
            title = "Text Size",
            subtitle = "${gridItem.gridItemSettings.textSize}",
            onClick = {
                showTextSizeDialog = true
            },
        )

        Spacer(modifier = Modifier.height(5.dp))

        SwitchRow(
            checked = gridItem.gridItemSettings.showLabel,
            title = "Show Label",
            subtitle = "Show the label",
            onCheckedChange = {
                val newGridItemSettings = gridItem.gridItemSettings.copy(showLabel = it)

                onUpdateGridItem(gridItem.copy(gridItemSettings = newGridItemSettings))
            },
        )
    }

    if (showIconSizeDialog) {
        IconSizeDialog(
            iconSize = gridItem.gridItemSettings.iconSize,
            onDismissRequest = {
                showIconSizeDialog = false
            },
            onUpdateClick = { newIconSize ->
                val newGridItemSettings = gridItem.gridItemSettings.copy(iconSize = newIconSize)

                onUpdateGridItem(gridItem.copy(gridItemSettings = newGridItemSettings))
            },
        )
    }

    if (showTextColorDialog) {
        TextColorDialog(
            textColor = gridItem.gridItemSettings.textColor,
            onDismissRequest = {
                showTextColorDialog = false
            },
            onUpdateClick = { newTextColor ->
                val newGridItemSettings = gridItem.gridItemSettings.copy(textColor = newTextColor)

                onUpdateGridItem(gridItem.copy(gridItemSettings = newGridItemSettings))
            },
        )
    }

    if (showTextSizeDialog) {
        TextSizeDialog(
            textSize = gridItem.gridItemSettings.textSize,
            onDismissRequest = {
                showTextSizeDialog = false
            },
            onUpdateClick = { newTextSize ->
                val newGridItemSettings = gridItem.gridItemSettings.copy(textSize = newTextSize)

                onUpdateGridItem(gridItem.copy(gridItemSettings = newGridItemSettings))
            },
        )
    }
}

@Composable
private fun SwitchRow(
    modifier: Modifier = Modifier,
    checked: Boolean,
    title: String,
    subtitle: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingsColumn(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun TextColor.getTextColorSubtitle(): String {
    return when (this) {
        TextColor.System -> "System"
        TextColor.Light -> "Light"
        TextColor.Dark -> "Dark"
    }
}