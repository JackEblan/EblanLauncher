package com.eblan.launcher.feature.home.component.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons

@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    hasShortcutHostPermission: Boolean,
    onSettings: () -> Unit,
    onEditPage: () -> Unit,
    onWidgets: () -> Unit,
    onShortcuts: () -> Unit,
    onWallpaper: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 2.dp,
        content = {
            Column {
                PopupMenuRow(
                    imageVector = EblanLauncherIcons.Settings,
                    title = "Settings",
                    onClick = onSettings
                )

                Spacer(modifier = Modifier.height(5.dp))

                PopupMenuRow(
                    imageVector = EblanLauncherIcons.Pages,
                    title = "Edit Pages",
                    onClick = onEditPage
                )

                Spacer(modifier = Modifier.height(5.dp))

                PopupMenuRow(
                    imageVector = EblanLauncherIcons.Widgets,
                    title = "Widgets",
                    onClick = onWidgets
                )

                if (hasShortcutHostPermission) {
                    Spacer(modifier = Modifier.height(5.dp))

                    PopupMenuRow(
                        imageVector = EblanLauncherIcons.Shortcut,
                        title = "Shortcuts",
                        onClick = onShortcuts
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                PopupMenuRow(
                    imageVector = EblanLauncherIcons.Image,
                    title = "Wallpaper",
                    onClick = onWallpaper
                )
            }
        },
    )
}

@Composable
private fun PopupMenuRow(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .width(150.dp)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(text = title)
    }
}
