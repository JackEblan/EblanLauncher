package com.eblan.launcher.feature.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons

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
            Row {
                IconButton(
                    onClick = onEdit,
                ) {
                    Icon(imageVector = EblanLauncherIcons.Edit, contentDescription = null)
                }

                if (showResize) {
                    IconButton(
                        onClick = onResize,
                    ) {
                        Icon(imageVector = EblanLauncherIcons.Resize, contentDescription = null)
                    }
                }

                IconButton(
                    onClick = {

                    },
                ) {
                    Icon(imageVector = EblanLauncherIcons.Delete, contentDescription = null)
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
            Row {
                IconButton(
                    onClick = onEdit,
                ) {
                    Icon(imageVector = EblanLauncherIcons.Edit, contentDescription = null)
                }

                if (showResize) {
                    IconButton(
                        onClick = onResize,
                    ) {
                        Icon(imageVector = EblanLauncherIcons.Resize, contentDescription = null)
                    }
                }

                IconButton(
                    onClick = {

                    },
                ) {
                    Icon(imageVector = EblanLauncherIcons.Delete, contentDescription = null)
                }
            }
        },
    )
}

@Composable
fun ApplicationInfoMenu(
    modifier: Modifier = Modifier,
    onApplicationInfo: () -> Unit,
    onWidgets: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Row {
                IconButton(
                    onClick = onApplicationInfo,
                ) {
                    Icon(imageVector = EblanLauncherIcons.Edit, contentDescription = null)
                }

                IconButton(
                    onClick = onWidgets,
                ) {
                    Icon(imageVector = EblanLauncherIcons.Widgets, contentDescription = null)
                }

            }
        },
    )
}

@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    onSettings: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Column {
                Row(modifier = Modifier.clickable(onClick = onSettings)) {
                    Icon(
                        imageVector = EblanLauncherIcons.Settings,
                        contentDescription = null,
                    )

                    Text(text = "Settings")
                }
            }
        },
    )
}
