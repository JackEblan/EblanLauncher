package com.eblan.launcher.feature.home.component.menu

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons

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
