package com.eblan.launcher.feature.home.screen.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.screen.shortcut.ShortcutScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import kotlin.math.abs

@Composable
fun GestureActionScreen(
    modifier: Modifier = Modifier,
    gestureAction: GestureAction,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    gridHorizontalPagerState: PagerState,
    rows: Int,
    columns: Int,
    appDrawerColumns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    drag: Drag,
    appDrawerRowsHeight: Int,
    hasShortcutHostPermission: Boolean,
    dragIntOffset: IntOffset,
    onLongPressGridItem: (currentPage: Int, gridItemSource: GridItemSource) -> Unit,
    onDraggingGridItem: () -> Unit,
    onDismiss: suspend () -> Unit,
    onOpenAppDrawer: () -> Unit,
) {
    val launcherApps = LocalLauncherApps.current

    when (gestureAction) {
        is GestureAction.OpenApp -> {
            launcherApps.startMainActivity(gestureAction.componentName)

            LaunchedEffect(key1 = true) {
                onDismiss()
            }
        }

        GestureAction.OpenAppDrawer -> {
            onOpenAppDrawer()

            ApplicationComponentScreen(
                modifier = modifier,
                eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                gridHorizontalPagerState = gridHorizontalPagerState,
                rows = rows,
                columns = columns,
                appDrawerColumns = appDrawerColumns,
                pageCount = pageCount,
                infiniteScroll = infiniteScroll,
                rootWidth = rootWidth,
                rootHeight = rootHeight,
                dockHeight = dockHeight,
                drag = drag,
                appDrawerRowsHeight = appDrawerRowsHeight,
                hasShortcutHostPermission = hasShortcutHostPermission,
                dragIntOffset = dragIntOffset,
                onLongPress = onLongPressGridItem,
                onDragging = onDraggingGridItem,
                onDismiss = onDismiss,
            )
        }

        GestureAction.OpenNotificationPanel -> {
            LaunchedEffect(key1 = true) {
                onDismiss()
            }
        }

        GestureAction.None -> Unit
    }
}

@Composable
fun ApplicationComponentScreen(
    modifier: Modifier = Modifier,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    gridHorizontalPagerState: PagerState,
    rows: Int,
    columns: Int,
    appDrawerColumns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    drag: Drag,
    appDrawerRowsHeight: Int,
    hasShortcutHostPermission: Boolean,
    dragIntOffset: IntOffset,
    onLongPress: (
        currentPage: Int,
        newGridItemSource: GridItemSource,
    ) -> Unit,
    onDragging: () -> Unit,
    onDismiss: suspend () -> Unit,
) {
    val overscrollAlpha = remember { Animatable(0f) }

    val alpha by remember {
        derivedStateOf {
            1f - (abs(overscrollAlpha.value) / 100f)
        }
    }

    Surface(
        modifier = modifier
            .graphicsLayer(alpha = alpha)
            .fillMaxSize(),
    ) {
        when (eblanApplicationComponentUiState) {
            EblanApplicationComponentUiState.Loading -> {
                LoadingScreen()
            }

            is EblanApplicationComponentUiState.Success -> {
                val applicationHorizontalPagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = {
                        if (hasShortcutHostPermission) {
                            3
                        } else {
                            2
                        }
                    },
                )

                HorizontalPager(state = applicationHorizontalPagerState) { page ->
                    when (page) {
                        0 -> {
                            ApplicationScreen(
                                currentPage = gridHorizontalPagerState.currentPage,
                                overscrollAlpha = overscrollAlpha,
                                appDrawerColumns = appDrawerColumns,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanApplicationInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanApplicationInfos,
                                drag = drag,
                                appDrawerRowsHeight = appDrawerRowsHeight,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
                                onDismiss = onDismiss,
                            )
                        }

                        1 -> {
                            WidgetScreen(
                                currentPage = gridHorizontalPagerState.currentPage,
                                overscrollAlpha = overscrollAlpha,
                                rows = rows,
                                columns = columns,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanAppWidgetProviderInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanAppWidgetProviderInfos,
                                rootWidth = rootWidth,
                                rootHeight = rootHeight,
                                dockHeight = dockHeight,
                                drag = drag,
                                dragIntOffset = dragIntOffset,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
                                onDismiss = onDismiss,
                            )
                        }

                        2 -> {
                            ShortcutScreen(
                                currentPage = gridHorizontalPagerState.currentPage,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanShortcutInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanShortcutInfos,
                                drag = drag,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
                            )
                        }
                    }
                }
            }
        }
    }
}
