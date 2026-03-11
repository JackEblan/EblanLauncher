package com.eblan.launcher.feature.home.screen.pager

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.OverlayImage(
    modifier: Modifier = Modifier,
    drag: Drag,
    overlayImageBitmap: ImageBitmap?,
    overlayIntOffset: IntOffset,
    overlayIntSize: IntSize,
    sharedElementKey: SharedElementKey?,
    onResetOverlay: () -> Unit,
) {
    if (overlayImageBitmap == null || sharedElementKey == null) return

    val density = LocalDensity.current

    val size = with(density) {
        DpSize(width = overlayIntSize.width.toDp(), height = overlayIntSize.height.toDp())
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Cancel || drag == Drag.End) {
            onResetOverlay()
        }
    }

    Image(
        modifier = modifier
            .offset {
                overlayIntOffset
            }
            .size(size)
            .sharedElementWithCallerManagedVisibility(
                rememberSharedContentState(key = sharedElementKey),
                visible = drag == Drag.Start || drag == Drag.Dragging,
            ),
        bitmap = overlayImageBitmap,
        contentDescription = null,
    )
}