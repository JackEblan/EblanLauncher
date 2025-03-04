package com.eblan.launcher.feature.home.component.menu

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MenuOverlay(
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onResize: () -> Unit,
) {
    Row(modifier = modifier) {
        IconButton(
            onClick = onEdit,
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
        }

        IconButton(
            onClick = {

            },
        ) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
        }

        IconButton(
            onClick = onResize,
        ) {
            Icon(imageVector = Icons.Default.Android, contentDescription = null)
        }
    }
}
