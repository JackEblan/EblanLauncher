package com.eblan.launcher.feature.home.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ApplicationInfoGridItemMenu(
    modifier: Modifier = Modifier,
    showResize: Boolean,
    onEdit: () -> Unit,
    onResize: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Row(modifier = modifier) {
                IconButton(
                    onClick = onEdit,
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                }

                if (showResize) {
                    IconButton(
                        onClick = onResize,
                    ) {
                        Icon(imageVector = Icons.Default.Android, contentDescription = null)
                    }
                }

                IconButton(
                    onClick = {

                    },
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }
        },
    )
}

@Composable
fun WidgetGridItemMenu(
    modifier: Modifier = Modifier,
    showResize: Boolean,
    onEdit: () -> Unit,
    onResize: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Row(modifier = modifier) {
                IconButton(
                    onClick = onEdit,
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                }

                if (showResize) {
                    IconButton(
                        onClick = onResize,
                    ) {
                        Icon(imageVector = Icons.Default.Android, contentDescription = null)
                    }
                }

                IconButton(
                    onClick = {

                    },
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }
        },
    )
}

@Composable
fun ApplicationInfoMenu(
    modifier: Modifier = Modifier,
    onApplicationInfo: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Row(modifier = modifier) {
                IconButton(
                    onClick = onApplicationInfo,
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                }
            }
        },
    )
}
