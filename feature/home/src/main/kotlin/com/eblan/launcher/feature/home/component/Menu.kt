package com.eblan.launcher.feature.home.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Menu(
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
    ) {
        Row {
            IconButton(
                onClick = onEdit,
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }

            Spacer(modifier = Modifier.width(5.dp))

            IconButton(
                onClick = {

                },
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null)
            }
        }
    }
}