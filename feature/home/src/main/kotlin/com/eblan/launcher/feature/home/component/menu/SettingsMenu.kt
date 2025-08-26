package com.eblan.launcher.feature.home.component.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            Column(modifier = Modifier.padding(5.dp)) {
                Row(modifier = Modifier.clickable(onClick = onSettings)) {
                    Icon(
                        imageVector = EblanLauncherIcons.Settings,
                        contentDescription = null,
                    )

                    Text(text = "Settings")
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(modifier = Modifier.clickable(onClick = onEditPage)) {
                    Icon(
                        imageVector = EblanLauncherIcons.Pages,
                        contentDescription = null,
                    )

                    Text(text = "Edit Pages")
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(modifier = Modifier.clickable(onClick = onWidgets)) {
                    Icon(
                        imageVector = EblanLauncherIcons.Widgets,
                        contentDescription = null,
                    )

                    Text(text = "Widgets")
                }

                if (hasShortcutHostPermission) {
                    Spacer(modifier = Modifier.height(5.dp))

                    Row(modifier = Modifier.clickable(onClick = onShortcuts)) {
                        Icon(
                            imageVector = EblanLauncherIcons.Shortcut,
                            contentDescription = null,
                        )

                        Text(text = "Shortcuts")
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(modifier = Modifier.clickable(onClick = onWallpaper)) {
                    Icon(
                        imageVector = EblanLauncherIcons.Image,
                        contentDescription = null,
                    )

                    Text(text = "Wallpaper")
                }
            }
        },
    )
}
