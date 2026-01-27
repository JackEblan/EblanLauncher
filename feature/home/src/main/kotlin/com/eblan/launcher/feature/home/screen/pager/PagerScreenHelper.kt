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
import android.os.IBinder
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.PagerState
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.feature.home.model.Klwp
import com.eblan.launcher.feature.home.util.KUSTOM_ACTION
import com.eblan.launcher.feature.home.util.KUSTOM_ACTION_EXT_NAME
import com.eblan.launcher.feature.home.util.KUSTOM_ACTION_VAR_NAME
import com.eblan.launcher.feature.home.util.KUSTOM_ACTION_VAR_VALUE
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.handleEblanAction
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper
import kotlinx.serialization.json.Json

internal fun swipeEblanAction(
    gestureSettings: GestureSettings,
    swipeUpY: Float,
    swipeDownY: Float,
    screenHeight: Int,
    launcherApps: AndroidLauncherAppsWrapper,
    context: Context,
) {
    val swipeThreshold = 100f

    if (swipeUpY < screenHeight - swipeThreshold) {
        handleEblanAction(
            eblanAction = gestureSettings.swipeUp,
            launcherApps = launcherApps,
            context = context,
            onOpenAppDrawer = {},
        )
    }

    if (swipeDownY < screenHeight - swipeThreshold) {
        handleEblanAction(
            eblanAction = gestureSettings.swipeDown,
            launcherApps = launcherApps,
            context = context,
            onOpenAppDrawer = {},
        )
    }
}

internal suspend fun resetSwipeOffset(
    gestureSettings: GestureSettings,
    swipeDownY: Animatable<Float, AnimationVector1D>,
    screenHeight: Int,
    swipeUpY: Animatable<Float, AnimationVector1D>,
) {
    suspend fun animateOffset(
        eblanAction: EblanAction,
        swipeY: Animatable<Float, AnimationVector1D>,
    ) {
        if (eblanAction.eblanActionType == EblanActionType.OpenAppDrawer) {
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

    animateOffset(
        eblanAction = gestureSettings.swipeUp,
        swipeY = swipeUpY,
    )

    animateOffset(
        eblanAction = gestureSettings.swipeDown,
        swipeY = swipeDownY,
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
    swipeY: Animatable<Float, AnimationVector1D>,
    screenHeight: Int,
    showWidgets: Boolean,
    showShortcutConfigActivities: Boolean,
    eblanApplicationInfoGroup: EblanApplicationInfoGroup?,
    onHome: () -> Unit,
) {
    if (intent.action != Intent.ACTION_MAIN && !intent.hasCategory(Intent.CATEGORY_HOME)) {
        return
    }

    if ((intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
        return
    }

    onHome()

    if (swipeY.value < screenHeight.toFloat() || showWidgets || showShortcutConfigActivities || eblanApplicationInfoGroup != null) {
        return
    }

    gridHorizontalPagerState.scrollToPage(
        if (infiniteScroll) {
            (Int.MAX_VALUE / 2) + initialPage
        } else {
            initialPage
        },
    )

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
    launcherApps: AndroidLauncherAppsWrapper,
    context: Context,
    onOpenAppDrawer: () -> Unit,
) {
    if (intent.action != EblanAction.ACTION) return

    val eblanAction = intent.getStringExtra(EblanAction.NAME)?.let { eblanAction ->
        Json.decodeFromString<EblanAction>(eblanAction)
    } ?: return

    handleEblanAction(
        eblanAction = eblanAction,
        launcherApps = launcherApps,
        context = context,
        onOpenAppDrawer = onOpenAppDrawer,
    )
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
    onOpenAppDrawer: () -> Unit,
) {
    if (!hasDoubleTap) return

    handleEblanAction(
        eblanAction = gestureSettings.doubleTap,
        launcherApps = launcherApps,
        context = context,
        onOpenAppDrawer = onOpenAppDrawer,
    )
}

internal fun handleKlwpBroadcasts(
    klwpIntegration: Boolean,
    isApplicationScreenVisible: Boolean,
    context: Context,
) {
    if (!klwpIntegration) return

    val intent = Intent(KUSTOM_ACTION).apply {
        putExtra(KUSTOM_ACTION_EXT_NAME, "einstein-launcher")
        putExtra(KUSTOM_ACTION_VAR_NAME, "screen")
    }

    if (isApplicationScreenVisible) {
        context.sendBroadcast(
            intent.apply {
                putExtra(KUSTOM_ACTION_VAR_VALUE, Klwp.AppDrawer.ordinal)
            },
        )
    } else {
        context.sendBroadcast(
            intent.apply {
                putExtra(KUSTOM_ACTION_VAR_VALUE, Klwp.Pager.ordinal)
            },
        )
    }
}
