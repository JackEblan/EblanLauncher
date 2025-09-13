package com.eblan.launcher.feature.home.screen.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GlobalAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun doGestureActions(
    gestureSettings: GestureSettings,
    swipeUpY: Float,
    swipeDownY: Float,
    screenHeight: Int,
    onStartMainActivity: (String?) -> Unit,
    onPerformGlobalAction: (GlobalAction) -> Unit,
) {
    val swipeThreshold = 100f

    if (swipeUpY < screenHeight - swipeThreshold) {
        when (val gestureAction = gestureSettings.swipeUp) {
            is GestureAction.OpenApp -> {
                onStartMainActivity(gestureAction.componentName)
            }

            GestureAction.OpenNotificationPanel -> {
                onPerformGlobalAction(GlobalAction.Notifications)
            }

            GestureAction.LockScreen -> {
                onPerformGlobalAction(GlobalAction.LockScreen)
            }

            GestureAction.OpenQuickSettings -> {
                onPerformGlobalAction(GlobalAction.QuickSettings)
            }

            GestureAction.OpenRecents -> {
                onPerformGlobalAction(GlobalAction.Recents)
            }

            GestureAction.None, GestureAction.OpenAppDrawer -> Unit
        }
    }

    if (swipeDownY < screenHeight - swipeThreshold) {
        when (val gestureAction = gestureSettings.swipeDown) {
            is GestureAction.OpenApp -> {
                onStartMainActivity(gestureAction.componentName)
            }

            GestureAction.OpenNotificationPanel -> {
                onPerformGlobalAction(GlobalAction.Notifications)
            }

            GestureAction.LockScreen -> {
                onPerformGlobalAction(GlobalAction.LockScreen)
            }

            GestureAction.OpenQuickSettings -> {
                onPerformGlobalAction(GlobalAction.QuickSettings)
            }

            GestureAction.OpenRecents -> {
                onPerformGlobalAction(GlobalAction.Recents)
            }

            GestureAction.None, GestureAction.OpenAppDrawer -> Unit
        }
    }
}

fun resetSwipeOffset(
    scope: CoroutineScope,
    gestureSettings: GestureSettings,
    swipeDownY: Animatable<Float, AnimationVector1D>,
    screenHeight: Int,
    swipeUpY: Animatable<Float, AnimationVector1D>,
) {
    val swipeThreshold = screenHeight - 200f

    scope.launch {
        if (gestureSettings.swipeDown is GestureAction.OpenAppDrawer) {
            val swipeDownYTarget = if (swipeDownY.value < swipeThreshold) {
                0f
            } else {
                screenHeight.toFloat()
            }

            swipeDownY.animateTo(swipeDownYTarget)
        } else {
            swipeDownY.snapTo(screenHeight.toFloat())
        }
    }

    scope.launch {
        if (gestureSettings.swipeUp is GestureAction.OpenAppDrawer) {
            val swipeUpYTarget = if (swipeUpY.value < swipeThreshold) {
                0f
            } else {
                screenHeight.toFloat()
            }
            swipeUpY.animateTo(swipeUpYTarget)
        } else {
            swipeUpY.snapTo(screenHeight.toFloat())
        }
    }
}