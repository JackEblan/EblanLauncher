/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.feature.home.screen.pager

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.PagerState
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GlobalAction
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun doGestureActions(
    gestureSettings: GestureSettings,
    swipeUpY: Float,
    swipeDownY: Float,
    screenHeight: Int,
    onStartMainActivity: (String?) -> Unit,
    onPerformGlobalAction: (GlobalAction) -> Unit,
) {
    val swipeThreshold = 100f

    if (swipeUpY < screenHeight - swipeThreshold) {
        handleGestureAction(gestureSettings.swipeUp, onStartMainActivity, onPerformGlobalAction)
    }

    if (swipeDownY < screenHeight - swipeThreshold) {
        handleGestureAction(gestureSettings.swipeDown, onStartMainActivity, onPerformGlobalAction)
    }
}

internal fun resetSwipeOffset(
    scope: CoroutineScope,
    gestureSettings: GestureSettings,
    swipeDownY: Animatable<Float, AnimationVector1D>,
    screenHeight: Int,
    swipeUpY: Animatable<Float, AnimationVector1D>,
) {
    fun animateOffset(
        gestureAction: GestureAction,
        swipeY: Animatable<Float, AnimationVector1D>,
        screenHeight: Int,
    ) {
        scope.launch {
            if (gestureAction is GestureAction.OpenAppDrawer) {
                val targetValue = if (swipeY.value < screenHeight - 200f) {
                    0f
                } else {
                    screenHeight.toFloat()
                }

                swipeY.animateTo(
                    targetValue = targetValue,
                    animationSpec = tween(
                        easing = FastOutSlowInEasing,
                    ),
                )
            } else {
                swipeY.snapTo(screenHeight.toFloat())
            }
        }
    }

    animateOffset(
        gestureAction = gestureSettings.swipeDown,
        swipeY = swipeDownY,
        screenHeight = screenHeight,
    )

    animateOffset(
        gestureAction = gestureSettings.swipeUp,
        swipeY = swipeUpY,
        screenHeight = screenHeight,
    )
}

internal suspend fun handleOnNewIntent(
    gridHorizontalPagerState: PagerState,
    intent: Intent,
    initialPage: Int,
    wallpaperScroll: Boolean,
    wallpaperManagerWrapper: AndroidWallpaperManagerWrapper,
    pageCount: Int,
    infiniteScroll: Boolean,
    windowToken: android.os.IBinder,
) {
    if (intent.action != Intent.ACTION_MAIN &&
        !intent.hasCategory(Intent.CATEGORY_HOME)
    ) {
        return
    }

    val initialPage = if (infiniteScroll) {
        (Int.MAX_VALUE / 2) + initialPage
    } else {
        initialPage
    }

    gridHorizontalPagerState.scrollToPage(initialPage)

    if (wallpaperScroll) {
        val page = calculatePage(
            index = gridHorizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        wallpaperManagerWrapper.setWallpaperOffsetSteps(
            xStep = 1f / (pageCount.toFloat() - 1),
            yStep = 1f,
        )

        wallpaperManagerWrapper.setWallpaperOffsets(
            windowToken = windowToken,
            xOffset = page / (pageCount.toFloat() - 1),
            yOffset = 0f,
        )
    }
}

private fun handleGestureAction(
    gestureAction: GestureAction,
    onStartMainActivity: (String?) -> Unit,
    onPerformGlobalAction: (GlobalAction) -> Unit,
) {
    when (gestureAction) {
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

        GestureAction.None, GestureAction.OpenAppDrawer -> {
            Unit
        }
    }
}

internal suspend fun handleApplyFling(
    offsetY: Animatable<Float, AnimationVector1D>,
    remaining: Float,
    screenHeight: Int,
    onDismiss: () -> Unit = {},
) {
    if (offsetY.value <= 0f && remaining > 10000f) {
        offsetY.animateTo(
            targetValue = screenHeight.toFloat(),
            initialVelocity = remaining,
            animationSpec = tween(
                easing = FastOutSlowInEasing,
            ),
        )

        onDismiss()
    } else if (offsetY.value > 200f) {
        offsetY.animateTo(
            targetValue = screenHeight.toFloat(),
            animationSpec = tween(
                easing = FastOutSlowInEasing,
            ),
        )

        onDismiss()
    } else {
        offsetY.animateTo(
            targetValue = 0f,
            initialVelocity = remaining,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }
}
