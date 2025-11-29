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

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.IBinder
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.PagerState
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GlobalAction
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

internal fun doEblanActions(
    gestureSettings: GestureSettings,
    swipeUpY: Float,
    swipeDownY: Float,
    screenHeight: Int,
    onStartMainActivity: (String) -> Unit,
    onPerformGlobalAction: (GlobalAction) -> Unit,
) {
    fun handleEblanAction(
        eblanAction: EblanAction,
        onStartMainActivity: (String) -> Unit,
        onPerformGlobalAction: (GlobalAction) -> Unit,
    ) {
        when (eblanAction) {
            is EblanAction.OpenApp -> {
                onStartMainActivity(eblanAction.componentName)
            }

            EblanAction.OpenNotificationPanel -> {
                onPerformGlobalAction(GlobalAction.Notifications)
            }

            EblanAction.LockScreen -> {
                onPerformGlobalAction(GlobalAction.LockScreen)
            }

            EblanAction.OpenQuickSettings -> {
                onPerformGlobalAction(GlobalAction.QuickSettings)
            }

            EblanAction.OpenRecents -> {
                onPerformGlobalAction(GlobalAction.Recents)
            }

            EblanAction.OpenAppDrawer, EblanAction.None -> Unit
        }
    }

    val swipeThreshold = 100f

    if (swipeUpY < screenHeight - swipeThreshold) {
        handleEblanAction(
            eblanAction = gestureSettings.swipeUp,
            onStartMainActivity = onStartMainActivity,
            onPerformGlobalAction = onPerformGlobalAction,
        )
    }

    if (swipeDownY < screenHeight - swipeThreshold) {
        handleEblanAction(
            eblanAction = gestureSettings.swipeDown,
            onStartMainActivity = onStartMainActivity,
            onPerformGlobalAction = onPerformGlobalAction,
        )
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
        eblanAction: EblanAction,
        swipeY: Animatable<Float, AnimationVector1D>,
    ) {
        scope.launch {
            if (eblanAction is EblanAction.OpenAppDrawer) {
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
        eblanAction = gestureSettings.swipeDown,
        swipeY = swipeDownY,
    )

    animateOffset(
        eblanAction = gestureSettings.swipeUp,
        swipeY = swipeUpY,
    )
}

internal suspend fun handleActionMainIntent(
    gridHorizontalPagerState: PagerState,
    intent: Intent,
    initialPage: Int,
    wallpaperScroll: Boolean,
    wallpaperManagerWrapper: AndroidWallpaperManagerWrapper,
    pageCount: Int,
    infiniteScroll: Boolean,
    windowToken: IBinder,
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

internal fun handleEblanActionIntent(
    intent: Intent,
    onStartMainActivity: (String) -> Unit,
    onPerformGlobalAction: (GlobalAction) -> Unit,
    onOpenAppDrawer: () -> Unit,
) {
    if (intent.action != EblanAction.ACTION) return

    val eblanAction = intent.getStringExtra(EblanAction.NAME)?.let { eblanAction ->
        Json.decodeFromString<EblanAction>(eblanAction)
    }

    when (eblanAction) {
        is EblanAction.OpenApp -> {
            onStartMainActivity(eblanAction.componentName)
        }

        EblanAction.OpenNotificationPanel -> {
            onPerformGlobalAction(GlobalAction.Notifications)
        }

        EblanAction.LockScreen -> {
            onPerformGlobalAction(GlobalAction.LockScreen)
        }

        EblanAction.OpenQuickSettings -> {
            onPerformGlobalAction(GlobalAction.QuickSettings)
        }

        EblanAction.OpenRecents -> {
            onPerformGlobalAction(GlobalAction.Recents)
        }

        EblanAction.OpenAppDrawer -> {
            onOpenAppDrawer()
        }

        EblanAction.None, null -> Unit
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

internal fun handleHasDoubleTap(
    hasDoubleTap: Boolean,
    gestureSettings: GestureSettings,
    launcherApps: AndroidLauncherAppsWrapper,
    context: Context,
    onShowAppDrawer: () -> Unit,
) {
    if (!hasDoubleTap) return

    when (val eblanAction = gestureSettings.doubleTap) {
        EblanAction.None -> {}

        is EblanAction.OpenApp -> {
            launcherApps.startMainActivity(
                componentName = eblanAction.componentName,
                sourceBounds = Rect(),
            )
        }

        EblanAction.OpenAppDrawer -> {
            onShowAppDrawer()
        }

        EblanAction.OpenNotificationPanel -> {
            val intent = Intent(GlobalAction.NAME).putExtra(
                GlobalAction.GLOBAL_ACTION_TYPE,
                GlobalAction.Notifications.name,
            )

            context.sendBroadcast(intent)
        }

        EblanAction.LockScreen -> {
            val intent = Intent(GlobalAction.NAME).putExtra(
                GlobalAction.GLOBAL_ACTION_TYPE,
                GlobalAction.LockScreen.name,
            )

            context.sendBroadcast(intent)
        }

        EblanAction.OpenQuickSettings -> {
            val intent = Intent(GlobalAction.NAME).putExtra(
                GlobalAction.GLOBAL_ACTION_TYPE,
                GlobalAction.QuickSettings.name,
            )

            context.sendBroadcast(intent)
        }

        EblanAction.OpenRecents -> {
            val intent = Intent(GlobalAction.NAME).putExtra(
                GlobalAction.GLOBAL_ACTION_TYPE,
                GlobalAction.Recents.name,
            )

            context.sendBroadcast(intent)
        }
    }
}
