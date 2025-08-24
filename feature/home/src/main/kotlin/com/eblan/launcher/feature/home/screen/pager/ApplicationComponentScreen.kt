package com.eblan.launcher.feature.home.screen.pager

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.screen.shortcut.ShortcutScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen

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
    gridWidth: Int,
    gridHeight: Int,
    dockHeight: Int,
    paddingValues: PaddingValues,
    drag: Drag,
    appDrawerRowsHeight: Int,
    hasShortcutHostPermission: Boolean,
    gridItemSettings: GridItemSettings,
    onDismiss: () -> Unit,
    onAnimateDismiss: () -> Unit,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
        intOffset: IntOffset,
    ) -> Unit,
) {
    val overscrollAlpha = remember { Animatable(0f) }

    BackHandler {
        onAnimateDismiss()
    }

    Surface(
        modifier = modifier
            .graphicsLayer(alpha = 1f - (overscrollAlpha.value / 500f))
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
                                appDrawerColumns = appDrawerColumns,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanApplicationInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanApplicationInfos,
                                appDrawerRowsHeight = appDrawerRowsHeight,
                                gridItemSettings = gridItemSettings,
                                drag = drag,
                                paddingValues = paddingValues,
                                overscrollAlpha = overscrollAlpha,
                                onLongPressGridItem = onLongPressGridItem,
                                onFling = onDismiss,
                                onFastFling = onAnimateDismiss,
                            )
                        }

                        1 -> {
                            WidgetScreen(
                                currentPage = gridHorizontalPagerState.currentPage,
                                rows = rows,
                                columns = columns,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanAppWidgetProviderInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanAppWidgetProviderInfos,
                                gridWidth = gridWidth,
                                gridHeight = gridHeight,
                                dockHeight = dockHeight,
                                gridItemSettings = gridItemSettings,
                                drag = drag,
                                paddingValues = paddingValues,
                                overscrollAlpha = overscrollAlpha,
                                onLongPressGridItem = onLongPressGridItem,
                                onFling = onDismiss,
                                onFastFling = onAnimateDismiss,
                            )
                        }

                        2 -> {
                            ShortcutScreen(
                                currentPage = gridHorizontalPagerState.currentPage,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanShortcutInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanShortcutInfos,
                                gridItemSettings = gridItemSettings,
                                drag = drag,
                                paddingValues = paddingValues,
                                overscrollAlpha = overscrollAlpha,
                                onLongPressGridItem = onLongPressGridItem,
                                onFling = onDismiss,
                                onFastFling = onAnimateDismiss,
                            )
                        }
                    }
                }
            }
        }
    }
}