package com.eblan.launcher.feature.home.component.gesture

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.feature.home.util.handleEblanAction
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun Modifier.swipeGestures(
    swipeUp: EblanAction,
    swipeDown: EblanAction,
    onOpenAppDrawer: () -> Unit,
): Modifier {
    val context = LocalContext.current

    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    val launcherApps = LocalLauncherApps.current

    return if (swipeUp.eblanActionType != EblanActionType.None ||
        swipeDown.eblanActionType != EblanActionType.None
    ) {
        val swipeY = remember { Animatable(0f) }

        val maxSwipeY = with(density) {
            40.dp.roundToPx()
        }

        pointerInput(key1 = Unit) {
            detectVerticalDragGestures(
                onDragStart = {
                    scope.launch {
                        swipeY.snapTo(0f)
                    }
                },
                onVerticalDrag = { _, dragAmount ->
                    scope.launch {
                        swipeY.snapTo(swipeY.value + dragAmount)
                    }
                },
                onDragCancel = {
                    scope.launch {
                        swipeY.animateTo(0f)
                    }
                },
                onDragEnd = {
                    scope.launch {
                        when {
                            swipeY.value <= -maxSwipeY -> {
                                swipeY.animateTo(0f)

                                handleEblanAction(
                                    eblanAction = swipeUp,
                                    launcherApps = launcherApps,
                                    context = context,
                                    onOpenAppDrawer = onOpenAppDrawer,
                                )
                            }

                            swipeY.value >= maxSwipeY -> {
                                swipeY.animateTo(0f)

                                handleEblanAction(
                                    eblanAction = swipeDown,
                                    launcherApps = launcherApps,
                                    context = context,
                                    onOpenAppDrawer = onOpenAppDrawer,
                                )
                            }
                        }
                    }
                },
            )
        }.offset {
            IntOffset(
                x = 0,
                y = swipeY.value.roundToInt().coerceIn(-maxSwipeY..maxSwipeY),
            )
        }
    } else {
        this
    }
}

internal fun onDoubleTap(
    doubleTap: EblanAction,
    launcherApps: AndroidLauncherAppsWrapper,
    context: Context,
    onOpenAppDrawer: () -> Unit,
): ((Offset) -> Unit)? = if (doubleTap.eblanActionType != EblanActionType.None) {
    {
        handleEblanAction(
            eblanAction = doubleTap,
            launcherApps = launcherApps,
            context = context,
            onOpenAppDrawer = onOpenAppDrawer,
        )
    }
} else {
    null
}