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
import android.graphics.Rect
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.util.Consumer
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GlobalAction
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.EblanShortcutInfoByGroup
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.shortcutconfig.ShortcutConfigScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalWallpaperManager
import kotlinx.coroutines.launch

@Composable
internal fun PagerScreen(
    modifier: Modifier = Modifier,
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    drag: Drag,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    hasShortcutHostPermission: Boolean,
    hasSystemFeatureAppWidgets: Boolean,
    gestureSettings: GestureSettings,
    appDrawerSettings: AppDrawerSettings,
    gridItemSource: GridItemSource?,
    homeSettings: HomeSettings,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfosByLabel: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    iconPackInfoPackageName: String,
    gridHorizontalPagerState: PagerState,
    currentPage: Int,
    statusBarNotifications: Map<String, Int>,
    eblanShortcutInfos: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    eblanShortcutConfigsByLabel: Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>,
    onTapFolderGridItem: (String) -> Unit,
    onDraggingGridItem: () -> Unit,
    onEdit: (String) -> Unit,
    onResize: () -> Unit,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onResetOverlay: () -> Unit,
) {
    val context = LocalContext.current

    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    var showApplicationInfos by remember { mutableStateOf(false) }

    var showWidgets by remember { mutableStateOf(false) }

    var showShortcutConfigActivities by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateEndPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val gridWidth = screenWidth - horizontalPadding

    val gridHeight = screenHeight - verticalPadding

    val swipeUpY = remember { Animatable(screenHeight.toFloat()) }

    val swipeDownY = remember { Animatable(screenHeight.toFloat()) }

    val wallpaperManagerWrapper = LocalWallpaperManager.current

    val view = LocalView.current

    val activity = LocalActivity.current as ComponentActivity

    val isApplicationComponentVisible by remember {
        derivedStateOf {
            swipeUpY.value == 0f || swipeDownY.value == 0f
        }
    }

    val swipeY by remember {
        derivedStateOf {
            if (swipeUpY.value < screenHeight.toFloat() && gestureSettings.swipeUp is GestureAction.OpenAppDrawer) {
                swipeUpY
            } else if (swipeDownY.value < screenHeight.toFloat() && gestureSettings.swipeDown is GestureAction.OpenAppDrawer) {
                swipeDownY
            } else {
                Animatable(screenHeight.toFloat())
            }
        }
    }

    val alpha by remember {
        derivedStateOf {
            val threshold = screenHeight / 2

            ((swipeY.value - threshold) / threshold).coerceIn(0f, 1f)
        }
    }

    DisposableEffect(key1 = scope) {
        val listener = Consumer<Intent> { intent ->
            scope.launch {
                handleActionMainIntent(
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    intent = intent,
                    initialPage = homeSettings.initialPage,
                    wallpaperScroll = homeSettings.wallpaperScroll,
                    wallpaperManagerWrapper = wallpaperManagerWrapper,
                    pageCount = homeSettings.pageCount,
                    infiniteScroll = homeSettings.infiniteScroll,
                    windowToken = view.windowToken,
                )
            }
        }

        activity.addOnNewIntentListener(listener)

        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }

    HorizontalPagerScreen(
        modifier = modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        scope.launch {
                            swipeUpY.snapTo(swipeUpY.value + dragAmount)

                            swipeDownY.snapTo(swipeDownY.value - dragAmount)
                        }
                    },
                    onDragEnd = {
                        doGestureActions(
                            gestureSettings = gestureSettings,
                            swipeUpY = swipeUpY.value,
                            swipeDownY = swipeDownY.value,
                            screenHeight = screenHeight,
                            onStartMainActivity = { componentName ->
                                launcherApps.startMainActivity(
                                    componentName = componentName,
                                    sourceBounds = Rect(),
                                )
                            },
                            onPerformGlobalAction = { globalAction ->
                                val intent = Intent(GlobalAction.NAME).putExtra(
                                    GlobalAction.GLOBAL_ACTION_TYPE,
                                    globalAction.name,
                                )

                                context.sendBroadcast(intent)
                            },
                        )

                        resetSwipeOffset(
                            scope = scope,
                            gestureSettings = gestureSettings,
                            swipeDownY = swipeDownY,
                            screenHeight = screenHeight,
                            swipeUpY = swipeUpY,
                        )
                    },
                    onDragCancel = {
                        scope.launch {
                            swipeUpY.animateTo(screenHeight.toFloat())

                            swipeDownY.animateTo(screenHeight.toFloat())
                        }
                    },
                )
            }
            .alpha(alpha),
        gridHorizontalPagerState = gridHorizontalPagerState,
        currentPage = currentPage,
        isApplicationComponentVisible = isApplicationComponentVisible,
        gridItems = gridItems,
        gridItemsByPage = gridItemsByPage,
        gridWidth = gridWidth,
        gridHeight = gridHeight,
        paddingValues = paddingValues,
        dockGridItems = dockGridItems,
        textColor = textColor,
        drag = drag,
        hasShortcutHostPermission = hasShortcutHostPermission,
        hasSystemFeatureAppWidgets = hasSystemFeatureAppWidgets,
        gridItemSource = gridItemSource,
        homeSettings = homeSettings,
        iconPackInfoPackageName = iconPackInfoPackageName,
        statusBarNotifications = statusBarNotifications,
        eblanShortcutInfos = eblanShortcutInfos,
        gestureSettings = gestureSettings,
        swipeY = swipeY.value,
        screenHeight = screenHeight,
        onTapFolderGridItem = onTapFolderGridItem,
        onEdit = onEdit,
        onResize = onResize,
        onSettings = onSettings,
        onEditPage = onEditPage,
        onWidgets = {
            showWidgets = true
        },
        onShortcutConfigActivities = {
            showShortcutConfigActivities = true
        },
        onDoubleTap = {
            showApplicationInfos = true
        },
        onLongPressGridItem = onLongPressGridItem,
        onUpdateGridItemOffset = onUpdateGridItemOffset,
        onDraggingGridItem = onDraggingGridItem,
        onDeleteGridItem = onDeleteGridItem,
        onResetOverlay = onResetOverlay,
    )

    if (gestureSettings.swipeUp is GestureAction.OpenAppDrawer || gestureSettings.swipeDown is GestureAction.OpenAppDrawer) {
        ApplicationScreen(
            currentPage = currentPage,
            offsetY = {
                swipeY.value
            },
            isApplicationComponentVisible = isApplicationComponentVisible,
            eblanApplicationComponentUiState = eblanApplicationComponentUiState,
            paddingValues = paddingValues,
            drag = drag,
            appDrawerSettings = appDrawerSettings,
            eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
            iconPackInfoPackageName = iconPackInfoPackageName,
            screenHeight = screenHeight,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
            gridItemSource = gridItemSource,
            onDismiss = {
                scope.launch {
                    swipeY.animateTo(
                        targetValue = screenHeight.toFloat(),
                        animationSpec = tween(
                            easing = FastOutSlowInEasing,
                        ),
                    )
                }
            },
            onDraggingGridItem = onDraggingGridItem,
            onResetOverlay = onResetOverlay,
            onVerticalDrag = { dragAmount ->
                scope.launch {
                    swipeY.snapTo(swipeY.value + dragAmount)
                }
            },
            onDragEnd = { remaining ->
                scope.launch {
                    handleApplyFling(
                        offsetY = swipeY,
                        remaining = remaining,
                        screenHeight = screenHeight,
                    )
                }
            },
        )
    }

    if (showApplicationInfos) {
        when (val gestureAction = gestureSettings.doubleTap) {
            GestureAction.None -> {
            }

            is GestureAction.OpenApp -> {
                SideEffect {
                    launcherApps.startMainActivity(
                        componentName = gestureAction.componentName,
                        sourceBounds = Rect(),
                    )

                    showApplicationInfos = false
                }
            }

            GestureAction.OpenAppDrawer -> {
                LaunchedEffect(key1 = Unit) {
                    swipeY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                    )
                }

                ApplicationScreen(
                    modifier = modifier,
                    currentPage = currentPage,
                    offsetY = {
                        swipeY.value
                    },
                    isApplicationComponentVisible = isApplicationComponentVisible,
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    paddingValues = paddingValues,
                    drag = drag,
                    appDrawerSettings = appDrawerSettings,
                    eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    screenHeight = screenHeight,
                    onLongPressGridItem = onLongPressGridItem,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    gridItemSource = gridItemSource,
                    onDismiss = {
                        scope.launch {
                            swipeY.animateTo(
                                targetValue = screenHeight.toFloat(),
                                animationSpec = tween(
                                    easing = FastOutSlowInEasing,
                                ),
                            )

                            showApplicationInfos = false
                        }
                    },
                    onDraggingGridItem = onDraggingGridItem,
                    onResetOverlay = onResetOverlay,
                    onVerticalDrag = { dragAmount ->
                        scope.launch {
                            swipeY.snapTo(swipeY.value + dragAmount)
                        }
                    },
                    onDragEnd = { remaining ->
                        scope.launch {
                            handleApplyFling(
                                offsetY = swipeY,
                                remaining = remaining,
                                screenHeight = screenHeight,
                                onDismiss = {
                                    showApplicationInfos = false
                                },
                            )
                        }
                    },
                )
            }

            GestureAction.OpenNotificationPanel -> {
                SideEffect {
                    val intent = Intent(GlobalAction.NAME).putExtra(
                            GlobalAction.GLOBAL_ACTION_TYPE,
                            GlobalAction.Notifications.name,
                        )

                    context.sendBroadcast(intent)

                    showApplicationInfos = false
                }
            }

            GestureAction.LockScreen -> {
                SideEffect {
                    val intent = Intent(GlobalAction.NAME).putExtra(
                            GlobalAction.GLOBAL_ACTION_TYPE,
                            GlobalAction.LockScreen.name,
                        )

                    context.sendBroadcast(intent)

                    showApplicationInfos = false
                }
            }

            GestureAction.OpenQuickSettings -> {
                SideEffect {
                    val intent = Intent(GlobalAction.NAME).putExtra(
                            GlobalAction.GLOBAL_ACTION_TYPE,
                            GlobalAction.QuickSettings.name,
                        )

                    context.sendBroadcast(intent)

                    showApplicationInfos = false
                }
            }

            GestureAction.OpenRecents -> {
                SideEffect {
                    val intent = Intent(GlobalAction.NAME).putExtra(
                            GlobalAction.GLOBAL_ACTION_TYPE,
                            GlobalAction.Recents.name,
                        )

                    context.sendBroadcast(intent)

                    showApplicationInfos = false
                }
            }
        }
    }

    if (showWidgets) {
        WidgetScreen(
            currentPage = currentPage,
            isApplicationComponentVisible = isApplicationComponentVisible,
            eblanApplicationComponentUiState = eblanApplicationComponentUiState,
            gridItemSettings = homeSettings.gridItemSettings,
            paddingValues = paddingValues,
            drag = drag,
            eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
            screenHeight = screenHeight,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
            onDismiss = {
                showWidgets = false
            },
            onDraggingGridItem = onDraggingGridItem,
            onResetOverlay = onResetOverlay,
        )
    }

    if (showShortcutConfigActivities) {
        ShortcutConfigScreen(
            currentPage = currentPage,
            isApplicationComponentVisible = isApplicationComponentVisible,
            eblanApplicationComponentUiState = eblanApplicationComponentUiState,
            paddingValues = paddingValues,
            drag = drag,
            gridItemSettings = homeSettings.gridItemSettings,
            eblanShortcutConfigsByLabel = eblanShortcutConfigsByLabel,
            screenHeight = screenHeight,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanShortcutConfigsByLabel = onGetEblanShortcutConfigsByLabel,
            onDismiss = {
                showShortcutConfigActivities = false
            },
            onDraggingGridItem = onDraggingGridItem,
            onResetOverlay = onResetOverlay,
        )
    }
}
