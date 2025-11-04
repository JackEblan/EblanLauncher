package com.eblan.launcher.feature.home.screen.application

import android.graphics.Rect
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoMenu
import com.eblan.launcher.feature.home.component.menu.MenuPositionProvider
import com.eblan.launcher.ui.local.LocalLauncherApps

@Composable
fun PopupApplicationInfoMenu(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    popupMenuIntOffset: IntOffset,
    gridItem: GridItem?,
    popupMenuIntSize: IntSize,
    onEditApplicationInfo: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val applicationInfo = gridItem?.data as? GridItemData.ApplicationInfo ?: return

    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val x = popupMenuIntOffset.x - leftPadding

    val y = popupMenuIntOffset.y - topPadding

    Popup(
        popupPositionProvider = MenuPositionProvider(
            x = x,
            y = y,
            width = popupMenuIntSize.width,
            height = popupMenuIntSize.height,
        ),
        onDismissRequest = onDismissRequest,
        content = {
            ApplicationInfoMenu(
                modifier = modifier,
                onApplicationInfo = {
                    launcherApps.startAppDetailsActivity(
                        serialNumber = applicationInfo.serialNumber,
                        componentName = applicationInfo.componentName,
                        sourceBounds = Rect(
                            x,
                            y,
                            x + popupMenuIntSize.width,
                            y + popupMenuIntSize.height,
                        ),
                    )

                    onDismissRequest()
                },
                onEdit = {
                    onEditApplicationInfo(
                        applicationInfo.serialNumber,
                        applicationInfo.packageName
                    )

                    onDismissRequest()
                }
            )
        },
    )
}